package com.playhaven.androidsdk.test;

import org.json.JSONObject;

import junit.framework.Assert;
import android.test.AndroidTestCase;

import com.playhaven.src.publishersdk.metadata.PHPublisherMetadataRequest;
import com.playhaven.src.common.PHAPIRequest;

public class PHPublisherMetadataRequestTest extends AndroidTestCase implements PHAPIRequest.PHAPIRequestDelegate {
	public void setUp() {
		//very basic testing which really only matters on iOS
		PHPublisherMetadataRequest request = new PHPublisherMetadataRequest(this);
		Assert.assertNotNull(request);
	}

	//Section: Delegate callbacks.
	//======================================
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		// TODO Auto-generated method stub
		
	}
}
