package com.idean.atthack.network;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.idean.atthack.Pref;
import com.idean.atthack.api.ApiName;
import com.idean.atthack.api.ApiSpec.HttpVerb;
import com.idean.atthack.api.Param;

/**
 * Mark API's that are supported with {@link ApiName} annotation. The app will
 * automatically show that the annotated API is supported and attempt to execute
 * the API with the corresponding method.
 * <p>
 * All API methods should have the signature
 * <code>Result fooBar(Bundle params)</code>
 */
public class RequestHelper {
	public final static String DEFAULT_FALLBACK_BASE = "http://lightning.att.io:3000/";
	private static final String TAG = "ReqHlp";
	private Context mContext;

	public RequestHelper(Context context) {
		mContext = context;
		if (!Pref.VIN.contains(mContext) || !Pref.USERNAME.contains(mContext)
				|| !Pref.PASSWORD.contains(mContext)) {
			Log.w(TAG, "Missing required preference values: vin, pin, username");
		}

		Log.d(TAG, "Found username " + Pref.USERNAME.get(context) + "Pin "
				+ Pref.PIN.get(context));
	}

	private String getUrlBase() {
		String base = Pref.SERVER.get(mContext);
		if (TextUtils.isEmpty(base)) {
			return DEFAULT_FALLBACK_BASE;
		}
		if (!base.endsWith("/")) {
			return base + "/";
		}
		return base;
	}

