package com.playhaven.src.publishersdk.metadata;

import java.util.HashMap;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;

/** Class that represents a request for meta data. Primarily used for the notifications API.*/
public class PHPublisherMetadataRequest extends PHAPIRequest {
	private String placement = "";
	
	public PHPublisherMetadataRequest(PHAPIRequest.PHAPIRequestDelegate delegate) {
		super(delegate);
	}
	public PHPublisherMetadataRequest(PHAPIRequest.PHAPIRequestDelegate delegate, String placement) {
		super(delegate);
		this.placement = placement;
	}
	public PHPublisherMetadataRequest(PHAPIRequest.PHAPIRequestDelegate delegate, String token, String secret, String placement) {
		super(delegate, token, secret);
		this.placement = placement;
	}
	
	//Section: Override customizations
	////////////////////////////////////////////////////
	@Override
	public String baseURL() {
		return PHConstants.phURL("/v3/publisher/content/");
	}
	@Override
	public HashMap<String, String> getAdditionalParams() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("placement_id", this.placement);
		params.put("metadata", "1");
		return params;
	}
	
	//Section: Accessors
	/////////////////////////////////////////////////////
	public String getPlacement() {
		return placement;
	}
	public void setPlacement(String placement) {
		this.placement = placement;
	}
}
