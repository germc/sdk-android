package com.playhaven.src.common;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;

import com.playhaven.src.publishersdk.content.PHContent;

/**
 * <h1>Summary</h1> 
 * Very simple class with static methods and variables for holding constants relevant to the SDK.
 * 
 * We also handle the PH_TOKEN and PH_SECRET key. It will throw an exception
 * if you call it without setting the keys.
 * 
 * We also use this class to grab the device information at startup. We can't find the device
 * info without a context so we need you to pass this in from an activity (at the start
 * of the application).
 * 
 *  Thus, you always need to do the following when using the SDK. 
 * 
 * 1. Call {@link PHConstants#setKeys(String, String)}
 * 2. Call {@link PHConstants#findDeviceInfo(Context)}
 * 
 * 
 * <h1>Environments</h1>
 * Due to testing requirements across a variety of platforms (such as Unity, the emulator, and devices), PHConstants 
 * uses a set of inner classes which act as different "Environments". Each environment contains custom settings for 
 * device information such as name, model, os, etc. In addition, each environment handles content template urls differently;
 * some redirecting to a local server, other to a dev server. Finally, each environment handles a different API path, allowing
 * us to test against development, staging, and finally production.
 * 
 * {@link PHConstants} simply acts a facade through which we can access the properties defined by the current environment. Since it is a static class,
 * it makes the access simpler.
 * 
 * To change environments (the default is production) you should call {@link PHConstants#setEnvironment(Environment)}
 * 
 * <h3>Class Hierarchy</h3>
 * The class hiearchy is quite simple. Each environment inherits from the default "production" environment with all settings defined.
 * Each subclass then overrides only the properties and methods it wishes to change for that environment. 
 * 
 * <h1>Debugging Info</h1>
 * We have a quiet inner class which contains methods that mirror much of the functionality in the "live" methods.
 * These are used for debugging; especially while the server API is being developed. It relies on the state of 
 * the contain {@see PHConstants} class.
 */

public class PHConstants {
	private static Environment environment = new Production();
	
	/////////////////////////////////////////////////////////
	/////////////// Testing Environments ////////////////////
	
	/**
	 * @class Environment
	 * @author samstewart
	 * Base class which each specific environment class extends.
	 */
	public static class Environment {
		
		private HashMap<Integer, Integer> idiom_mappings = new HashMap<Integer, Integer>();
		
		protected boolean useApiAuth;

		protected String PH_SERVER = "api2.playhaven.com";
		protected String PH_SERVER_URL = "http://"+PH_SERVER;

		protected String PH_MEDIA_SERVER = "media.playhaven.com";

		protected String PH_TOKEN = "your_token";
		
		protected String PH_SECRET = "your_secret";
		
		protected WeakReference<DisplayMetrics> displayMetrics;
		
		protected int densityType = DisplayMetrics.DENSITY_DEFAULT;
		
		protected float screenDensity = 0.0f;
		
		protected HashMap<String, String> device_info = new HashMap<String, String>();

		
		/** Caches all the device information in one large dictionary for later use.
		 * You must call this at least once with a context b/c we need one to get the device
		 * information. Every time you call this function, the cached values are refreshed. The reason we cache values in the first place
		 * is for efficiency as some of the calls are quite expensive. You should call this before each request to ensure we have access to the 
		 * "freshest".
		 * @param activity The activity from which to get the information (needs to be activity to ensure we can get available window size)
		 */
		public void findDeviceInfo(Activity context) {
			if (context == null) return;
			
			//TODO: connection parameter (cell or wifi)
			try {
				device_info.put("device_id", System.getString(context.getContentResolver(), System.ANDROID_ID));

				// info about app itself
				PackageManager pm = context.getPackageManager();
				PackageInfo pinfo = pm.getPackageInfo(context.getPackageName(), 0);

				device_info.put("app_id", pinfo.packageName);
				
				device_info.put("app_version", pinfo.versionName);

				// get the device model
				device_info.put("device_model", Build.MODEL);

				device_info.put("os_name", Build.VERSION.CODENAME);

				device_info.put("os_version", Integer.toString(Build.VERSION.SDK_INT));
				
				device_info.put("original_width", String.valueOf(context.getResources().getDisplayMetrics().widthPixels));
				
				device_info.put("original_height", String.valueOf(context.getResources().getDisplayMetrics().heightPixels));
				
				densityType = context.getResources().getDisplayMetrics().densityDpi;
				
				screenDensity = context.getResources().getDisplayMetrics().density;
				
				displayMetrics = new WeakReference<DisplayMetrics>(context.getResources().getDisplayMetrics());
				
				//TODO: get real device idiom and add it to the 'device_info' dictionary
				getIosDeviceIdiom(context);
				
			} catch(Exception e) {
				e.printStackTrace();
			}

		}
		
