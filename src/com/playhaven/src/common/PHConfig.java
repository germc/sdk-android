package com.playhaven.src.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Settings.System;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;

/** Lightweight class for managing configuration details of the SDK. 
 * You should call {@link PHConfig#cacheDeviceInfo} to cache information about the device from a given context. 
 * We do *not* want to hold onto the context as this creates all sorts of leaks and null-pointer badness.
 * To ensure the content is up-to-date, you should call this before
 * every content request. We avoid getters and setters to keep things lightweight. */
public class PHConfig {
	/////////////////////////////////////////////
	///////// Configuration Variables ///////////
	/// (Try not to set default values here) ////
	
	public static String sdk_version = "";
	
	public static String token		 = "";
	
	public static String secret		 = "";
		
	public static String os_name	 = "";
	
	public static int	 os_version;
	
	public static String device_id   = "";
	
	public static String device_model= "";
	
	public static String api		 = "";
	
	public static float  screen_density;
	
	public static int 	 protocol   = -1;
	
	public static int    screen_density_type;
	
	public static int    screen_size_type;
	
	public static int 	 device_size;
	
	public static Rect 	 screen_size           = new Rect(0, 0, 0, 0);
	
	public static Rect	 available_screen_size = new Rect(0, 0, 0, 0);
	
	public static String urgency_level = "";
	
	public static String app_package   = "";
	
	public static String app_version   = "";
	
	public static boolean cache;
	
	public static int	  cache_size = 1024 * 1024 * 8;
	
	/** Caches the live device info but anything in JSON_CONFIG
	 * overrides it.
	 * @param context
	 */
	public static void cacheDeviceInfo(Context context) {
		if (context == null) throw new IllegalArgumentException("Must supply a valid Context when extracting device info");
		
		try {
			// set all values from context
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			
			app_package 		= pinfo.packageName;
			
			app_version 		= pinfo.versionName;
			
			os_name 			= Build.VERSION.RELEASE;
			
			os_version 			= Build.VERSION.SDK_INT;
			
			DisplayMetrics dm 	= context.getResources().getDisplayMetrics();
			
			screen_size 		= new Rect(0, 0, dm.widthPixels, dm.heightPixels);
			
			screen_density_type = dm.densityDpi;
			
			screen_density 		= dm.density;
			
			device_size 		= context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
			
			device_id 			= System.getString(context.getContentResolver(), System.ANDROID_ID);
			
			device_model 		= Build.MODEL;
			
		} catch (Exception e) {
			PHCrashReport.reportCrash(e, PHCrashReport.Urgency.low);
		}
		
	}
	
	// environment for testing (defaults to prod)
	public static String environment = "prod";
	
	// string blob of JSON which we can change from build scripts  (see ant filtertask)
	public static final String JSON_CONFIG = 
						"{\n" + 
						"   \"prod\":{\n" + 
						"      \"sdk_version\":\"1.10.3\",\n" + 
						"      \"api\":\"http://api2.playhaven.com\",\n" + 
						"      \"cache\":false,\n" + 
						"      \"protocol\":4,\n" + 
						"      \"urgency_level\":\"low\"\n" + 
						"   },\n" + 
						"   \"dev\":null\n" + 
						"}";
	
	static {
		loadConfig(JSON_CONFIG, environment);
	}
	
	/** 
	 * Loads the configuration from a JSON blob. This method
	 * allows us to load from files and change via build settings.
	 * Uses java reflection to set the properties. Will overwrite the keys
	 * set by cacheDeviceInfo.
	 * 
	 *  @param jsonConfig The JSON configuration dictionary blob
	 *  @param environment The configuration environment. If null is passed we use 'prod'
	 */
	public static void loadConfig(String jsonConfig, String environment) {
		try {
			JSONObject environments = new JSONObject(jsonConfig);
			
			JSONObject config = environments.optJSONObject(environment == null ? "prod" : environment);
			
 			if ( ! JSONObject.NULL.equals(config)) {
 				
 				String key = null;
 				Iterator keys = config.keys();
 				
 				// try to set public variable to json values
 				while (keys.hasNext()) {
 					key = (String)keys.next();
 					
 					try {
 						Field field = PHConfig.class.getField(key);
 						field.set(key, config.opt(key)); // update the field value from the JSON blob *hopes types match*
 						
 					} catch (NoSuchFieldException e) {
 						// pass (continue)
 						e.printStackTrace();
 					} catch (IllegalArgumentException e) {
						// pass
 						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// pass
						e.printStackTrace();
					} catch (Exception e) {
						// finally swallow all exceptions
						e.printStackTrace();
					}
 					
 					
 				}
 			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
}
