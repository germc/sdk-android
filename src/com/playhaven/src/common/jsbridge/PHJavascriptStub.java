package com.playhaven.src.common.jsbridge;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/** Simple class which you should extend when stubbing a native javascript interface.
 * Whenever you add a method, just make sure to call through "forwarding" method and let the magic happen. 
 * Also, you'll need to pass the arguments through.
 * */
public class PHJavascriptStub {
	private WeakReference<PHJavascriptBridge> bridge;
	
	private HashMap<String, Method> methodMappings = new HashMap<String, Method>();
	
	protected void setBridge(PHJavascriptBridge bridge) {
		this.bridge = new WeakReference<PHJavascriptBridge>(bridge);
	}
	
	protected ArrayList<PHInvocation> listMethods() {
		//TODO: list methods as list of PHInvocation
		return null;
	}
	
	/** Grabs currently executing method from the stack trace.*/
	private Method currentMethod() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		
		//should be at least 3 (original -> forwardMethod -> currentMethod)
		if (stack.length < 3) {
			throw new IndexOutOfBoundsException("Cannot determine Java method for Javascript forwarding"); //unchecked exceptions..
		}
		
		String curMethodName = stack[4].getMethodName(); //[original]4, forwardMethod: 3, currentMethod:2,getStackTrace:1, getThreadStackTrace:0
		
		//check to see if we've cahced it?
		if (methodMappings.containsKey(curMethodName)) return methodMappings.get(curMethodName);
		
		Method methods[] = this.getClass().getMethods();
		Method curMethod = null;
		for(int i=0; i<methods.length; i++) {
			if (methods[i].getName().equals(curMethodName)) 
				curMethod = methods[i];
		}
		
		if (curMethod != null) methodMappings.put(curMethodName, curMethod);
		
		return curMethod;
		
	}
	
	protected Object forwardMethod(Object...args) {
		//TODO: grab currently executed method and execute on the javascript interface.
		Method curMethod = currentMethod();
		
		if (bridge.get() != null) return bridge.get().callJsMethod(curMethod, args);
		
		return null;
	}
}
