package com.playhaven.androidsdk.test;

import junit.framework.Assert;

import org.json.JSONObject;

import android.test.AndroidTestCase;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHAsyncRequest;
import com.playhaven.src.common.PHAsyncRequest.PHHttpConn;

/** Tests the {@link PHAPIRequest} class extensively*/
public class PHAPIRequestTest extends AndroidTestCase implements PHAPIRequest.PHAPIRequestDelegate {
	private PHHttpConn http_con;
	private PHAsyncRequest request;
	public void setUp() {
		
	}
	
	public void testPHHttpCon() {
		
	}
	public void testSignatureHash() {
		
	}
	
	public void testRequestParameters() {
		
	}
	
	/** Tests the generation of the request url.*/
	public void testURLProperty() {
		PHAPIRequest request = new PHAPIRequest(this);
		String desiredURL = "http://thisisatesturlstring.com";
		
		request.setBaseURL(desiredURL);
		Assert.assertTrue(request.URL().contains(desiredURL));
	}
	
	//-------------------------
	//dummy delegate methods which we'll never use.
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		// TODO Auto-generated method stub
		
	}
}
