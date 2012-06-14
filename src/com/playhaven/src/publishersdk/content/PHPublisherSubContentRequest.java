package com.playhaven.src.publishersdk.content;

import org.json.JSONObject;

import android.content.Context;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.utils.PHStringUtil;

/** Represents a sub request which we can use to handle ph:// urls from the webview*/
public class PHPublisherSubContentRequest extends PHAPIRequest {
	public PHContentView source;
	
	public String callback;
	
	public PHPublisherSubContentRequest(Context context, PHAPIRequest.Delegate delegate) {
		super(context, delegate);
	}
	
	@Override
	public String getURL() {
		if(this.fullUrl == null) {
			// we don't want any prefix or signed params, just the base url.
			// This will have been set externally.
			this.fullUrl = this.baseURL();
		}
		return this.fullUrl;
	}
	
	@Override
	public void send() {
		if ( ! JSONObject.NULL.equals(baseURL()) 	&&
		       baseURL().length() > 0				  ) {
			
			super.send();
			return;
		}
		
		PHStringUtil.log("No URL set for PHPublisherSubContentRequest");
		
	}
}
