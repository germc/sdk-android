package com.playhaven.src.common.jsbridge;

import java.lang.ref.WeakReference;

/** Simple class you should extend when providing a native interface to javascript.*/
public class PHJavascriptInterface {
	private WeakReference<PHJavascriptBridge> bridge;
	
	protected void setBridge(PHJavascriptBridge bridge) {
		this.bridge = new WeakReference<PHJavascriptBridge>(bridge);
	}
}