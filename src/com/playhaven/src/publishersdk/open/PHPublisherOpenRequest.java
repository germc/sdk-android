package com.playhaven.src.publishersdk.open;


import org.json.JSONObject;

import android.content.Context;

import com.playhaven.src.common.PHAPIRequest;

public class PHPublisherOpenRequest extends PHAPIRequest {

	
	public PHPublisherOpenRequest(Context context, PHAPIRequest.Delegate delegate) {
		this(context);
		this.setDelegate(delegate);
	}
	
	public PHPublisherOpenRequest(Context context) {
		super(context);
	}
	
	@Override
	public String baseURL() {
		return super.createAPIURL("/v3/publisher/open/");
	}
	
	@Override
	public void handleRequestSuccess(JSONObject res) {
		//TODO: handle prefetch cache response 
		
		if ( ! res.isNull("session")) {
			PHAPIRequest.setSession(res.optString("session"));
		}
		
		super.handleRequestSuccess(res);
	}

}
