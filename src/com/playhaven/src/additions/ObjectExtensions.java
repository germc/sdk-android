package com.playhaven.src.additions;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.playhaven.src.common.PHStringUtil;

/** A simple class which has a bunch of static utility methods for working with Objects.
 * We have special nested classes for working with strings, dictionarys, and uris, etc.
 * We're trying to emulate the objective-c category functionality the best we can.
 * @author samuelstewart
 *
 */
public class ObjectExtensions {
	public static class JSONExtensions {
		/**utility function to suppress JSONExceptions if the key doesn't exist.
		 * Instead, it simply returns null and prints a stack trace if there's an error. 
		 */
		public static String getJSONString(JSONObject obj, String key) {
			if (obj == null) return "";
			
			String val = null;
			try {
				val = obj.getString(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return val;
		}
		/** Allows user to set a string value for a string key without having to handle any exceptions.
		 * If there is an error, it's logged to the console quietly.
		 */
		public static void setJSONString(JSONObject obj, String key, String value) {
			try {
				obj.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		/** Attempts to get a json array for the specified key. Returns null if it doesn't exist.*/
		public static JSONArray getJSONArray(JSONObject obj, String key) {
			JSONArray array = null;
			try {
				array = obj.getJSONArray(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return array;
		}
		/** Attempts to get the sub json object at the specified index. Returns null if it doesn't exist.*/
		public static JSONObject getJSONObject(JSONArray array, int index) {
			JSONObject obj = null;
			try {
				obj = array.getJSONObject(index);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;
		}
		/** Attempts to get the sub json object for the specified key. Returns null if it doesn't exist.*/
		public static JSONObject getJSONObject(JSONObject fromObj, String key) {
			JSONObject obj = null;
			try {
				obj = fromObj.getJSONObject(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;
		}
	}
	public static class StringExtensions {
		public static String decodeURL(String url) {
			return null;
		}
		public static String encodeURL(String url) {
			return null;
		}
		public static Dictionary<String, String> createQueryDict(String url) {
			String[] parts = url.split("&");
			Hashtable<String, String> queryComps = new Hashtable<String, String>();
			
			for(int i=0; i<parts.length; i++) {
				String[] kv = parts[i].split("=");
				if(kv.length < 2) continue;
				
				String key = PHStringUtil.urlDecode(kv[0]);
				String val = PHStringUtil.urlDecode(kv[1]);
				//TODO: handle multiple values per key (not currently necessary)
				queryComps.put(key, val);
			}
			return queryComps;
		}
		/** Grabs query component only from url. If no query component, returns null.*/
		public static String queryComponent(String url) {
			int queryStart = url.indexOf("?");
			if(queryStart != -1) {
				return url.substring(queryStart+1);
			}
			return null;
		}
		/** Returns the query components as a dictionary. (Added "s" differentiates from queryComponent.*/
		public static Dictionary<String, String> queryComponents(String url) {
			return createQueryDict(queryComponent(url));
		}
	}
	
	public static class DictionaryExtensions {
		/** Creates typical URL query for attaching to get request*/
		public static String createQuery(HashMap<String, String> dict) {
			StringBuilder query = new StringBuilder();
			boolean isFirst = true;
			
			
			Iterator<String> e = dict.keySet().iterator();
			while(e.hasNext()) {
				String key = e.next();
				key = PHStringUtil.urlEncode(key);
				
				//Note: little bit inefficient since we're doing a lookup, but OK for small size.
				String value = dict.get(key);
				//we do a weak encoding (not compliant) to work with the server.
				value = PHStringUtil.weakUrlEncode(value);
				
				if(isFirst) {
					query.append(String.format("%s=%s", key, value));
					isFirst = false;
				} else {
					query.append(String.format("&%s=%s", key, value));
				}
				
			}
			return query.toString();
		}
	}
	
	public static class UriExtensions {
		public static String createQueryDict(Uri uri) {
			return null;
		}
	}
	
}
