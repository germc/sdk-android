package com.playhaven.src.publishersdk.promos;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;

public class PHPublisherPromoRequest extends PHAPIRequest {
	
	@Override
	public String baseURL() {
		return PHConstants.phFormatUrl("/v3/publisher/promos/");
	}
	
	public PHPublisherPromoRequest(PHAPIRequest.PHAPIRequestDelegate delegate) {
		super(delegate);
	}	
	
}
