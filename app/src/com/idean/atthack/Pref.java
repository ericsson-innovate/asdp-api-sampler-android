package com.idean.atthack;

import android.content.Context;
import android.preference.PreferenceManager;

public enum Pref {

	USERNAME("pref_username"),
	PASSWORD("pref_password"),
	PIN("pref_pin"),
	VIN("pref_vin"),
	SERVER("pref_server");
	
	
	public String key;
	private Pref(String key) {
		this.key = key;
	}
	
	public boolean contains(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
	}
	
	public String get(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
	}
	
	public void set(Context context, String val) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, val).commit();
	}

	public static void clearAll(Context context) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
	}
}