		@SuppressWarnings("unused")
		private int getConnectionType(Context context) {
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			
			switch (info.getType()) {
			case ConnectivityManager.TYPE_MOBILE:
				return 1;
			case ConnectivityManager.TYPE_WIFI:
				return 2;
			default:
				return 0;
			}
		}
		
		/** HACK, used for converting device idiom to iOS style*/
		private void getIosDeviceIdiom(Context context) {
			//TODO: currently converting between ANDROID and iOS, should change this in the future
			
			//create mappings between Android screen size and iOS UI_INTERFACE_IDIOM
			// 0 - iPhone
			// 1 - iPad
			Integer iphoneIdiom = new Integer(0);
			Integer ipadIdiom = new Integer(1);
			
			idiom_mappings.put(new Integer(Configuration.SCREENLAYOUT_SIZE_NORMAL), iphoneIdiom);
			idiom_mappings.put(new Integer(Configuration.SCREENLAYOUT_SIZE_SMALL), iphoneIdiom);
			idiom_mappings.put(new Integer(Configuration.SCREENLAYOUT_SIZE_LARGE), ipadIdiom);
			idiom_mappings.put(new Integer(Configuration.SCREENLAYOUT_SIZE_UNDEFINED), ipadIdiom);
			
			int screenType = context.getResources().getConfiguration().screenLayout;
			Integer typeKey = new Integer(screenType & Configuration.SCREENLAYOUT_SIZE_MASK);
			
			Integer iosType = (idiom_mappings.get(typeKey) != null ? idiom_mappings.get(typeKey) : ipadIdiom); // default to ipad idiom if no entry exists
			
			Log.i("Playhaven Debug", "Android type: "+typeKey+" to iOS type: "+iosType);
			
			device_info.put("device_idiom", iosType.toString());
		}
		
		public boolean shouldCacheWebView() {
			return true;
		}
		
		public boolean useStrictMode() {
			return true;
		}
		public float dipToPixels(float dip) {
			return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics.get());
		}
		
		protected String getCachedValue(String key) {
			if (device_info.containsKey(key)) return device_info.get(key);
			
			throw new UnsupportedOperationException("You must call 'PHConstants.findDeviceInfo' because '"+key+"' does not exist. \n" +
													"You should call 'PHConstants.findDeviceInfo' before *every* PHAPIRequest call");
		}
		
		private boolean didSetKeys() {
			return !(PH_TOKEN.equalsIgnoreCase("your_token") || PH_SECRET.equalsIgnoreCase("your_secret"));
		}
		
		protected boolean didSetDeviceInfo() {
			// should contain no null values and should have at least something set
			return (device_info.size() > 0 && device_info.containsValue(null));
		}
		
		public String getAPIUrl() {
			return PH_SERVER_URL;
		}

		public void setAPIUrl(String url) {
            PH_SERVER_URL = url;
		}

		public String getAPIServerAddress() {
			return PH_SERVER;
		}

		public String getMediaServerAddress() {
			return PH_MEDIA_SERVER;
		}

		public String phURL(String path) {
			if(didSetKeys())
				return getAPIUrl()+path;
			else
				throw new UnsupportedOperationException("You must set the secret and developer keys.");
		}
		
