package com.playhaven.resources;

import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

import com.playhaven.src.common.PHConstants;

/** Represents a ninepatch scalable drawable. The data must be a png file which has been encoded by android
 * into the "nine patch" format complete with data chunk. (use abrc script) 
 * StackOverflow: http://stackoverflow.com/questions/5079868/create-a-ninepatch-ninepatchdrawable-in-runtime
 * @author samuelstewart
 *
 */
public class PHNinePatchResource extends PHImageResource {
	private HashMap<Integer, NinePatch> nine_patch_cache = new HashMap<Integer, NinePatch>();
	
	
	/** Follows same pattern as {@link PHImageResource}*/
	public NinePatch loadNinePatch(int densityType) {
		super.densityType = densityType;
		return loadNinePatch();
	}
	
	/** Loads the nine patch drawable from the cached data.*/
	private NinePatch loadNinePatch() {
		NinePatch cachedNinePatch = nine_patch_cache.get(new Integer(this.densityType));
		
		if (cachedNinePatch == null) {
			
			Bitmap image = super.loadImage(densityType); // will get appropriate image from our data strings..
			
			//create nine patch image..
			byte[] chunk = image.getNinePatchChunk();
			
			//TODO: for debugging only..
			boolean result = NinePatch.isNinePatchChunk(chunk);
			
			if (!NinePatch.isNinePatchChunk(chunk)) return null;
			
			//We use "res" to ensure correct target density is set
			cachedNinePatch = new NinePatch(image, chunk, null);
			PHConstants.phLog("Loaded nine patch: "+cachedNinePatch);
			
			nine_patch_cache.put(new Integer(densityType), cachedNinePatch);
		}
		
		return cachedNinePatch;
	}
	
	/** Convenience method for loading ninepatch*/
	public NinePatchDrawable loadNinePatchDrawable(Resources res, int densityType) {
		return new NinePatchDrawable(res, loadNinePatch(densityType));
	}
}
