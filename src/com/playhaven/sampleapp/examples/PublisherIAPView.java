package com.playhaven.sampleapp.examples;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.publishersdk.content.PHPurchase;
import com.playhaven.src.publishersdk.purchases.PHPublisherIAPTrackingRequest;

/**
 * 
 * @author samstewart
 * Simple class for testing IAP purchases tracking request.
 */
public class PublisherIAPView extends ExampleView implements PHAPIRequest.Delegate {
	private PHPublisherIAPTrackingRequest request;
	
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		
		setTitle("IAP Tracking Request");
	}
	
	@Override
	public void startRequest() {
		super.startRequest();
		
		request = new PHPublisherIAPTrackingRequest(this, "sword_003", 2, PHPurchase.Resolution.Buy);
		
		request.send();
	}

	
	/////////////////////////////////////////////////////////
	///////////////////// Delegate Methods //////////////////
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		try {
			String formattedJson = responseData.toString(2);
			super.addMessage(String.format("Success with response: %s", formattedJson));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	public void requestFailed(PHAPIRequest request, Exception e) {
		super.addMessage(String.format("Failed with error: %s", e.toString()));
		
	}	
	
	
}
