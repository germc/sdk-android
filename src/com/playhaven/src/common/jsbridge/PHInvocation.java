package com.playhaven.src.common.jsbridge;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import android.webkit.WebView;


/** Represents a method name and the class it's attached to.*/
public class PHInvocation {
	public Object instance;
	
	public Method method;
	
	
	public PHInvocation(Object instance, Method method) {
		this.method = method;
		this.instance = instance;
	}

}