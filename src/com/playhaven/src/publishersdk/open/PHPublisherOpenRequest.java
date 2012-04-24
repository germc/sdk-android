package com.playhaven.src.publishersdk.open;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.prefetch.PHUrlPrefetchOperation;

public class PHPublisherOpenRequest extends PHAPIRequest implements PHAPIRequest.PHAPIRequestDelegate {
	
	private PHAPIRequest.PHAPIRequestDelegate caller = null;

	public PHPublisherOpenRequest() {
		super(PHConstants.getToken(), PHConstants.getSecret());
		caller = null;
		
	}

	public PHPublisherOpenRequest(PHAPIRequest.PHAPIRequestDelegate delegate) {
		super(delegate);
		caller = null;

	}

	public void setDelegate(PHAPIRequest.PHAPIRequestDelegate delegate) {
		caller = delegate;
		super.setDelegate(this);
	
	}

	@Override
	public String baseURL() {
		return PHConstants.phURL("/v3/publisher/open/");
	}

	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {

		PHConstants.phLog("Prefetch in open request succeeded: "+responseData.toString());

		try {
			JSONArray urls = responseData.getJSONArray("precache");

			for (int i = 0; i < urls.length(); ++i) {
			    String url = urls.getString(i);			    
			    PHUrlPrefetchOperation prefetchFile = new PHUrlPrefetchOperation();
			    prefetchFile.execute(url);
			    	
			}

			// TODO: DEBUGGING ONLY!!! - This content template is for Android and not in precache URL array
			//PHUrlPrefetchOperation debugPrefetchFile = new PHUrlPrefetchOperation();
		    //debugPrefetchFile.execute("http://media.playhaven.com/content-templates/31e1f1e4f74765679c2e9e08e7c7903011d27033/html/image.html.gz");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (caller != null) 
			caller.requestSucceeded(request, responseData);
		
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		if (caller != null) 
			caller.requestFailed(request, e);

	}

}
