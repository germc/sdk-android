package com.playhaven.src.publishersdk.content;

import com.playhaven.src.common.PHAPIRequest;

/** Represents a sub request which we can use to handle ph:// urls from the webview*/
public class PHPublisherSubContentRequest extends PHAPIRequest {
	public PHContentView source;
	
	public String callback;
	
	public PHPublisherSubContentRequest(PHAPIRequest.PHAPIRequestDelegate delegate) {
		super(delegate);
	}
	
	@Override
	public String URL() {
		if(this.fullUrl == null) {
			// we don't want any prefix or signed params, just the base url.
			// This will have been set externally.
			this.fullUrl = this.baseURL();
		}
		return this.fullUrl;
	}
}
