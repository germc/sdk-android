package com.playhaven.src.common;

/**
 * @class PHError
 * @author samstewart
 * Extremely simple error container class
 */
public class PHError {
	protected int errorCode;
	
	protected String message;
	
	public PHError(String message, int errorCode) {
		this.message = message;
		this.errorCode = errorCode;
	}
	
	public PHError(String message) {
		this(message, -1);
	}
	
	public PHError(int errorCode) {
		this("", errorCode);
	}
	
	public String toString() {
		return String.format("PHError with message '%s' and error code %d", this.message, this.errorCode);
	}
	
	////////////////////////////////////////////
	//////////////// Accessors /////////////////
	
	public int getErrorCode() {
		return this.errorCode;
	}
	
	public String getMessage() {
		return this.message;
	}
}
