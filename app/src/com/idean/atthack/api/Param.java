package com.idean.atthack.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * Parameter names used in the Connected Car API's
 */
public enum Param {
	username(Type.STRING), vin(Type.STRING), pin(Type.INTEGER), latitude(
			Type.FLOAT), longitude(Type.FLOAT), accuracy(Type.FLOAT),

	iccid(Type.STRING), imsi(Type.STRING), msisdn(Type.STRING), tcusn(
			Type.STRING), make(Type.STRING), model(Type.STRING), year(
			Type.INTEGER), description(Type.STRING), condition(Type.STRING), deliveryMileage(
			Type.STRING), deliveryDate(Type.STRING), licenseNumber(Type.STRING), engineNumber(
			Type.STRING), transmissionNumber(Type.STRING), ignitionKeyNumber(
			Type.STRING), doorKeyNumber(Type.STRING), status(Type.STRING), doors(
			Type.STRING), interiorColor(Type.STRING), exteriorColor(Type.STRING), transmissionType(
			Type.STRING), weight(Type.STRING), category(Type.STRING), options(
			Type.STRING), owner(Type.STRING), ownerType(Type.STRING), users(
			Type.STRING), custom(Type.STRING), metas(Type.STRING),

	query(Type.STRING), startNum(Type.INTEGER), pageSize(Type.INTEGER), sortItem(
			Type.STRING);

	public Type type;

	private Param(Type type) {
		this.type = type;
	}

	public enum Type {
		INTEGER, STRING, FLOAT, BOOLEAN, UNKNOWN
	}

	private static final String TAG = "Param";

	/**
	 * Put value into bundle using the appropriate type for the value
	 */
	public void putBundleAsTypedVal(Bundle bundle, String val) {
		if (TextUtils.isEmpty(val)) {
			return;
		}
		try {
			switch (type) {
			case BOOLEAN:
				bundle.putBoolean(name(), Boolean.parseBoolean(val));
				break;
			case FLOAT:
				bundle.putFloat(name(), Float.parseFloat(val));
				break;
			case INTEGER:
				bundle.putFloat(name(), Integer.parseInt(val));
				break;
			case STRING:
				bundle.putString(name(), val);
				break;
			default:
				throw new UnsupportedOperationException();
			}
		} catch (Exception e) {
			Log.w(TAG, "Unable to put value into bundle " + this + ", val: "
					+ val);
		}
	}

	/**
	 * Util for logging bundle of params
	 */
	public static String toString(Bundle bundle) {
		StringBuffer b = new StringBuffer();
		for (String key : bundle.keySet()) {
			b.append("[" + key + "] ");
			b.append(bundle.get(key) + ", ");
		}
		return b.toString();
	}

	/**
	 * Add param value from Bundle to JsonObject
	 * 
	 * @param obj
	 * @param params
	 */
	public void addToJson(JSONObject obj, Bundle params) {
		if (params.containsKey(name())) {
			try {
				switch (type) {
				case BOOLEAN:
					obj.put(name(), params.getBoolean(name()));
					break;
				case FLOAT:
					obj.put(name(), params.getFloat(name()));
					break;
				case INTEGER:
					obj.put(name(), params.getInt(name()));
					break;
				case STRING:
					obj.put(name(), params.getString(name()));
					break;
				default:
				}
			} catch (JSONException e) {
				Log.w(TAG, "Unable to add param " + this + " to json");
			}
		}
	}
	
	public String getAsString(Bundle params) {
		return params.getString(name());
	}

	public void addToJsonAsObject(JSONObject parent, Bundle params) {
		String defaultVal = params.getString(name());
		if (defaultVal == null) {
			Log.w(TAG, "Unable to add this param " + this + " as a JSON object");
			return;
		}
		try {
			JSONObject obj = new JSONObject(defaultVal);
			parent.put(name(), obj);
		} catch (JSONException e) {
			try {
				JSONArray arr = new JSONArray(defaultVal);
				parent.put(name(), arr);
			} catch (JSONException e1) {
				Log.w(TAG, "Unable to parse as JSON " + defaultVal);
			}
		}

	}
}