		public String phFormatUrl(String query) {
			//TODO: possibly accept format string with args?
			return null;
		}
		
		public void phLog(String message) {
			Log.i(String.format("[Playhaven-%s]", getSDKVersion()), message);
		}
		
		//////////////////////////////////////////////////////////////////
		/////////////// Credentials Management ///////////////////////////
		
		public void setKeys(String token, String secret) {
			PH_TOKEN = token;
			PH_SECRET = secret;
		}
		
		public String getToken() {
			if(didSetKeys())
				return PH_TOKEN;
			else
				throw new UnsupportedOperationException("You must set the token");
		}
		
		public String getSecret() {
			if(didSetKeys())
				return PH_SECRET;
			else
				throw new UnsupportedOperationException("You must set the secret key");
		}
		
		//////////////////////////////////////////////////////////////////
		/////////////////////// API Auth /////////////////////////////////
		
		public boolean useBasicAuth() {
			return false;
		}
		
		public String getBasicAuthUser() {
			return null;
		}
		
		public String getBasicAuthPass() {
			return null;
		}
		
		//////////////////////////////////////////////////////////////////
		/////////////// SDK Config (like Macros) /////////////////////////
		
		public boolean shouldRecycleContentViews() {
			return true;
		}
		
		public String getSDKVersion() {	
			return "1.10.2";
		}
		
		public int getProtocolVersion() {
			return 3;
		}
		
		/////////////////////////////////////////////////////////////////
		/////////////////////// Screen Information //////////////////////
		
		public int getDeviceOrientation(Context context) {
			return context.getResources().getConfiguration().orientation;
		}
		
		public int getScreenWidth(Context context) {
			return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		}
		
		public int getScreenHeight(Context context) {
			return  ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
		}
		
		public int getOriginalWidth() {
			
			return Integer.parseInt(getCachedValue("original_width"));
		}
		
		public int getOriginalHeight() {
			return Integer.parseInt(getCachedValue("original_height"));
		}
		
		public int getAvailableHeight(Activity parent) {
			Rect windowFrame = new Rect();
			
			// requires IPC call so a bit performance intensive
			parent.getWindow().getDecorView().getWindowVisibleDisplayFrame(windowFrame);
			return windowFrame.height();
		}
		
		public int getAvailableWidth(Activity parent) {

			Rect windowFrame = new Rect();
			
			// requires IPC call so a bit performance intensive
			parent.getWindow().getDecorView().getWindowVisibleDisplayFrame(windowFrame);
			return windowFrame.width();
		}
		
		public int getTopBarHeight(Context context) {
			return -1; // TODO: get top bar height
		}
		
		/////////////////////////////////////////////////////////////////
		/////////////////////// Device Info /////////////////////////////
		
		public boolean isOnline(Context context) {
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		}
		
		public int getOriginalConnectionType() {
			return Integer.parseInt(getCachedValue("connection"));
		}
		
		public String getUniqueID() {
			return getCachedValue("device_id");
		}
		
		public String getDeviceIdiom() {
			return getCachedValue("device_idiom");
		}
		
		public int getScreenDensityType() {
			return densityType; // actual density class
		}
		
		public float getScreenDensity() {
			return screenDensity; // actual dpi of density (nont density class)
		}
		
		public String getAppVersion() {
			return getCachedValue("app_version");
		}
		
		public String getAppID() {
			return getCachedValue("app_id");
		}
		
		public String getDeviceModel() {
			return getCachedValue("device_model");
		}
		
		public String getOSName() {
			return getCachedValue("os_name");
		}
		
		public String getOSVersion() {
			return getCachedValue("os_version");
		}
		
		
		/////////////////////////////////////////////////////////////////
		////////////// Connection Management ////////////////////////////
		
