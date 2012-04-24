package com.playhaven.src.common.jsbridge;

import java.lang.reflect.Method;

/** Represents a method name and the class it's attached to.*/
public class PHInvocation {
	public Object instance;
	
	public Method method;
	
	
	public PHInvocation(Object instance, Method method) {
		this.method = method;
		this.instance = instance;
	}

}