	// ## START 2.6.4-login
	// Should be disabled in the UI. So don't annotate
	// @ApiName(value="2.6.4-login", isRequireStatusCheck=false)
	public Result login(Bundle params) {
		String username = params.getString(Param.username.name());
		String vin = params.getString(Param.vin.name());
		String pin = params.getString(Param.pin.name());
		if (username == null || vin == null || pin == null) {
			return new Result(400);
		}

		HttpURLConnection conn = null;
		try {
			URL url = new URL(getUrlBase() + "remoteservices/v1/vehicle/login/"
					+ vin);

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(HttpVerb.POST.name());
			String auth = username + ":" + pin;
			String encoded = Base64.encodeToString(auth.getBytes(),
					Base64.DEFAULT);
			conn.setRequestProperty("Authorization", "Basic " + encoded);

			conn.connect();
			// Closes input stream afterwards
			String body = readStream(conn.getInputStream());
			return new Result(conn.getResponseCode(), body);

		} catch (IOException e) {
			Log.w(TAG, "Unable to login " + e.getMessage(), e);
			return new Result("Unable to login: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	// ## END 2.6.4-login

	// ## START 2.6.1-sign-up
	// ## START 2.6.2-validate-otp
	// ## START 2.6.3-set-pin
	// ## START 2.6.5-door-unlock
	@ApiName(value = "2.6.5-door-unlock", isRequireStatusCheck = true)
	public Result unlockDoor(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.latitude.addToJson(obj, params);
		Param.longitude.addToJson(obj, params);
		Param.accuracy.addToJson(obj, params);
		String urlStr = getUrlBase() + "remoteservices/v1/vehicle/unlock/"
				+ Pref.VIN.get(mContext);
		return sendHttpPost(obj, urlStr);
	}

	// ## END 2.6.1-sign-up
	// ## END 2.6.2-validate-otp
	// ## END 2.6.3-set-pin
	// ## END 2.6.5-door-unlock

	// ## START 2.6.6-door-lock
	@ApiName(value = "2.6.6-door-lock", isRequireStatusCheck = true)
	public Result lockDoor(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.latitude.addToJson(obj, params);
		Param.longitude.addToJson(obj, params);
		Param.accuracy.addToJson(obj, params);
		String urlStr = getUrlBase() + "remoteservices/v1/vehicle/lock/"
				+ Pref.VIN.get(mContext);

		return sendHttpPost(obj, urlStr);
	}

	// ## END 2.6.6-door-lock

	// ## START 2.6.7-engine-on
	@ApiName(value = "2.6.7-engine-on", isRequireStatusCheck = true)
	public Result engineOn(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.latitude.addToJson(obj, params);
		Param.longitude.addToJson(obj, params);
		Param.accuracy.addToJson(obj, params);
		String urlStr = getUrlBase() + "remoteservices/v1/vehicle/engineOn/"
				+ Pref.VIN.get(mContext);
		return sendHttpPost(obj, urlStr);
	}

	// ## END 2.6.7-engine-on

	// ## START 2.6.8-engine-off
	@ApiName(value = "2.6.8-engine-off", isRequireStatusCheck = true)
	public Result engineOff(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.latitude.addToJson(obj, params);
		Param.longitude.addToJson(obj, params);
		Param.accuracy.addToJson(obj, params);
		String urlStr = getUrlBase() + "remoteservices/v1/vehicle/engineOff/"
				+ Pref.VIN.get(mContext);
		return sendHttpPost(obj, urlStr);
	}

	// ## END 2.6.8-engine-off

	// ## START 2.6.9-honk-and-blink
	@ApiName(value = "2.6.9-honk-and-blink", isRequireStatusCheck = true)
	public Result honkAndBlink(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.latitude.addToJson(obj, params);
		Param.longitude.addToJson(obj, params);
		Param.accuracy.addToJson(obj, params);
		String urlStr = getUrlBase() + "remoteservices/v1/vehicle/honkBlink/"
				+ Pref.VIN.get(mContext);
		return sendHttpPost(obj, urlStr);
	}

	// ## END 2.6.9-honk-and-blink

	// ## START 2.6.10-check-request-status
	// ## START 2.6.11-view-diagnostic-data
	@ApiName(value = "2.6.11-view-diagnostic-data", isRequireStatusCheck = true)
	public Result getDiagnosticData(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.latitude.addToJson(obj, params);
		Param.longitude.addToJson(obj, params);
		Param.accuracy.addToJson(obj, params);
		String urlStr = getUrlBase()
				+ "remoteservices/v1/vehicle/diagnostics/view/"
				+ Pref.VIN.get(mContext);
		return sendHttpPost(obj, urlStr);
	}

	// ## END 2.6.10-check-request-status
	// ## END 2.6.11-view-diagnostic-data

	// ## START 2.6.12-get-vehicle-status
	@ApiName(value = "2.6.12-get-vehicle-status", isRequireStatusCheck = true)
	public Result getVehicleStatus(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.latitude.addToJson(obj, params);
		Param.longitude.addToJson(obj, params);
		Param.accuracy.addToJson(obj, params);
		String urlStr = getUrlBase() + "remoteservices/v1/vehicle/status/view/"
				+ Pref.VIN.get(mContext);

		return sendHttpPost(obj, urlStr);
	}
	// ## END 2.6.12-get-vehicle-status

	// ## START 2.16.1-add-a-vehicle
	@ApiName(value = "2.16.1-add-a-vehicle", isRequireStatusCheck = false)
	public Result addVehicle(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.vin.addToJson(obj, params);
		Param.iccid.addToJson(obj, params);
		Param.imsi.addToJson(obj, params);
		Param.msisdn.addToJson(obj, params);
		Param.tcusn.addToJson(obj, params);
		Param.make.addToJson(obj, params);
		Param.model.addToJson(obj, params);
		Param.year.addToJson(obj, params);
		Param.description.addToJson(obj, params);
		Param.condition.addToJson(obj, params);
		Param.deliveryMileage.addToJson(obj, params);
		Param.deliveryDate.addToJson(obj, params);
		Param.licenseNumber.addToJson(obj, params);
		Param.engineNumber.addToJson(obj, params);
		Param.transmissionNumber.addToJson(obj, params);
		Param.ignitionKeyNumber.addToJson(obj, params);
		Param.doorKeyNumber.addToJson(obj, params);
		Param.status.addToJson(obj, params);
		Param.doors.addToJson(obj, params);
		Param.interiorColor.addToJsonAsObject(obj, params);
		Param.exteriorColor.addToJsonAsObject(obj, params);
		Param.transmissionType.addToJson(obj, params);
		Param.weight.addToJson(obj, params);
		Param.category.addToJson(obj, params);		
		Param.options.addToJson(obj, params);
		Param.owner.addToJson(obj, params);
		Param.ownerType.addToJson(obj, params);
		Param.users.addToJsonAsObject(obj, params);
		Param.custom.addToJsonAsObject(obj, params);
		Param.metas.addToJsonAsObject(obj, params);
		String urlStr = getUrlBase() + "vehicles/v1/vehicle/add";
		return sendHttpPost(obj, urlStr);
	}

	// ## END 2.16.1-add-a-vehicle

	// ## START 2.16.2-update-a-vehicle
	@ApiName(value = "2.16.2-update-a-vehicle", isRequireStatusCheck = false)
	public Result updateVehicle(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.vin.addToJson(obj, params);
		Param.iccid.addToJson(obj, params);
		Param.imsi.addToJson(obj, params);
		Param.msisdn.addToJson(obj, params);
		Param.tcusn.addToJson(obj, params);
		Param.make.addToJson(obj, params);
		Param.model.addToJson(obj, params);
		Param.year.addToJson(obj, params);
		Param.description.addToJson(obj, params);
		Param.condition.addToJson(obj, params);
		Param.deliveryMileage.addToJson(obj, params);
		Param.deliveryDate.addToJson(obj, params);
		Param.licenseNumber.addToJson(obj, params);
		Param.engineNumber.addToJson(obj, params);
		Param.transmissionNumber.addToJson(obj, params);
		Param.ignitionKeyNumber.addToJson(obj, params);
		Param.doorKeyNumber.addToJson(obj, params);
		Param.status.addToJson(obj, params);
		Param.doors.addToJson(obj, params);
		Param.interiorColor.addToJsonAsObject(obj, params);
		Param.exteriorColor.addToJsonAsObject(obj, params);
		Param.transmissionType.addToJson(obj, params);
		Param.weight.addToJson(obj, params);
		Param.category.addToJson(obj, params);
		Param.options.addToJson(obj, params);
		Param.owner.addToJson(obj, params);
		Param.ownerType.addToJson(obj, params);
		Param.users.addToJsonAsObject(obj, params);
		Param.custom.addToJsonAsObject(obj, params);
		Param.metas.addToJsonAsObject(obj, params);
		String urlStr = getUrlBase() + "vehicles/v1/vehicle/update/" + Pref.VIN.get(mContext);
		return sendHttpPost(obj, urlStr);
	}

	// ## END 2.16.2-update-a-vehicle
	
	
	// ## START 2.16.3-delete-a-vehicle
	@ApiName(value = "2.16.3-delete-a-vehicle", isRequireStatusCheck = false)
	public Result deleteVehicle(Bundle params) {
		JSONObject obj = new JSONObject();
		String urlStr = getUrlBase() + "vehicles/v1/vehicle/delete/"
				+ Pref.VIN.get(mContext);
		return sendHttpPost(obj, urlStr);
	}
	// ## END 2.16.3-delete-a-vehicle

	
	// ## START 2.16.4-view-a-vehicle
	@ApiName(value = "2.16.4-view-a-vehicle", isRequireStatusCheck = false)
	public Result viewVehicle(Bundle params) {
		String urlStr = getUrlBase() + "vehicles/v1/vehicle/view/"
				+ Pref.VIN.get(mContext);
		return sendHttpGet(urlStr);
	}
	// ## END 2.16.4-view-a-vehicle

	
	// ## START 2.16.5-update-vehicle-users
	@ApiName(value = "2.16.5-update-vehicle-users", isRequireStatusCheck = false)
	public Result updateVehicleUsers(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.vin.addToJson(obj, params);
		Param.users.addToJsonAsObject(obj,  params);
		String urlStr = getUrlBase() + "vehicles/v1/users/update";
		return sendHttpPost(obj, urlStr);
	}
	// ## END 2.16.5-update-vehicle-users

	
	// ## START 2.16.6-delete-vehicle-users
	@ApiName(value = "2.16.6-delete-vehicle-users", isRequireStatusCheck = false)
	public Result deleteVehicleUsers(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.vin.addToJson(obj, params);
		Param.users.addToJsonAsObject(obj,  params);
		String urlStr = getUrlBase() + "vehicles/v1/users/delete";
		return sendHttpPost(obj, urlStr);
	}
	// ## END 2.16.6-delete-vehicle-users

	
	// ## START 2.16.7-search-vehicles
	@ApiName(value = "2.16.7-search-vehicles", isRequireStatusCheck = false)
	public Result searchVehicles(Bundle params) {
		JSONObject obj = new JSONObject();
		Param.query.addToJson(obj, params);
		Param.startNum.addToJson(obj, params);
		Param.pageSize.addToJson(obj, params);
		Param.sortItem.addToJsonAsObject(obj, params);		
		String urlStr = getUrlBase() + "vehicles/v1/users/search";
		return sendHttpPost(obj, urlStr);
	}
	// ## END 2.16.7-search-vehicles

	

	// Disabled in the UI
	public Result checkRequestStatus(String requestId) {
		String urlStr = getUrlBase() + "remoteservices/v1/vehicle/status/"
				+ Pref.VIN.get(mContext) + "/" + requestId;
		return sendHttpGet(urlStr);
	}

	// ## START COMMON
	/**
	 * Send secured HTTP Post to argument URL. Secured with basic http
	 * authentication
	 */
	private Result sendHttpPost(JSONObject obj, String urlStr) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(HttpVerb.POST.name());
			setBasicAuth(conn);
			conn.setDoOutput(true);
			writeBody(conn, obj);
			conn.connect();
			Log.d(TAG, "[" + conn.getRequestMethod() + "] to " + url.toString());

			String body = readStream(conn.getInputStream());
			return new Result(conn.getResponseCode(), body);
		} catch (IOException e) {
			return new Result(e.getClass() + ": " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private Result sendHttpGet(String urlStr) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(HttpVerb.GET.name());
			setBasicAuth(conn);
			conn.connect();
			Log.d(TAG, "[" + conn.getRequestMethod() + "] to " + url.toString());

			String body = readStream(conn.getInputStream());
			return new Result(conn.getResponseCode(), body);
		} catch (IOException e) {
			return new Result(e.getClass() + ": " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private void writeBody(HttpURLConnection urlConnection, JSONObject obj) {
		try {
			String body = obj.toString();
			if (obj.length() == 0 || TextUtils.isEmpty(body)) {
				return;
			}
			urlConnection
					.setRequestProperty("Content-Type", "application/json");

			PrintWriter writer = new PrintWriter(new BufferedOutputStream(
					urlConnection.getOutputStream()));
			writer.print(body); // presumably in UTF-8
			Log.d(TAG, "Wrote request body: \n\n" + body);
		} catch (Exception e) {
			Log.w(TAG, "Unable to send request body", e);
		}
	}

	private void setBasicAuth(HttpURLConnection conn) {
		String auth = Pref.USERNAME.get(mContext) + ":"
				+ Pref.PIN.get(mContext);
		String encoded = Base64.encodeToString(auth.getBytes(), Base64.DEFAULT);
		conn.setRequestProperty("Authorization", "Basic " + encoded);
	}

	public static String readStream(InputStream in) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuffer b = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				b.append(line);
			}
			return b.toString();
		} catch (Exception e) {
			Log.w(TAG, "Unable to read stream", e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}
	// ## END COMMON

}
