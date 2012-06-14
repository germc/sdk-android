package com.playhaven.src.publishersdk.content;

import java.util.Hashtable;

import android.net.Uri;

/** Lightweight object which handles routing urls from the webview
 * callbacks. It maintains a list of runnables it should call with various parameters.
 * The runnables can obtain information about the current state of the router
 * such as additional request parameters, etc. It works particularly well with
 * anonymous Runnable classes.
 * 
 * You should call {@link PHRequestRouter#addRoute(String, Runnable)} to add a new route. The route
 * you add should only match the base url portion without query variables. In the future we may add
 * regex matching but that is unnecessary for now. 
 * @author samstewart
 *
 */
public class PHRequestRouter {
	
	private static Uri mCurUrl;
		
	Hashtable<String, Runnable> mRoutes = new Hashtable<String, Runnable>();
	
	public static String getCurrentURL() {
		synchronized (PHRequestRouter.class) {
			return (mCurUrl != null 				? 
					mCurUrl.toString(): 
					null);
		}
	}
	
	public static String getCurrentQueryVar(String name) {
		synchronized (PHRequestRouter.class) {
			
			
			return (mCurUrl != null 				? 
					mCurUrl.getQueryParameter(name) : 
					null);
		}
	}
	
	public void addRoute(String route, Runnable callback) {
		mRoutes.put(route, callback);
	}
	
	private String stripQuery(String url) {
		return url.substring(0, (url.indexOf("?") > 0 ? 
				 				 url.indexOf("?")     : 
				 				 url.length())
							);
	}
	public boolean hasRoute(String url) {
		return mRoutes.containsKey(stripQuery(url));
	}
	
	/**
	 * Actually parses and "routes" the given url. 
	 * @param url Must be a valid URL and can contain query variables. 
	 */
	public void route(String url) {
		synchronized (this) {
			mCurUrl = Uri.parse(url);
			
			if (mCurUrl == null) return;
			
			String stripped = stripQuery(url);
			
			Runnable route = mRoutes.get(stripped);
			
			if (route != null) route.run();
		}

	}
	
	public void clearRoutes() {
		mRoutes.clear();
	}
}
