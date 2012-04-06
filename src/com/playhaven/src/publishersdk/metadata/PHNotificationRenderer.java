package com.playhaven.src.publishersdk.metadata;

import org.json.JSONObject;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

/** Simple interface for drawing the actual notification.  You must return the size
 * of the notification as well as do the actual drawing. The width varies depending
 * on the value determined by the value returned from the server. The server response can
 * pick the render by setting the "type" parameter.*/
public abstract class PHNotificationRenderer {
	/** Actually draws the notification to the canvas..*/
	public abstract void draw(Canvas canvas, JSONObject notificationData);
	/** returns the rectangle (size) in which we'll draw the rect.*/
	public abstract Rect size(JSONObject data);
}
