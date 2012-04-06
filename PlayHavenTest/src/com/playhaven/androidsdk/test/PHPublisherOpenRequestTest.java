package com.playhaven.androidsdk.test;

import junit.framework.Assert;

import org.json.JSONObject;

import android.test.AndroidTestCase;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.publishersdk.open.PHPublisherOpenRequest;

public class PHPublisherOpenRequestTest extends AndroidTestCase implements PHAPIRequest.PHAPIRequestDelegate {

	@Override
	protected void setUp() {
		String token, secret;
		token = "GTmvEbpeTtKAHTcUOMk7CQ:oulbfVPeRF6tQ-1xZcYE4Q";
		secret = "hKMf0WM4SGy-dKeFOVXtAg";
		
		PHConstants.setKeys(token, secret);
		PHPublisherOpenRequest open = new PHPublisherOpenRequest(this);
		String requestUrl = open.URL();
		
		Assert.assertNotNull(requestUrl);
		Assert.assertTrue(requestUrl.contains("device="));
		Assert.assertTrue(requestUrl.contains("nonce="));
		Assert.assertTrue(requestUrl.contains("signature="));
		Assert.assertTrue(requestUrl.contains("signature="));
		
		//check if we extend the right class
		Assert.assertTrue(open instanceof PHAPIRequest);
	}
	
	//---------------------
	// dummy methods..
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		// TODO Auto-generated method stub
		
	}
}
