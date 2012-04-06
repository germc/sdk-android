package com.playhaven.androidsdk.test;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.AndroidTestCase;

import com.playhaven.src.common.PHAPIRequest;


public class PHAPIRequestResponseTest extends AndroidTestCase implements PHAPIRequest.PHAPIRequestDelegate {
	private boolean didProcess;
	private PHAPIRequest request;
	public void setUp() {
		request = new PHAPIRequest(this);
		didProcess = false;
	}
	
	public void tearDown() {
		Assert.assertTrue("Didn't process request.", didProcess);
	}
	public void testResponse() {
		
		try {
			JSONObject testDict = new JSONObject();
			testDict.put("awesome", "awesomesause");
			
			JSONObject responseDict = new JSONObject();
			responseDict.put("error", null);
			responseDict.put("errobj", null);
			responseDict.put("response", testDict);
			
			request.processRequestResponse(responseDict);
		} catch(JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	// delegate call backs
	//--------------------
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		try {
			Assert.assertNotNull(responseData);
			Assert.assertTrue(responseData.length() == 1);
			Assert.assertTrue(responseData.has("awesome"));
			Assert.assertTrue(responseData.getString("awesome").equals("awesomesause"));
			
			didProcess = true;
		} catch(JSONException e) {
			Assert.fail(e.toString());
		}
		
		
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		Assert.fail("API Request failed with error: "+e.toString());
	}
	

	
}
