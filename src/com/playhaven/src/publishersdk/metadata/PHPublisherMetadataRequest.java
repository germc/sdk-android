package com.playhaven.src.publishersdk.metadata;

import java.util.Hashtable;

import android.content.Context;

import com.playhaven.src.common.PHAPIRequest;

/** Class that represents a request for meta data. Primarily used for the notifications API.*/
public class PHPublisherMetadataRequest extends PHAPIRequest {
	public String placement = "";
	
	public PHPublisherMetadataRequest(Context context, String placement) {
		super(context);
		this.placement = placement;
	}
	
	public PHPublisherMetadataRequest(Context context, PHAPIRequest.Delegate delegate, String placement) {
		this(context, placement);
		this.setDelegate(delegate);
	}
	
	////////////////////////////////////////////////////
	@Override
	public String baseURL() {
		return super.createAPIURL("/v3/publisher/content/");
	}
	@Override
	public Hashtable<String, String> getAdditionalParams() {
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put("placement_id", this.placement);
		params.put("metadata", "1");
		
		return params;
	}
}
