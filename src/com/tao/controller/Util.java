package com.tao.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {
	public static Map<String, String> splitQueryString(String param) {
		Map<String, String> queryPairs = new LinkedHashMap<String, String>();
		String[] pairs = param.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1));
		}
		return queryPairs;
	}
	public static boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}
}
