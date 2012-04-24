package com.playhaven.src.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.playhaven.src.additions.ObjectExtensions;

/** Represents a single api request to the server*/
public class PHAPIRequest implements PHAsyncRequest.PHAsyncRequestDelegate {
	
	public PHAPIRequestDelegate delegate;
	
	private PHAsyncRequest conn;
	
	public String token;
	
	public String secret;
	
	private HashMap<String, String> signedParams;
	
	private HashMap<String, String> additionalParams;
	
	private String urlPath;
	
	private int hashCode;
	
	private static ArrayList<PHAPIRequest> allRequests;
	
	// protected so subclasses can access it. You shouldn't.
	protected String fullUrl;
	
	/** The delegate interface all delegates must implement*/
	public static interface PHAPIRequestDelegate {
		public void requestSucceeded(PHAPIRequest request, JSONObject responseData);
		public void requestFailed(PHAPIRequest request, Exception e);
	}
	
	/** constructs without delegate*/
	public PHAPIRequest(String token, String secret) {
		this.token = token;
		this.secret = secret;

	}

	/** constructs with delegate*/
	public PHAPIRequest(PHAPIRequestDelegate delegate) {
		this(delegate, PHConstants.getToken(), PHConstants.getSecret());
	}
	
	
	/** Construct with delegate and private/public key.*/
	public PHAPIRequest(PHAPIRequestDelegate delegate, String token, String secret) {
		this.delegate = delegate;
		
		this.token = token;
		this.secret = secret;
		
		addRequest(this);
	
	}
	///////////////////////////////////////////////
	public void setDelegate(PHAPIRequestDelegate delegate) {
		this.delegate = delegate;

	}

