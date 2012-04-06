package com.playhaven.androidsdk.test;

import junit.framework.Assert;

import org.json.JSONObject;

import android.test.AndroidTestCase;

import com.playhaven.src.common.PHAPIRequest;

/** Tests the error handling of the api request.*/
public class PHAPIRequestErrorTest extends AndroidTestCase implements PHAPIRequest.PHAPIRequestDelegate {
	private PHAPIRequest request;
	private boolean didProcess;
	
	public void setUp() {
		request = new PHAPIRequest(this);
		didProcess = false;
		
	}

	public void tearDown() {
		Assert.assertTrue(didProcess);
	}
	// delegate methods
	//----------------------------
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		Assert.fail("Request succeeded but it shouldn't have. "+responseData);
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		Assert.assertNotNull(e);
		didProcess = true;
	}
}
