package com.playhaven.androidsdk.test;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import com.playhaven.src.publishersdk.content.PHContent;

import android.content.res.Configuration;
import android.graphics.RectF;
import android.net.Uri;
import android.test.AndroidTestCase;

public class PHContentTest extends AndroidTestCase {
	public void testContent() throws Exception {
		String empty, keyword, rect;
		empty = "{}";
	    keyword = "{\"frame\":\"PH_FULLSCREEN\",\"url\":\"http://google.com\",\"transition\":\"PH_MODAL\",\"context\":{\"awesome\":\"awesome\"}}";
	    rect = "{\"frame\":{\"PH_LANDSCAPE\":{\"x\":60,\"y\":40,\"w\":200,\"h\":400},\"PH_PORTRAIT\":{\"x\":40,\"y\":60,\"w\":240,\"h\":340}},\"url\":\"http://google.com\",\"transition\":\"PH_DIALOG\",\"context\":{\"awesome\":\"awesome\"}}";
	    
	    JSONObject emptyDict, keywordDict, rectDict;
	    emptyDict = new JSONObject(empty);
	    keywordDict = new JSONObject(keyword);
	    rectDict = new JSONObject(rect);
	    
	    PHContent emptyC = new PHContent(emptyDict);
	    Assert.assertNotNull(emptyC);
	    
	    PHContent keywordC = new PHContent(keywordDict);
	    Assert.assertNotNull(keywordC);
	    
	    PHContent rectC = new PHContent(rectDict);
	    Assert.assertNotNull(rectC);
	    
	    RectF screenRect = new RectF(0f, 0f, 320f, 480f);
	    //TODO: test full screen (probably have to mock Display and WindowManager)
	    
	    Uri url = Uri.parse("http://google.com");
	    
	    Assert.assertTrue(url.equals(keywordC));
	    
	    Assert.assertTrue(keywordC.transition == PHContent.TransitionType.Modal);
	    
	    Assert.assertNotNull(keywordC.context.get("awesome"));
	    
	    RectF testRect = new RectF(40,60,40+240,60+340);
	    //TODO: mock context...
	    Assert.assertTrue(testRect.equals(rectC.getFrame(Configuration.ORIENTATION_LANDSCAPE, null)));
	}
	
	public void testCloseButtonDelay() throws JSONException {
		PHContent content = new PHContent();
		Assert.assertTrue(content.closeButtonDelay == 10.0f);
		
		String rect = "{\"frame\":{\"x\":60,\"y\":40,\"w\":200,\"h\":400},\"url\":\"http://google.com\",\"transition\":\"PH_DIALOG\",\"context\":{\"awesome\":\"awesome\"},\"close_ping\":\"http://playhaven.com\"}";
		JSONObject frameDict = new JSONObject(rect);
		
		PHContent mockRect = new PHContent();
		mockRect.fromJSON(frameDict);
		Assert.assertTrue(mockRect.closeURL.equals("http://playhaven.com"));
		
		
	}
}
