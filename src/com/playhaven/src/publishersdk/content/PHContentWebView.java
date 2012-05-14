package com.playhaven.src.publishersdk.content;

import android.content.Context;
import android.content.res.Configuration;
import android.webkit.WebView;

/** The actual webview that does the loading of the advertisments. */
public class PHContentWebView extends WebView {

	/** Constructor which allows us to create our webview */
	public PHContentWebView(Context context) {
		super(context);
		this.getSettings().setJavaScriptEnabled(true);
		this.setHorizontalScrollBarEnabled(false);
	}


	/** Clears all the content without having to reload.*/
	public void clearContent() {
		// detailed: http://stackoverflow.com/questions/2933315/clear-uiwebview-content-upon-dismissal-of-modal-view-iphone-os-3-0
		this.loadUrl("javascript:document.open();document.close();");
	}

	/**
	 * Tells the webview content we've changed orientation. Call into
	 * javascript.
	 */
	public void updateOrientation(int orientation) {
		String jsonStr = (orientation == Configuration.ORIENTATION_LANDSCAPE ? "PH_LANDSCAPE"
				: "PH_PORTRAIT");
		String javascript = String.format("PlayHaven.updateOrientation(%s)",
				jsonStr);
		this.loadUrl("javascript:" + javascript);
	}
}
