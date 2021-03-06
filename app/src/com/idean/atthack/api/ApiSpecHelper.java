package com.idean.atthack.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.idean.atthack.api.ApiSpec.HttpVerb;
import com.idean.atthack.network.RequestHelper;
import com.idean.atthack.network.Result;

/**
 * Helper for loading API specifications. They are encapsulated in the form of
 * the class {@link ApiSpec}.
 * <p>
 * Users can update the JSON source used to generate these specifications by
 * running the script:
 * <p>
 * 
 * <pre>
 * <code>
 * >> cd util
 * >> ./apicreate.py update
 * </code>
 * </pre>
 */
public enum ApiSpecHelper {
	SINGLETON;

	private static final String TAG = "ApiSpecHlp";
	private static final String JSON_URL = "http://ericsson-innovate.github.io/hackathon-portal/dist/data/specifications.json";

	private Map<ApiCategory, List<ApiSpec>> mCat2Specs = new HashMap<ApiCategory, List<ApiSpec>>();

	/**
	 * Must not be done on the UI thread
	 */
	public void loadSpecs(Context context) {
		Log.d(TAG, "loadSpecs");
		if (mCat2Specs.isEmpty()) {
			Log.d(TAG, "Loading specs from the server");
			loadApiSpecFromRemoteJson(context);
			//loadApiSpecFromLocalJson(context);
		}
	}

	public List<ApiSpec> getSpecs(ApiCategory category) {
		Log.d(TAG, "Get specs for category " + category);
		List<ApiSpec> res = mCat2Specs.get(category);
		if (res == null) {
			return new ArrayList<ApiSpec>();
		}
		return res;
	}

	/**
	 * @param apiSpecId
	 *            matches the "id" attribute of an {@link ApiSpec}
	 * @return null if no spec found matching the spec ID
	 */
	public ApiSpec getSpec(String apiSpecId) {
		// less efficient, but less redundancy of the data
		for (ApiCategory cat : ApiCategory.values()) {
			List<ApiSpec> specs = mCat2Specs.get(cat);
			for (ApiSpec spec : specs) {
				if (spec.id.equals(apiSpecId)) {
					return spec;
				}
			}
		}
		return null;
	}

	public boolean isSupported(ApiSpec spec) {
		return getAnnotation(spec) != null;
	}

	public boolean isRequireCheckStatus(ApiSpec spec) {
		ApiName name = getAnnotation(spec);
		Log.d(TAG,
				"Got api name " + name.value() + " : "
						+ name.isRequireStatusCheck());
		return name != null && name.isRequireStatusCheck();
	}

	public Result executeApi(ApiSpec spec, RequestHelper helper, Bundle params) {
		Class<RequestHelper> parent = RequestHelper.class;
		for (Method method : parent.getDeclaredMethods()) {
			ApiName annotation = (ApiName) method.getAnnotation(ApiName.class);
			if (annotation != null && annotation.value().equals(spec.id)) {
				try {
					return (Result) method.invoke(helper,
							new Object[] { params });
				} catch (Exception e) {
					Log.w(TAG, "Unable to execute api " + spec.id, e);
					return new Result("Unable to execute " + spec.id + ": "
							+ e.getMessage());
				}
			}
		}
		return new Result("No support for API " + spec.id);
	}

	private ApiName getAnnotation(ApiSpec spec) {
		Class<RequestHelper> parent = RequestHelper.class;
		for (Method method : parent.getDeclaredMethods()) {
			ApiName annotation = (ApiName) method.getAnnotation(ApiName.class);
			if (annotation != null) {
				String name = annotation.value();
				if (name.equals(spec.id)) {
					return annotation;
				}
			}
		}
		return null;
	}

	private void loadApiSpecFromLocalJson(Context context) {
		AssetManager mgr = context.getAssets();
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(mgr.open("specifications.json"));
			loadApiSpecFromInputStream(reader);
		} catch (IOException e) {
			Log.w(TAG, "Unable to open local asset", e);
		}
	}

	private void loadApiSpecFromRemoteJson(Context context) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(JSON_URL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(HttpVerb.GET.name());
			conn.connect();
			InputStreamReader reader = new InputStreamReader(
					conn.getInputStream());
			loadApiSpecFromInputStream(reader);
		} catch (Exception e) {
			Log.w(TAG, "Unable to get remote API spec", e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private void loadApiSpecFromInputStream(InputStreamReader reader) {
		Gson gson = new Gson();

		try {
			ApiSpecRaw[] specs = gson.fromJson(reader, ApiSpecRaw[].class);
			if (specs != null && specs.length > 0) {
				Log.d(TAG, "Got " + specs.length
						+ " api specs from remote source");
				for (ApiSpecRaw specRaw : specs) {
					ApiSpec spec = new ApiSpec(specRaw);
					if (spec.isValid()) {
						// Add spec to first category it matches
						ApiCategory cat = ApiCategory.derive(spec);
						if (cat == null) {
							Log.w(TAG, "Unable to resolve category for spec "
									+ spec.id);
							continue;
						}
						List<ApiSpec> res = mCat2Specs.get(cat);
						if (res == null) {
							// Lazily initialize
							res = new ArrayList<ApiSpec>();
							mCat2Specs.put(cat, res);
						}
						res.add(spec);
					} else {
						Log.w(TAG, "Skipping invalid spec, " + spec.id);
					}
				}
			}
			for (ApiCategory cat : ApiCategory.values()) {
				List<ApiSpec> res = mCat2Specs.get(cat);
				Log.d(TAG, "For category " + cat + ", got "
						+ ((res != null) ? res.size() : "0") + " API specs");
			}

		} catch (Exception e) {
			Log.w(TAG, "Unable to get Api JSON spec " + e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		Log.d(TAG, "Loaded " + mCat2Specs.size() + " API categories");
	}

	public enum ApiCategory {
		KNOW_DRIVER("know-driver", "Know Driver"), KNOW_CAR("know-car",
				"Know Car"), CONTROL_CAR("control-car", "Control Car"), INTERNAL("internal","[Internal]");

		public String machineName;
		public String displayName;

		public static ApiCategory derive(ApiSpec spec) {
			if (spec != null && spec.categories != null
					&& spec.categories.length > 0) {
				for (String cat : spec.categories) {
					ApiCategory category = ApiCategory.fromMachineName(cat);
					if (category == null) {
						Log.w(TAG, "Unable to resolve category name " + cat);
						continue;
					} else {
						return category;
					}
				}
				Log.w(TAG, "None of the spec categories were recognized: "
						+ spec.id);
				return null;
			} else {
				Log.w(TAG, "Spec doesn't have any categories: " + spec.id);
				return null;
			}
		}

		private static ApiCategory fromMachineName(String cat) {
			// Not most efficient, but readable
			for (ApiCategory category : values()) {
				if (category.machineName.equals(cat)) {
					return category;
				}
			}
			return null;
		}

		private ApiCategory(String machineName, String displayName) {
			this.machineName = machineName;
			this.displayName = displayName;
		}
	}
}
