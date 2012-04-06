package com.playhaven.resources;

import android.util.Base64;

/** Represents a wrapper around simple resource. You should extend this class to provide specific
 * types (image, xml, text, etc.). Each separate resource is represented by a simple class that extends
 * this class. We do this when embedding the resources in code because the base64 representation is oft rather large.
 * 
 * Note: Under no circumstances should you every *cache* or reference Drawables with a strong reference. If you do, 
 * the SDK will leak memory on orientaiton change becauase of the static context reference. Instead, you should work with
 * Bitmap objects, NinePatch objects directly and only include <em>convenience</em> methods for creating drawables. You should
 * not maintain a reference to those drawables.
 * @author samuelstewart
 *
 */
public class PHResource {
	private String key;
	
	/** base 64 encoded data.*/
	private String data; 
	
	//-------------------
	// Data accessor methods
	
	//sets the data by encoding to base64
	public void setDataByte(byte[] data) {
		data = Base64.encode(data, Base64.NO_PADDING);
	}
	
	public void setDataStr(String data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return Base64.decode(data, Base64.NO_PADDING);
	}
	
	public String getResourceKey() {
		return key;
	}
	
	public void setResourceKey(String key) {
		this.key = key;
	}
}
