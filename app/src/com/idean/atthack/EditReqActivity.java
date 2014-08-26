package com.idean.atthack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.idean.atthack.api.ApiSpec;
import com.idean.atthack.api.ApiSpecHelper;
import com.idean.atthack.api.ApiSpecRaw.ReqParam;
import com.idean.atthack.api.Param;

public class EditReqActivity extends ActionBarActivity {

	private static final String TAG = "EditReq";
	public static final String EXTRA_SPECID = "EXTRA_SPECID";
	private ApiSpec mSpec;
	private Map<Param, EditText> mParam2Field;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mParam2Field = new HashMap<Param, EditText>();

		setContentView(R.layout.apirequest);
		mSpec = getSpec(getIntent());
		if (mSpec == null) {
			Log.w(TAG, "Unable to determine api specfrom intent");
			Toast.makeText(this, R.string.no_api, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// bind name and description
		getSupportActionBar().setTitle(mSpec.name);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setText(R.id.description, "[" + mSpec.docNumber + "] "
				+ mSpec.description + "\n\n" + mSpec.route());

		// bind request params
		bindRequestParams(mSpec, mParam2Field);
		final ScrollView scroll = (ScrollView) findViewById(R.id.requestScroll);
		scroll.postDelayed(new Runnable() {
			@Override
			public void run() {
				scroll.fullScroll(ScrollView.FOCUS_UP);
			}
		}, 400);
	}

	private void bindRequestParams(ApiSpec spec,
			Map<Param, EditText> param2Field) {
		ReqParam[] params = spec.requestParams();
		List<String> routeParams = spec.routeParams();
		if (params == null && routeParams.size() == 0) {
			findViewById(R.id.request_empty).setVisibility(View.VISIBLE);
			return;
		}
		findViewById(R.id.request_empty).setVisibility(View.GONE);

		ViewGroup parent = (ViewGroup) findViewById(R.id.requestParent);

		// Bind the route parameters

		Log.d(TAG, "Got routeParams " + routeParams);
		if (routeParams != null && routeParams.size() > 0) {
			for (String routeParam : routeParams) {
				Param param = null;
				try {
					param = Param.valueOf(routeParam);
				} catch (Exception e) {
					param = null;
				}
				if (param == null) {
					Log.w(TAG, "Unable to process param " + routeParam
							+ ". Skipping");
					continue;
				}
				View view = ParamHelper.SINGLETON.createRouteParamWidget(this,
						param, routeParam);
				ParamHelper.SINGLETON.addWidget(view, parent);

				// Map param key to the editText that contains the value of the
				// parameter we're sending
				EditText edit = (EditText) view.findViewById(R.id.param_val);
				// For VIN, set the stored value as the default value for the field
				handleSetVinDefaultValue(edit, param);
		
				if (edit != null) {
					param2Field.put(param, edit);
				}
			}
		}

		if (params != null) {
			for (ReqParam param : params) {
				if (!param.isValid() || param2Field.containsKey(param.key())) {
					Log.w(TAG, "Skipping invalid param " + param.description
							+ ". Or skipping b/c key already has been set");
					continue;
				}
				View view = ParamHelper.SINGLETON
						.createParamWidget(this, param);
				ParamHelper.SINGLETON.addWidget(view, parent);

				// Map param key to the editText that contains the value of the
				// parameter we're sending
				EditText edit = (EditText) view.findViewById(R.id.param_val);
				Param paramKey = param.key();
				
				// For VIN, set the stored value as the default value for the field
				handleSetVinDefaultValue(edit, paramKey);
				if (edit != null && paramKey != null) {
					param2Field.put(paramKey, edit);
				}
			}
		}
	}

	private void handleSetVinDefaultValue(EditText edit, Param paramKey) {
		if (paramKey.equals(Param.vin)) {
			edit.setText(Pref.VIN.get(this));
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_edit_req, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_send) {
			new GatherRequestTask().execute(null, null, null);
			return true;
		} else if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setText(int viewId, String text) {
		((TextView) findViewById(viewId)).setText(text);
	}

	public static ApiSpec getSpec(Intent intent) {
		String specName = intent.getStringExtra(EXTRA_SPECID);
		if (specName == null) {
			return null;
		}
		return ApiSpecHelper.SINGLETON.getSpec(specName);
	}

	private class GatherRequestTask extends AsyncTask<Void, Void, Bundle> {

		@Override
		protected void onPostExecute(final Bundle bundle) {
			Intent i = new Intent(EditReqActivity.this, ViewRespActivity.class);
			i.putExtra(ViewRespActivity.EXTRA_REQ_PARAMS, bundle);
			i.putExtra(EXTRA_SPECID, mSpec.id);
			startActivity(i);
		}

		@Override
		protected Bundle doInBackground(Void... params) {

			// Extract info from EditText UI fields
			Bundle bundle = new Bundle();
			Set<Entry<Param, EditText>> entries = mParam2Field.entrySet();
			for (Entry<Param, EditText> entry : entries) {
				if (entry.getKey() != null && entry.getValue() != null) {
					EditText edit = entry.getValue();
					String val = edit.getText().toString();
					Param param = entry.getKey();
					param.putBundleAsTypedVal(bundle, val);
				}
			}
			return bundle;
		}

	}

}
