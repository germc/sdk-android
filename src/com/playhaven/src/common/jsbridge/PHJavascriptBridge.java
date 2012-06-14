package com.playhaven.src.common.jsbridge;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;
import android.webkit.WebView;

/**
 * Simple bridge between Java native code and Javascript in a web view. It wraps
 * a webview (Decorator pattern) and allows you to expose a native interface and
 * stub a Javascript interface. Note: we will probably implement this on the iOS
 * version as well.
 */

public class PHJavascriptBridge {
	private HashMap<String, PHJavascriptInterface> nativeInterfaces = new HashMap<String, PHJavascriptInterface>();

	private HashMap<String, PHJavascriptStub> jsStubs = new HashMap<String, PHJavascriptStub>();

	private WebView webview;

	public PHJavascriptBridge(WebView webview) {
		this.webview = webview;
	}

	public void addNativeInterface(String namespace, PHJavascriptInterface nativeInterface) {
		//TODO: use weak references!!!!!
		// tie the native interface back to the bridge
		nativeInterface.setBridge(this);

		nativeInterfaces.put(namespace, nativeInterface);

		webview.addJavascriptInterface(nativeInterface, namespace);
	}
	
	/** Same as below but uses class name as namespace.*/
	public void addJavascriptStub(PHJavascriptStub javascriptInterface) {
		this.addJavascriptStub(javascriptInterface.getClass().getSimpleName(), javascriptInterface);
	}
	
	/** Adds a javascript stub which will call through and convert to javascript calls.*/
	public void addJavascriptStub(String namespace, PHJavascriptStub javascriptInterface) {
		
		// inform the interface where to callback
		javascriptInterface.setBridge(this);
		
		//TODO: use weak references!!!!!
		jsStubs.put(namespace, javascriptInterface);
	}

	/**
	 * Converts java method to javascript method signature and calls the method
	 * and returns the result. Currently returns null.
	 */
	protected Object callJsMethod(Method method, Object... args) {
		//TODO: test difference between getName and getSimpleName
		String className = method.getDeclaringClass().getSimpleName();
		String namespace = null;
		
		//find the JS namespace prefix we should use
		Iterator<String> keys = jsStubs.keySet().iterator();
		
		while(keys.hasNext()) {
			String key = keys.next();
			PHJavascriptStub stub = jsStubs.get(key);
			
			//TODO: figure out why we can't find the class given the name..
			if (stub.getClass().getSimpleName().equals(className)) {
				namespace = key;
			}
		}
		
		if (namespace != null) namespace = namespace.concat("."); //add dot before method name
		
		StringBuilder argumentString = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			argumentString.append(args.toString());
			
			if (i < args.length-1)
				argumentString.append(","); //if not last element, add ,
		}
		
		
		String javascriptCommand = String.format("%s(%s)", namespace, argumentString.toString());
		Log.i("Playhaven Debug", "Javascript call: "+javascriptCommand);
		
		webview.loadUrl("javascript:"+javascriptCommand);
		
		//for now, we do not return a result..
		return null;
	}
}
