package com.playhaven.resources;

import java.util.Hashtable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.DisplayMetrics;

/** Represents image resource which wraps a base64 encoded image. It contains a dictionary of images for the different densities.
 * This dictionary of different values allows us to automatically handle different screen densities (like the default resource loading does).
 * 
 * Sub-casses should call the setDataStr along with the <em>target</em> density of the image data. We'll handle the scaling up/down.
 */
public class PHImageResource extends PHResource {
	
	//map between device density enum and bitmap images
	private Hashtable<Integer, Bitmap> cached_images = new Hashtable<Integer, Bitmap>();
	
	//map between device density enum and base64 data
	private Hashtable<Integer, String> data_map = new Hashtable<Integer, String>();
	
	protected int densityType = DisplayMetrics.DENSITY_DEFAULT;
	
	/** Returns the image data (from internal map) based on the specified density.*/
	public byte[] getData(int density) {
		String dataStr = data_map.get(new Integer(density));
		
		if (dataStr == null) return null;
		
		return Base64.decode(dataStr.getBytes(), Base64.NO_PADDING);
	}
	
	/** Sets the image data for a "designed" density.*/
	public void setDataStr(int density, String data) {
		if (data != null)
			data_map.put(new Integer(density), data);
	}
	
	/** Sets the data for multiple "designed" densities*/
	public void setDataStr(int[] densities, String data) {
		for (int density : densities) {
			this.setDataStr(density, data);
		}
	}
	@Override
	public void setDataStr(String data) {
		throw new UnsupportedOperationException("You must use setDataStr(density, data) when setting image data");
	}
	
	@Override
	public byte[] getData() {
		throw new UnsupportedOperationException("You must use getData(density) when loading images");
	}
	
	/** Attempts to load the image data with the closet density (absolute distance).*/
	private Bitmap getClosestImage(int requestedDensity) {
		// find closest density (find smallest difference)
		if (data_map.size() == 0) return null;
		
		int minDiff = Integer.MAX_VALUE;
		int closestDensity = DisplayMetrics.DENSITY_DEFAULT;
		
		for (Integer density : data_map.keySet()) {
		 	int diff = Math.abs( density - requestedDensity );
			
			if (diff < minDiff) { // continually refine our minimum distance..
				closestDensity = density;
				minDiff = diff;
			}
		}
		
		byte[] buffer = getData(closestDensity); // guaranteed to have the density..
		
		if (buffer == null) return null;
		
		Bitmap closestImage = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
		closestImage.setDensity(closestDensity); //IMPORTANT! We must set this to support scaling
		
		return closestImage;
	}
	
	/** Converts b64 image into the image with the appropriate density.*/
	private Bitmap loadImage() throws ArrayIndexOutOfBoundsException {
		Bitmap cached_image = cached_images.get(new Integer(densityType));
		
		if (cached_image == null) {
			cached_image = getClosestImage(densityType);

			// if still no data!!
			if (cached_image == null)
				throw new ArrayIndexOutOfBoundsException("You have not specified image data for the requested density");
			
			cached_images.put(new Integer(densityType), cached_image);
		}
		
		return cached_image;
	}
	
	/** Loads the image data and scales according to specified density. "densityType" should be a value from {@link DeviceMetrics}*/
	public Bitmap loadImage(int densityType) throws ArrayIndexOutOfBoundsException {
		this.densityType = densityType;
		return loadImage();
	}
	
	
	
	/////////////////////////////////////////////////////////////////////
	/////////////////////////// Convenience Methods /////////////////////
	
	public BitmapDrawable loadBitmapDrawable(Resources res, int densityType) throws ArrayIndexOutOfBoundsException {
		return new BitmapDrawable(res, loadImage(densityType));
	}
}
