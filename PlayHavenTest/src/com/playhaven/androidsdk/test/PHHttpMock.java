package com.playhaven.androidsdk.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import com.playhaven.src.common.PHAsyncRequest.PHHttpConn;

/** Collection of mocking objects for http connection. We override the PHHttpRequest
 * to provide a tidy substitution. You can also adjust redirects, etc.*/
public class PHHttpMock extends PHHttpConn {
	
	private HttpUriRequest request;
	
	private PHHttpResponseMock response;
	
	private ArrayList<String> redirects = new ArrayList<String>();
	
	/** Simple class used for mocking http connections*/
	public static class PHHttpClientMock extends DefaultHttpClient {
		
	}
	
	/** Simple class used for mocking http responses*/
	public static class PHHttpResponseMock extends BasicHttpResponse {

		public PHHttpResponseMock(int responseCode) {
			super(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), responseCode, "Mock response"));
			
		}
		
		/** Creates dummy entity*/
		public void createDummyEntity(String content) throws UnsupportedEncodingException {
			this.setEntity(new StringEntity(content, "UTF8"));
		}
		
	}
	
	@Override
	/** Just return the response immediately.*/
	public HttpResponse start(HttpUriRequest request) throws IOException {
		this.request = request;
		hitRedirects();
		return response;
	}
	/** Run through all the redirects and call the handler methods.*/
	private void hitRedirects() {
		for (int i=0; i < redirects.size(); i++) {
			PHHttpResponseMock dummyResponse = new PHHttpResponseMock(302);
			dummyResponse.addHeader("Location", redirects.get(i));
			//TODO: actually call the redirect handlers (not needed now)
		}
	}
	
	public PHHttpMock() {
		response = new PHHttpResponseMock(200);
	}
	
	//-----------------------
	// Accessors
	public void addRedirect(String url) {
		redirects.add(url);
	}
	public void clearRedirects() {
		redirects.clear();
	}
	public HttpUriRequest getInitialRequest() {
		return request;
	}
	
	public void setResponseCode(int code) {
		response.setStatusCode(code);
	}
	public void setResponse(String content) throws UnsupportedEncodingException {
		response.setEntity(new StringEntity(content, "UTF8"));
	}
}