	///////////////////////////////////////////////
	
	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}
	
	public int getHashCode() {
		return this.hashCode;
	}

	
	///////////////////////////////////////////////////
	
	/** Cancels all requests which have the specified delegate. */
	public static void cancelRequests(PHAPIRequestDelegate delegate) {
		for (PHAPIRequest request : PHAPIRequest.getAllRequests()) {
			if (request.delegate == delegate) {
				request.cancel();
			}
			
		}
	}
	
	public static ArrayList<PHAPIRequest> getAllRequests() {
		if (allRequests == null) {
			// On first request only do a DNS lookup to speed up future requests
	        PHConstants.Environment environ = new PHConstants.Environment();
			try {
				InetAddress.getByName(environ.getAPIServerAddress());
				InetAddress.getByName(environ.getMediaServerAddress());

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				PHConstants.phLog("UnknowHostException: "+e.getMessage());

			}
			allRequests = new ArrayList<PHAPIRequest>();
		}
		
		return allRequests;
	}
	
	public static void addRequest(PHAPIRequest request) {
		PHAPIRequest.getAllRequests().add(request);
	}
	
	public static void removeRequest(PHAPIRequest request) {
		PHAPIRequest.getAllRequests().remove(request);
	}
	
	///////////////////////////////////////////////
	
	
	/** Produces dictionary with signed parameters and appends the additional parameters.*/
	public HashMap<String, String> signedParams() {
		if(signedParams == null) {
			String device, nonce, sigHash, sig, appId, appVersion, hardware, 
			os, idiom, sdk_version, width, height, sdk_platform, orientation,
			screen_density;
			
			//os = "iPhone+OS+4.3";
			//hardware = "i386";
			//appId = "com.playhaven.Example";
			device = PHConstants.getUniqueID();
			idiom = PHConstants.getDeviceIdiom();
			orientation = "0"; // TODO: use actual live orientation
			
			// make sure we generate the device id before doing the sighash!
			//device = PHConstants.getUniqueID();
			nonce = PHAPIRequest.base64Sig(PHStringUtil.getUUID());
			sigHash = String.format("%s:%s:%s:%s", this.token, device, nonce, this.secret);
			sig = PHAPIRequest.base64Sig(sigHash);
			appId = PHConstants.getAppID();
			appVersion = PHConstants.getAppVersion();
			hardware = PHConstants.getDeviceModel();
			os = String.format("%s %s", PHConstants.getOSName(), PHConstants.getOSVersion());
			sdk_version = PHConstants.getAppVersion();
			sdk_platform = "android";
			width = String.valueOf(PHConstants.getOriginalWidth());
			height = String.valueOf(PHConstants.getOriginalHeight());
			screen_density = String.valueOf(PHConstants.getScreenDensity()); 
			
			//connection = String.valueOf(PHConstants.getOriginalConnectionType());
			
			// decide if we add to existing params.
			HashMap<String, String> add_params = (getAdditionalParams() != null ? new HashMap<String, String>(getAdditionalParams()) : new HashMap<String, String>());
			
			signedParams = new HashMap<String, String>();
			signedParams.put("device", device);
			
			signedParams.put("token", this.token);
			signedParams.put("signature", sig);
			
			signedParams.put("nonce", nonce);
			signedParams.put("app", appId);
			
			signedParams.put("app_version", appVersion);
			signedParams.put("hardware", hardware);
			
			signedParams.put("os", os);
			signedParams.put("idiom", idiom);
			
			signedParams.put("width", width);
			signedParams.put("height", height);
			
			signedParams.put("sdk_version", sdk_version);
			signedParams.put("sdk_platform", sdk_platform);
			
			signedParams.put("orientation", orientation);
			signedParams.put("dpi", screen_density);
			
			signedParams.put("languages", Locale.getDefault().getDisplayLanguage());

			//signedParams.put("connection", connection);
			
			add_params.putAll(signedParams);
			signedParams = add_params;
		}
		
		PHConstants.phLog("Signed params: "+signedParams);
		return signedParams;
	}
	
	/** Gets the additional parameters. Declared as an accessor so it can be overrode*/
	public HashMap<String, String> getAdditionalParams() {
		return additionalParams;
	}
	
	/** Produces a query string with the signed parameters attached.*/
	public String signedParamsStr() {
		return ObjectExtensions.DictionaryExtensions.createQuery(signedParams());
	}
	
	public void send() {
		conn = new PHAsyncRequest(this);
		
		// set auth if testing against private api
		if (PHConstants.useBasicAuth()) {
			conn.setUsername(PHConstants.getBasicAuthUser());
			conn.setPassword(PHConstants.getBasicAuthPass());
		}
		
		conn.request_type = PHAsyncRequest.RequestType.Get;
		PHConstants.phLog("URL: "+URL());
		conn.execute(Uri.parse(URL()));
	}
	
	/** Should only be overridden and not called directly from external class.*/
	protected void finish() {
		conn.cancel(true);
		
		removeRequest(this);
	}
	
	public void cancel() {
		PHConstants.phLog(this.getClass().getName()+" canceled!");
		finish();
	}
	
	/** Gets url along with parameters attached (plus auth parameters).
	 * It's public so subclasses can override it.
	 */
	public String URL() {
		if(fullUrl == null) {
			fullUrl = String.format("%s?%s", this.baseURL(), signedParamsStr());
		}
		return fullUrl;
	}
	
	/** Gets the url path set by the user. We wrap it so that you can override
	 * this method in subclasses.
	 */
	public String baseURL() {
		return urlPath;
	}
	
	/** Sets the base url.*/
	public void setBaseURL(String url) {
		this.urlPath = url;
	}
	
	/** Hashing method for submitting requests.*/
	private static String base64Sig(String in) {
		return PHStringUtil.base64Digest(in);
	}
	
	///////////////////////////////////////////////////////
	//methods that handle response. We use these so that they can be overridden later in case there is no response.
	
	/** Simple method to call delegate callbacks. We declare in another method so subclasses
	 * can handle responses in their own ways.
	 */
	protected void handleRequestSuccess(JSONObject res) {
		if(res != null)
			if (delegate != null)  delegate.requestSucceeded(this, res);
		else 
			if (delegate != null)  delegate.requestFailed(this, new JSONException("Couldn't parse json"));
		
	}
	
	/** Processes response. Can't be used for unit testing as well.*/
	public void processRequestResponse(JSONObject response) {
		JSONObject res = response.optJSONObject("response");
		
		if (res != null) {
			PHConstants.phLog("got response: "+response);
			
			handleRequestSuccess(res);
		} else {
			if (delegate != null)  delegate.requestFailed(this, new JSONException("Couldn't parse json"));
		}
	}

	//-------------------
	// PHPAsyncRequest delegate methods
	public void requestFinished(ByteBuffer response) {
		// parse into json
		try {
			String res_str = new String(response.array(), "UTF8");
			PHConstants.phLog("request finished: "+res_str);
			JSONObject json = new JSONObject(res_str);
			
			processRequestResponse(json);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			PHConstants.phLog("Couldn't parse json response. Perhaps non json?");
			
			// force us to call the delegate and inform them of the parse error.
			JSONObject json = new JSONObject();
			try {
				json.put("response", null);
				processRequestResponse(json);
			} catch(JSONException e2) {
				e2.printStackTrace();
			}
			
		}
		
	}

	public void requestFailed(Exception e) {
		PHConstants.phLog("request failed: "+e.toString());
		if (delegate != null) delegate.requestFailed(this, e);
		
	}

	public void requestResponseCode(int responseCode) {
		PHConstants.phLog("request got response code: "+responseCode);
		if(responseCode != 200) {
			if (delegate != null) delegate.requestFailed(this, new IOException("API Request failed with code: "+responseCode));
		}
	}

	public void requestProgressUpdate(int progress) {
		//TODO: handle progress updates (currently not interested)
		
	}
	
	
}
