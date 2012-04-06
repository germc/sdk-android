package com.playhaven.androidsdk.test;

import java.util.HashMap;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;  

import com.playhaven.sampleapp.PublisherContentView;
import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.publishersdk.content.PHContent;
import com.playhaven.src.publishersdk.content.PHContentView;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest.PHDismissType;
import com.playhaven.src.publishersdk.content.PHPurchase;
import com.playhaven.src.publishersdk.content.PHReward;

public class PHPublisherContentRequestTest extends ActivityInstrumentationTestCase2<PublisherContentView> implements PHPublisherContentRequest.PHPublisherContentRequestDelegate, PHContentView.PHContentViewDelegate {

	public PublisherContentView contentView;

	public PHPublisherContentRequestTest() {  
		super("com.playhaven.sampleapp.PublisherContentView", PublisherContentView.class);  
	}  

	protected void setUp() throws Exception {
		super.setUp();

		contentView = getActivity();
		PHConstants.findDeviceInfo(contentView);
        PHConstants.setKeys("zombie7", "haven1");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAnimatedSetting() {
        //PHConstants.setKeys("zombie7", "haven1");
		PHPublisherContentRequest request = new PHPublisherContentRequest(this, contentView);
		Assert.assertTrue(request.isAnimated); //should be default yes..
		
		request.isAnimated = false;
		//sorta pointless to check if updated
		Assert.assertFalse(request.isAnimated);
	}
	
	public void testRequestParams() {
		//PHContentView contentView = new PHContentView();
		//PHConstants.findDeviceInfo(contentView);
        //PHConstants.setKeys("zombie7", "haven1");
		PHPublisherContentRequest request = new PHPublisherContentRequest(this, contentView);
		request.placement = "placement_id";
		
		HashMap<String, String> signed = request.signedParams();
		Assert.assertNotNull(signed.get("placement_id"));
		
		String paramString = request.signedParamsStr();
		String placementParam = "placement_id=placement_id";
		Assert.assertTrue(paramString.contains(placementParam));
	}

	public void testContent() throws Exception {
		String empty, keyword, rect;
		empty = "{}";
	    //keyword = "{\"frame\":\"PH_FULLSCREEN\",\"url\":\"http://google.com\",\"transition\":\"PH_MODAL\",\"context\":{\"awesome\":\"awesome\"}}";
		// This is new to stop exception from all keys not available
		// NOTE: fails from exception because not all keys are in JSON
		keyword = "{\"frame\":{\"PH_LANDSCAPE\":{\"x\":60,\"y\":40,\"w\":200,\"h\":400},\"PH_PORTRAIT\":{\"x\":40,\"y\":60,\"w\":240,\"h\":340}},\"url\":\"http://google.com\",\"transition\":\"PH_MODAL\",\"context\":{\"awesome\":\"awesome\"},\"close_ping\":\"http://playhaven.com\", \"close_delay\":\"0.1\"}";
		rect = "{\"frame\":{\"PH_LANDSCAPE\":{\"x\":60,\"y\":40,\"w\":200,\"h\":400},\"PH_PORTRAIT\":{\"x\":40,\"y\":60,\"w\":240,\"h\":340}},\"url\":\"http://google.com\",\"transition\":\"PH_DIALOG\",\"context\":{\"awesome\":\"awesome\"},\"close_ping\":\"http://playhaven.com\", \"close_delay\":\"0.1\"}";
	    
	    JSONObject emptyDict, keywordDict, rectDict;
	    emptyDict = new JSONObject(empty);
	    keywordDict = new JSONObject(keyword);
	    rectDict = new JSONObject(rect);
	    
	    PHContent rectC = new PHContent(rectDict);
		Assert.assertTrue(rectC.closeURL.equals("http://playhaven.com"));
	    
	    PHContent emptyC = new PHContent(emptyDict);
		Assert.assertTrue(emptyC.url == null);
	    
	    PHContent keywordC = new PHContent(keywordDict);
	    Assert.assertFalse(keywordC.url == null);
	    
	    //RectF screenRect = new RectF(0f, 0f, 320f, 480f);
	    
	    Uri url = Uri.parse("http://google.com");
	    
	    Assert.assertTrue(url.equals(keywordC.url));
	    
	    Assert.assertTrue(keywordC.transition == PHContent.TransitionType.Modal);
	    
	    Assert.assertNotNull(keywordC.context.get("awesome"));
	}
	
	public void testCloseButtonDelay() throws JSONException {
		PHContent content = new PHContent();
		Assert.assertTrue(content.closeButtonDelay == 10.0f);
	}

	public void testCloseButtonUrlParameter() throws JSONException {
		  PHContent content = new PHContent();
		  Assert.assertTrue(content.closeURL == null);
		  
		  String rect = "{\"frame\":{\"PH_LANDSCAPE\":{\"x\":60,\"y\":40,\"w\":200,\"h\":400},\"PH_PORTRAIT\":{\"x\":40,\"y\":60,\"w\":240,\"h\":340}},\"url\":\"http://google.com\",\"transition\":\"PH_DIALOG\",\"context\":{\"awesome\":\"awesome\"},\"close_ping\":\"http://playhaven.com\", \"close_delay\":\"0.1\"}";
		  JSONObject frameDict = new JSONObject(rect);
			
		  PHContent mockRect = new PHContent();
		  mockRect.fromJSON(frameDict);
		  Assert.assertTrue(mockRect.closeURL.equals("http://playhaven.com"));
	}

	//-------------------
	// delegate methods
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		
	}

	public void willGetContent() {
		
	}

	public void willGetContent(PHPublisherContentRequest request) {
		
	}

	@Override
	public void willDisplayContent(PHPublisherContentRequest request, PHContent content) {
		
	}

	@Override
	public void didDisplayContent(PHPublisherContentRequest request, PHContent content) {
		
	}

	public void didDismissContent(PHPublisherContentRequest request, PHContent content) {
		
	}

	public void didDismissContent(PHPublisherContentRequest request, PHPublisherContentRequest.PHDismissType type) {
		
	}

	@Override
	public void didShow(PHContentView view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didLoad(PHContentView view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didFail(PHContentView view, String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didDismiss(PHContentView view, PHDismissType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didUnlockReward(PHContentView view, PHReward reward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didMakePurchase(PHContentView view, PHPurchase purchase) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didSendSubrequest(JSONObject context, String callback,
			PHContentView source) {
		// TODO Auto-generated method stub
		
	}
}
