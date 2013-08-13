package com.goodow.drive.android.toolutils;

import org.json.JSONArray;
import org.json.JSONObject;
import android.text.TextUtils;

public final class JSONTools {
	private JSONTools() {
		
	}
	
	public static boolean isEmpty(final JSONObject jsonObject, final String key) throws Exception {
		if (null == jsonObject || TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("jsonObject or key is null!");
		}
		
		return (!jsonObject.has(key) || jsonObject.isNull(key));
	}
	
	public static String safeParseJSONObjectForValueIsString(final JSONObject jsonObject, final String key, final String defaultValue) throws Exception {
		if (null == jsonObject || TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("jsonObject or key is null!");
		}
		
		String value = defaultValue;
		if (jsonObject.has(key) && !jsonObject.isNull(key)) {
			value = jsonObject.optString(key, defaultValue);
		}
		return value;
	}
	
	public static int safeParseJSONObjectForValueIsInteger(final JSONObject jsonObject, final String key, final int defaultValue) throws Exception {
		if (null == jsonObject || TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("jsonObject or key is null!");
		}
		
		int value = defaultValue;
		if (jsonObject.has(key) && !jsonObject.isNull(key)) {
			value = jsonObject.optInt(key, defaultValue);
		}
		return value;
	}
	
	public static double safeParseJSONObjectForValueIsDouble(final JSONObject jsonObject, final String key, final double defaultValue) throws Exception {
		if (null == jsonObject || TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("jsonObject or key is null!");
		}
		
		double value = defaultValue;
		if (jsonObject.has(key) && !jsonObject.isNull(key)) {
			value = jsonObject.optDouble(key, defaultValue);
		}
		return value;
	}
	
	public static boolean safeParseJSONObjectForValueIsBoolean(final JSONObject jsonObject, final String key, final boolean defaultValue) throws Exception {
		if (null == jsonObject || TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("jsonObject or key is null!");
		}
		
		boolean value = defaultValue;
		if (jsonObject.has(key) && !jsonObject.isNull(key)) {
			value = jsonObject.optBoolean(key, defaultValue);
		}
		return value;
	}
	
	public static long safeParseJSONObjectForValueIsLong(final JSONObject jsonObject, final String key, final long defaultValue) throws Exception {
		if (null == jsonObject || TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("jsonObject or key is null!");
		}
		
		long value = defaultValue;
		if (jsonObject.has(key) && !jsonObject.isNull(key)) {
			value = jsonObject.optLong(key, defaultValue);
		}
		return value;
	}
	
	public static JSONArray safeParseJSONObjectForValueIsJSONArray(final JSONObject jsonObject, final String key) throws Exception {
		if (null == jsonObject || TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("jsonObject or key is null!");
		}
		
		JSONArray value = null;
		if (jsonObject.has(key) && !jsonObject.isNull(key)) {
			value = jsonObject.optJSONArray(key);
		}
		return value;
	}
	
	public static JSONObject safeParseJSONObjectForValueIsJSONObject(final JSONObject jsonObject, final String key) throws Exception {
		if (null == jsonObject || TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("jsonObject or key is null!");
		}
		
		JSONObject value = null;
		if (jsonObject.has(key) && !jsonObject.isNull(key)) {
			value = jsonObject.optJSONObject(key);
		}
		return value;
	}
}
