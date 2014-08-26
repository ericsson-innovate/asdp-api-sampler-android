package com.idean.atthack.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.idean.atthack.api.ApiSpecRaw.ReqParam;

import android.text.TextUtils;
import android.util.Log;

public class ApiSpec {
	private ApiSpecRaw mRaw;
	
	// Facade over composed ApiSpecRaw
	public String name;
	public String[] categories;
	public String id;
	public String description;
	public String docNumber;
	
	public String route() {
		if (mRaw.resourceTable != null) {
			return mRaw.resourceTable.route;
		}
		return null;
	}
	
	private static final String REGEX_CURLY = "\\{(\\w*)\\}";

	private static final String TAG = "ApiSpec";


	/**
	 * 
	 * @return List of params embedded in the URL route
	 */
	public List<String> routeParams() {
		String route= route();
		if (TextUtils.isEmpty(route)) {
			return new ArrayList<String>();
		}

		Pattern PATTERN_CURL = Pattern.compile(REGEX_CURLY);
		Matcher m = PATTERN_CURL.matcher(route);
		List<String> res = new ArrayList<String>();
		while (m.find()) {
			String next = m.group(1);
			Log.d(TAG,"Found next " + next);
			if (!TextUtils.isEmpty(next)) {
				res.add(next);
			}
		}
		Log.d(TAG,"Extracting route params from " + route + " and got " + res);
		return res;
	}
	
	public HttpVerb verb() {
		if (mRaw.resourceTable != null
				&& mRaw.resourceTable.verbs != null
				&& mRaw.resourceTable.verbs.length > 0) {
			String verb = mRaw.resourceTable.verbs[0];
			try {
				// should be either POST or GET
				return HttpVerb.valueOf(verb);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
	
	public ReqParam[] requestParams() {
		if (mRaw.parameters != null
				&& mRaw.parameters.requestBody != null
				&& mRaw.parameters.requestBody.length > 0) {
			return mRaw.parameters.requestBody;
		}
		return null;
	}
	
	public enum HttpVerb {
		POST,
		GET
	}
	
	public ApiSpec(ApiSpecRaw raw) {
		mRaw = raw;
		name= mRaw.name;
		categories = mRaw.categories;
		id = mRaw.id;
		description = mRaw.description;
		docNumber = mRaw.docNumber;
	}
	
	public boolean isValid() {
		return !TextUtils.isEmpty(name) 
				&& !TextUtils.isEmpty(id) 
				&& !TextUtils.isEmpty(description) 
				&& !TextUtils.isEmpty(docNumber) 
				&& categories != null && categories.length > 0;
	}
}