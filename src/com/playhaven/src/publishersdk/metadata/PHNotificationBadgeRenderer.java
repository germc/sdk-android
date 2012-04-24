package com.playhaven.src.publishersdk.metadata;

import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

import com.playhaven.resources.PHNinePatchResource;
import com.playhaven.resources.PHResourceManager;
import com.playhaven.src.additions.ObjectExtensions;
import com.playhaven.src.common.PHConstants;

/** Represents a renderer for drawing a badge notification.*/
public class PHNotificationBadgeRenderer extends PHNotificationRenderer {
	private NinePatchDrawable badgeImage;
	
	private final float TEXT_SIZE = 17.0f;
		
	private final float TEXT_SIZE_REDUCE = 8.0f;
	
	private Paint whitePaint;
	
	public PHNotificationBadgeRenderer(Resources res) {
		PHNinePatchResource ninePatchRes = (PHNinePatchResource)PHResourceManager.sharedResourceManager().getResource("badge_image");
		
		badgeImage = ninePatchRes.loadNinePatchDrawable(res, PHConstants.getScreenDensityType());
		badgeImage.setFilterBitmap(true);
	}
	
	//Section: Badge Renderer Methods
	//======================================
	@Override
	public void draw(Canvas canvas, JSONObject notificationData) {
		int value = requestedValue(notificationData);
		if(value == 0) return;
		
		Rect size = size(notificationData);
		badgeImage.setBounds(size);
		
		badgeImage.draw(canvas);
		
		canvas.drawText(Integer.toString(value), PHConstants.dipToPixels(10.0f), PHConstants.dipToPixels(17.0f), getTextPaint());
		
	}
	
	private Paint getTextPaint() {
		if (whitePaint == null) {
			whitePaint = new Paint();
			whitePaint.setStyle(Paint.Style.FILL);
			whitePaint.setAntiAlias(true);
			whitePaint.setTextSize(PHConstants.dipToPixels(TEXT_SIZE));
			//whitePaint.setTypeface(Typeface.DEFAULT_BOLD); Should we use a bold font or not?
			whitePaint.setColor(Color.WHITE);
		}
		return whitePaint;
	}
	
	/** Grabs the actual value to display in this notification. If the "value" key doesn't exist, we return 0.*/
	private int requestedValue(JSONObject notificationData) {
		if(notificationData != null) {
			String valueStr = ObjectExtensions.JSONExtensions.getJSONString(notificationData, "value");
			int value = Integer.parseInt(valueStr);
			return value;
		}
		return 0;
	}
	
	@Override
	public Rect size(JSONObject data) {
		//the width and 
		float width = badgeImage.getMinimumWidth(); // unlike the iOS SDK, we must start with baseline width to ensure image scales correctly.
		float height = badgeImage.getMinimumHeight();
		
		int value = requestedValue(data);
		if(value == 0) return new Rect(0,0,0,0);
		
		float valueWidth = getTextPaint().measureText(String.valueOf(requestedValue(data)));
		width = (width + valueWidth) - PHConstants.dipToPixels(TEXT_SIZE_REDUCE);
		
		PHConstants.phLog("Notification Width: "+width+" valueWidth: "+valueWidth);
		return new Rect(0, 0, (int)width, (int)height);
	}

}
