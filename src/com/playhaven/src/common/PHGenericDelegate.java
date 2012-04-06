package com.playhaven.src.common;

import android.os.Handler;

/** Represents a very generic delegate. Basically, we extend Handler so that we can post updates on the main UI thread.
 * We want to hide the "class wrapping" that usually has to happen when dealing with handlers. Thus, we simply provide a handler
 * from wherever this delegate is called. It allows us to do away with the tiring Runnable architecture.
 * 
 * Make sure you always call the constructor before using the delegate b/c the handler needs to "grab" the current thread.
 * 
 * The type parameter T is the interface type that the delegate should be implementing. Make sure you always
 * set the delegate as well.
 * @author samuelstewart
 *
 */
public class PHGenericDelegate <T> {
	private Handler delegateHandler;
	public T delegate;
	
	public PHGenericDelegate(T delegate) {
		this.delegate = delegate;
		createDelegateOnThread();
	}
	/** Re-creates the delegate on the current thread. You usually won't need to call this,
	 * but sometimes may want to.
	 */
	public void createDelegateOnThread() {
		delegateHandler = new Handler(); // "grabs" current thread
	}
	
	/** Gets the handler so that we can post messages to the handler*/
	public Handler getThreadHandler() {
		return delegateHandler;
	}
}