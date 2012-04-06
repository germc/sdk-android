package com.playhaven.androidsdk.test;

import org.json.JSONObject;

import android.test.AndroidTestCase;

import com.playhaven.src.common.PHConstants;
import com.playhaven.src.publishersdk.promos.PHPublisherPromoRequest;
import com.playhaven.src.common.PHAPIRequest;

public class PHPublisherTokensRequestTest extends AndroidTestCase implements PHAPIRequest.PHAPIRequestDelegate {
	boolean didHandleRequest;
	private PHPublisherPromoRequest request;
	@Override
	protected void setUp() {
		String token, secret;
		token = "GTmvEbpeTtKAHTcUOMk7CQ:oulbfVPeRF6tQ-1xZcYE4Q";
		secret = "hKMf0WM4SGy-dKeFOVXtAg";
		
		PHConstants.setKeys(token, secret);
		request = new PHPublisherPromoRequest(this);
	}
	
	//---------------------
	//delegate methods for PHAPIRequest
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		// TODO Auto-generated method stub
		
	}
}
