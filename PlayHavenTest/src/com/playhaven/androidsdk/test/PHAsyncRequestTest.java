package com.playhaven.androidsdk.test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.playhaven.src.common.PHAsyncRequest;

public class PHAsyncRequestTest extends AndroidTestCase implements PHAsyncRequest.PHAsyncRequestDelegate {
	private boolean wasSuccessful = false;
	private int statusCode = 0;
	private String responseStr;
	PHAsyncRequest request = new PHAsyncRequest(this);
	
	public void setUp() {
		wasSuccessful = false;
	}
	public void testHttpMockRequest() throws Exception {
		PHHttpMock httpConn = new PHHttpMock();
		responseStr = "hello";
		statusCode = 200;
		httpConn.setResponse(responseStr);
		httpConn.setResponseCode(statusCode);
		
		HttpResponse response = httpConn.start(new HttpGet("http://google.com"));
		Assert.assertNotNull(response);
		Assert.assertEquals(statusCode, response.getStatusLine().getStatusCode());
		
		Assert.assertNotNull(response.getEntity());
		String extractedRes = PHAsyncRequest.streamToString(response.getEntity().getContent());
		
		Assert.assertTrue(extractedRes.contains(responseStr));
	}
	public void testUrlRequestGet() throws Exception {
		wasSuccessful = false;
		PHHttpMock httpMock = new PHHttpMock();
		responseStr = "{\"fake_key\":\"fake value\"";
		httpMock.setResponse(responseStr);
		statusCode = 200;
		httpMock.setResponseCode(statusCode);
		
		request.setHttpClient(httpMock);
		
		//latch to insure sync
		CountDownLatch signal = new CountDownLatch(1);
		request.setCountDownLatch(signal);
		
		request.execute(Uri.parse("http://fakeapi.com/get"));
		signal.await();//block until completed..
	}
	public void testUrlRequestPost() {
		
	}
	public void testAsyncRequestRedirect() {
		PHHttpMock httpMock = new PHHttpMock();
		
	}
	
	//--------------
	//PHAsyncRequest Delegate Methods
	@Override
	public void requestFinished(ByteBuffer response) {
		wasSuccessful = true;
		try {
			String res = new String(response.array(), "UTF8");
			Assert.assertTrue(res.equalsIgnoreCase(responseStr));
		} catch(UnsupportedEncodingException e) {
			Assert.assertNull("Error with string encoding: "+e, e);//this will fail of course..
		}
		
	}

	@Override
	public void requestFailed(Exception e) {
		Assert.assertNotNull(e);
		Assert.fail("Error with connection: "+e);
		
	}

	@Override
	public void requestResponseCode(int responseCode) {
		Assert.assertTrue(responseCode >= 0);
		Assert.assertTrue("We received an invalid response code", statusCode == responseCode);
		
	}

	@Override
	public void requestProgressUpdate(int progress) {
		//ignore progress updates as it will be instantaneous
	}
}
