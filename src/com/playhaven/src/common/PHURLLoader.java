package com.playhaven.src.common;

import java.nio.ByteBuffer;
import java.util.LinkedHashSet;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/** Represents a simple class that can download resources from a url. You should set the delegate to receive callbacks.
 * 
 *  This class is primarily used for open device urls (other apps on the device, market, web browser, etc).
 *  Thus, use this class when you're expecting to open a device url. Otherwise use {@link PHAsyncRequest}.
 *  
 *  TODO: perhaps examine maximum_redirects and total_redirects? 
 */
public class PHURLLoader implements PHAsyncRequest.PHAsyncRequestDelegate {
	
	public String targetURI;
	
	public boolean opensFinalURLOnDevice;
	
	public PHURLLoaderDelegate delegate;
	
	private PHAsyncRequest conn;
		
	/**simple state variable for checking to see if already processed response.*/
	private boolean alreadyFinished = false;
	
	private final int MAXIMUM_REDIRECTS = 10;
	
	private Context context;
	
	private JSONObject json_context;
	
	private static LinkedHashSet<PHURLLoader> allLoaders = new LinkedHashSet<PHURLLoader>();
	
	public static interface PHURLLoaderDelegate {
		public void loaderFinished(PHURLLoader loader);
		public void loaderFailed(PHURLLoader loader);
		public void redirectMarketURL(String url);
	}
	
	public PHURLLoader(Context context, PHURLLoaderDelegate delegate) {
		// since we are used primarily for opening other apps...
		opensFinalURLOnDevice = true;
		this.context = context;
		this.delegate = delegate;
	}
	public PHURLLoader(Context context) {
		this.context = context;
		// since we are used primarily for opening other apps...
		opensFinalURLOnDevice = true;
		delegate = null;
	}
	

	///////////////////////////////////////////////////////////
	
	public void setJSONContext(JSONObject context) {
		json_context = context;
	}

	public JSONObject getJSONContext() {
		return json_context;
	}
	
	///////////////////////////////////////////////////////////
	///////////////// Management Methods //////////////////////
	
	public static void invalidateLoaders(PHURLLoaderDelegate delegate) {
		for (PHURLLoader loader : allLoaders) {
			if (loader.delegate == delegate) {
				loader.invalidate();
			}
		}
	}
	
	public static void removeLoader(PHURLLoader loader) {
		allLoaders.remove(loader);
	}
	
	public static void addLoader(PHURLLoader loader) {
		allLoaders.add(loader);
	}
	public static void openDeviceURL(String url) {
		//TODO: open device url though not currently used by iOS SDK?
	}
	
	///////////////////////////////////////////////////////////
	public void open() {
		if(targetURI != null) {
			PHConstants.phLog(String.format("Opening url in PHURLLoader: %s", targetURI));
			
			alreadyFinished = false;
			
			conn = new PHAsyncRequest(this);
			conn.setMaxRedirects(MAXIMUM_REDIRECTS);
			conn.request_type = PHAsyncRequest.RequestType.Get;
			conn.execute(Uri.parse(targetURI));
			
			synchronized(this) {
				PHURLLoader.addLoader(this);
			}
		}
	}
	
	// handle status updates
	private void finish() {
		if(!alreadyFinished) {
			if(delegate != null)
				delegate.loaderFinished(this);
			
			alreadyFinished = true;
			
			targetURI = conn.getLastRedirect();
			if (targetURI == null) targetURI = "";
			
			PHConstants.phLog("Last redirect uri: "+targetURI);
			
			invalidate();
			
			// should we open the url on the device? (probably)
			// we make sure we actually have a uri to open..
			if(opensFinalURLOnDevice && targetURI != null && !targetURI.equals("")) {
				Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetURI));
				context.startActivity(openIntent);
			}
			
		}
	}
	
	
	public void invalidate() {
		delegate = null;
		
		synchronized (this) {
			conn.cancel(true);
			PHURLLoader.removeLoader(this);
		}
	}
	
	private void fail() {
		if(delegate != null)
			delegate.loaderFailed(this);
		
		invalidate();
	}
	
	////////////////////////////////////////////////////
	/////// PHAsyncRequest delegate methods ////////////
	
	public void requestFinished(ByteBuffer response) {
		// we can call this because it won't run if it's already been called.
		finish();
	}

	public void requestFailed(Exception e) {
		PHConstants.phLog("PHURLLoader failed with error: "+e);
		fail();
	}

	public void requestResponseCode(int responseCode) {
		if(responseCode < 300) {
			PHConstants.phLog("PHURLLoader finishing from initial url: "+targetURI);
			finish();
		} else {
			PHConstants.phLog("PHURLLoader failing from initial url: "+targetURI);
			fail();
		}
	}

	public void requestProgressUpdate(int progress) {
		// TODO ignore progress updates..
	}

	@Override
	public void redirectMarketURL(String url) {
		if(delegate != null)
			delegate.redirectMarketURL(url);
		
	}
}
