package com.playhaven.androidsdk.test;

import java.util.HashMap;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.publishersdk.content.PHContent;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest;
import com.playhaven.src.publishersdk.content.PHReward;

public class PHPublisherContentRequestTest extends AndroidTestCase implements PHPublisherContentRequest.PHPublisherContentRequestDelegate {
	public void testAnimatedSetting() {
		PHPublisherContentRequest request = new PHPublisherContentRequest(this, null);
		Assert.assertTrue(request.isAnimated); //should be default yes..
		
		request.isAnimated = false;
		//sorta pointless to check if updated
		Assert.assertFalse(request.isAnimated);
	}
	
	public void testRequestParams() {
		PHPublisherContentRequest request = new PHPublisherContentRequest(this, null);
		request.placement = "placement_id";
		
		HashMap<String, String> signed = request.signedParams();
		Assert.assertNotNull(signed.get("placement_id"));
		
		String paramString = request.signedParamsStr();
		String placementParam = "placement_id=placement_id";
		Assert.assertTrue(paramString.contains(placementParam));
	}
	
	//-------------------
	//Dummy delegate methods
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void willGetContent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void willDisplayContent(PHPublisherContentRequest request,
			PHContent content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didDisplayContent(PHPublisherContentRequest request,
			PHContent content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didDismissContent(PHPublisherContentRequest request,
			PHContent content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didFail(PHPublisherContentRequest request, Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contentDidFail(PHPublisherContentRequest request, Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bitmap closeButton(PHPublisherContentRequest request, int state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int borderColor(PHPublisherContentRequest request,
			PHContent content) {
		//just use default color.
		return -1;
	}

	@Override
	public void unlockedReward(PHPublisherContentRequest request, PHReward reward) {
		// TODO Auto-generated method stub
		
	}
}
