package com.playhaven.androidsdk.test;

import org.json.JSONObject;

import com.playhaven.src.common.PHConstants;
import com.playhaven.src.publishersdk.content.PHContentView;

import junit.framework.Assert;
import junit.framework.TestCase;

public class PHContentViewTest extends TestCase {

	public PHContentView contentView;

	public PHContentViewTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();

		contentView = new PHContentView();
		PHConstants.findDeviceInfo(contentView);
        PHConstants.setKeys("zombie7", "haven1");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void dismissRequestCallback(JSONObject context, String callback, PHContentView source) {
		  //STAssertNil(parameters, @"request with no parameters returned parameters!");
		  Assert.assertNull(source);
	}

	public void launchRequestCallback(JSONObject context, String callback, PHContentView source) {
		  //STAssertNil(parameters, @"request with no parameters returned parameters!");
		  Assert.assertNull(source);
	}

	public void testContentViewRedirects() throws Exception {
		//PHContentView contentView = new PHContentView();
		contentView.redirectRequest("ph://dismiss", this, "dismissRequestCallback");
		contentView.redirectRequest("ph://launch", this, "launchRequestCallback");
	}

}
