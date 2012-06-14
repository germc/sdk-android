package com.playhaven.src.utils;

import com.playhaven.src.common.PHConfig;

public class PHConversionUtils {
	
	public static float dipToPixels(float pixels) {
		return pixels * PHConfig.screen_density;
	}
}