		public Uri getTemplateURL(PHContent content) {
			return content.url; //simply pass-through
		}
	}
	
	/**
	 * @class Production 
	 * Empty class which is (currently) used mostly for name only.
	 * Since {@see Environment} contains all the production code by default,
	 * there is no need for overriding.
	 * @author samstewart
	 *
	 */
	public static class Production extends Environment {
		//TODO: override for more specific needs
	}
	
	/**
	 * @class Development
	 * Development environment for testing and you can force a specific template url by calling {@link Development#forceExplicitURLType}.
	 * You can use a local testing url (on LAN) by calling {@link Development#useLocalTemplates()} {@link Development#useRemoteTemplates()}.
	 * 
	 * In future versions, you'll be able to set a custom template git "treeish" to use "staging" templates on the remote server. Currently, we do not bother
	 * supporting this as it isn't necessary.
	 * 
	 * Also in future versions, we would like to be able to force arbitrary template files (such as gow, more_games, etc) regardless of what the API
	 * returns.
	 * 
	 * We would like to be able to control the Basic-Auth cred as well.
	 * @author samstewart
	 *
	 */
	public static class Development extends Environment {
		
		private String devTemplateId = "e690da767f7487e82a072446843e675e97acc5d1";
		
		private String api_url;
		
		private String basic_username;
		
		private String basic_password;
		
		private HashMap<String, String> credentials = new HashMap<String, String>();
		
		private final String PH_TESTING_MACHINE = "";
		
		public static enum URLType {
			Android,
			iPhone,
			iPad,
			KindleFire,
			Passthrough // simply use original url
		}
		
		private URLType url_type = URLType.Passthrough;
		
		private String explicit_template = null;
		
		private boolean useLocalTemplates = false;
		
		/*
		 * Prefetch and caching constants
		 */
		public static boolean USE_PREFETCHING = true;
		public static int MAX_CACHE_SIZE = 1024 * 1024 * 8;
		public static String APP_CACHE_PATH = "/data/data/com.playhaven.androidsdk/cache";
		public static String PLAYHAVEN_PREFETCH_CACHE_PATH = "/playhaven_cache/";

		/** Converts the url to a url which references a local template on a testing machine.
		 * @param url
		 * @return
		 */
		private Uri getLocalTemplateURL(Uri url){
			String debug_url = url.toString();
			
			//strip compressed ending
			debug_url = debug_url.replace(".gz", "");
			
			//start at html directory (include the prefix /)
			int htmlDir = debug_url.indexOf("/html/");
			if (htmlDir != -1) {
				//grab everything (including /html)
				debug_url = debug_url.substring(htmlDir);
			}
			
			return Uri.parse(PH_TESTING_MACHINE+debug_url); //no need for intermediate '/' since it prefixes debug_url
		}
		
		//////////////////////////////////////////////////////////////////
		/////////////////////// API Auth /////////////////////////////////

		@Override
		public boolean useBasicAuth() {
			return useApiAuth;
		}
		
		public void setUseBasicAuth(boolean use_auth) {
			this.useApiAuth = use_auth;
		}
		
		@Override
		public String getAPIUrl() {
			if (api_url == null || api_url.equals("")) // rely on short ciruit
				api_url = super.getAPIUrl();
				
	
			return api_url;
		}
		
		public void setAPIUrl(String api_url) {
			this.api_url = api_url;
		}

		public String getAPIServerAddress() {
			return PH_SERVER;
		}

		public String getMediaServerAddress() {
			return PH_MEDIA_SERVER;
		}

		@Override
		public String getBasicAuthUser() {
			return this.basic_username;
		}

		public void setBasicAuthPass(String pass) {
			this.basic_password = pass;
		}
		
		public void setBasicAuthUser(String user) {
			this.basic_username = user;
		}
		
		@Override
		public String getBasicAuthPass() {
			return this.basic_password;
		}

		/////////////////////////////////////////////////////////
		///////////// Template URL Conversions //////////////////
		
		/** Gets the template file name without the extension*/
		private String getTemplateBasename(Uri url) {
			String urlStr = url.toString();
			
			//find filename
			int file_ext = urlStr.indexOf(".html");
			int slash = urlStr.lastIndexOf('/');
			
			if (slash < file_ext) {
				//grab the intermediary file name
				urlStr = urlStr.substring(slash+1, file_ext);
			}
			
			return urlStr;
		}
		
		/** Replaces the filename of a url with the same name but a different suffix.
		 * Mainly used in the url conversion methods.
		 * TODO: replace the git treeish with the staging one specified
		 */
		private Uri addBasenameSuffix(Uri url, String suffix) {
			String filename = getTemplateBasename(url);
			String urlStr = url.toString();
			
			String new_filename = filename.replaceAll("_ipad|_iphone|_android|_ios", ""); //strip out other suffixes
			
			// should we be using a different placement/template altogether?
			if (explicit_template != null)
				new_filename = explicit_template;
			
			//remove old file name and replace with new appropriate suffix
			urlStr = urlStr.replace(filename, new_filename+suffix);
			
			return Uri.parse(urlStr);
		}
		
		/** Returns the android content template url given a content url.*/
		private Uri getAndroidTemplate(Uri url) {
			url = addBasenameSuffix(url, "_android");
			return (useLocalTemplates ? getLocalTemplateURL(url) : url);
			
		}
		
		/** Returns the iPad content template url given a content url.*/
		private Uri getiPadTemplate(Uri url) {
			url = addBasenameSuffix(url, "_ipad");
			return (useLocalTemplates ? getLocalTemplateURL(url) : url);
		}
		
		/** Returns the iPhone content template url given a content url.
		 *  (could be iPad url, android url, etc. passed in)
		 */
		private Uri getiPhoneTemplate(Uri url) {
			url = addBasenameSuffix(url, "");
			return (useLocalTemplates ? getLocalTemplateURL(url) : url); // no suffix at all, just make sure other suffixes were stripped
		}
		
		/** Modify template url to point to the staging template version (on the server - not local).
		 * @deprecated should be moved to own Staging subclass
		*/
		@SuppressWarnings("unused")
		private Uri getStagingTemplateURL(Uri prod_url) {
			String dev_url = prod_url.toString().replaceAll("\\/\\w{40}\\/", "/"+devTemplateId+"/");
			return Uri.parse(dev_url);
		}
		
		/** Gets the original template url but will modify it if testing over LAN.*/
		private Uri getPassthroughTemplate(Uri url) {
			return (useLocalTemplates ? getLocalTemplateURL(url) : url); // if not local  url, siimply pass through remote url
		}
		///////////////////////////////////////////////////////
		
		public void forceExplicitURL(URLType url_type) {
			this.url_type = url_type;
		}
		
		public void forceExplicitTemplate(String template) {
			this.explicit_template = template.replace(".html", "");
		}
		
		public void useLocalTemplate() {
			this.useLocalTemplates = true;
			
		}
		
		@Override
		public boolean shouldCacheWebView() {
			return false;
		}
		
		public void useRemoteTemplate() {
			this.useLocalTemplates = false;
			
		}
		
		@Override
		public Uri getTemplateURL(PHContent content) {
			switch (url_type) {
			case Android:
				return getAndroidTemplate(content.url);
			case iPhone:
				return getiPhoneTemplate(content.url);
			case iPad:
				return getiPadTemplate(content.url);
			case Passthrough:
				return getPassthroughTemplate(content.url); //only change from local/remote testing, no suffix change
			}
			
			return content.url; //no type? unchanged..
		}
		
		/** Create fake device information*/
		@Override
		public void findDeviceInfo(Activity context) {
			super.findDeviceInfo(context); 
			
			//Note: no longer needed since server accepts android...
			//device_info.put("device_id", "9223ED0B-7FC0-54C3-A7A5-387A7496922B");
			
			
			//create login credentials for server..
			credentials.put("staging_username", "betahaven");
			credentials.put("staging_password", "smirnoff1c3");
			
		}
		 
	}
	
	/////////////////////////////////////////////////////////
	/////////////// Environment Handling ////////////////////
	
	public static void setEnvironment(Environment environ) {
		PHConstants.environment = environ;
	}
	
	public static Environment getEnvironment() {
		return environment; 
	}
	
	//////////////////////////////////////////////////////////
	
	public static void setKeys(String token, String secret) {
		environment.setKeys(token, secret);
	}
	
	public static void setAPIUrl(String url) {
		environment.setAPIUrl(url);
	}
	
	public static String getAPIUrl() {
		return environment.getAPIUrl();
	}

	public static String getAPIServerAddress() {
		return environment.getAPIServerAddress();
	}

	public static String getMediaServerAddress() {
		return environment.getMediaServerAddress();
	}

	public static String getToken() {
		return environment.getToken();
	}
	
	public static String getSecret() {
		return environment.getSecret();
	}
	
	public static void findDeviceInfo(Activity context) {
		environment.findDeviceInfo(context);
	}
	
	public static float dipToPixels(float dip) {
		return environment.dipToPixels(dip);
	}
	
	@SuppressWarnings("unused")
	private static boolean didSetKeys() {
		return environment.didSetKeys();
	}
	
	public static boolean useBasicAuth() {
		return environment.useBasicAuth();
	}
	
	public static String getBasicAuthUser() {
		return environment.getBasicAuthUser();
	}
	
	public static String getBasicAuthPass() {
		return environment.getBasicAuthPass();
	}
	
	public static String phURL(String path) {
		return environment.phURL(path);
	}
	
	public static String phFormatUrl(String query) {
		return environment.phFormatUrl(query);
	}
	
	public static void phLog(String message) {
		environment.phLog(message);
	}
	
	//////////////////////////////////////////////////////////////////
	
	public static Uri getTemplateURL(PHContent content) {
		return environment.getTemplateURL(content);
	}
	
	//////////////////////////////////////////////////////////////////
	////////////////// Pass-through Methods///////////////////////////

	public static boolean shouldCacheWebView() {
		return environment.shouldCacheWebView();
	}
	public static boolean isOnline(Context context) {
		return environment.isOnline(context);
	}
	
	public static int getOriginalConnectionType() {
		return environment.getOriginalConnectionType();
	}
	public static int getDeviceOrientation(Context context) {
		return environment.getDeviceOrientation(context);
	}

	public static int getOriginalWidth() {
		return environment.getOriginalWidth();
	}
	
	public static int getOriginalHeight() {
		return environment.getOriginalHeight();
	}
	public static int getScreenWidth(Context context) {
		return environment.getScreenWidth(context);
	}

	public static int getScreenHeight(Context context) {
		return environment.getScreenHeight(context);
	}

	public static int getAvailableHeight(Activity parent) {
		return environment.getAvailableHeight(parent);
	}

	public static int getAvailableWidth(Activity parent) {
		return environment.getAvailableWidth(parent);
		
	}
	public static int getTopBarHeight(Context context) {
		return environment.getTopBarHeight(context);
	}

	public static boolean shouldRecycleContentViews() {
		return environment.shouldRecycleContentViews();
	}
	
	public static String getSDKVersion() {	
		return environment.getSDKVersion();
	}

	public static int getProtocolVersion() {
		return environment.getProtocolVersion();
	}
	
	public static String getUniqueID() {
		return environment.getUniqueID();
	}
	
	public static String getDeviceIdiom() {
		return environment.getDeviceIdiom();
	}
	public static int getScreenDensityType() {
		return environment.getScreenDensityType(); // returns the density class (unlike getScreenDensity)
	}
	
	public static float getScreenDensity() {
		return environment.getScreenDensity(); // gets actual pixel density (in dpi) not density class
	}
	public static String getAppVersion() {
		return environment.getAppVersion();
	}
	
	public static String getAppID() {
		return environment.getAppID();
	}
	
	public static String getDeviceModel() {
		return environment.getDeviceModel();
	}
	
	public static String getOSName() {
		return environment.getOSName();
	}
	
	public static String getOSVersion() {
		return environment.getOSVersion();
	}
}
