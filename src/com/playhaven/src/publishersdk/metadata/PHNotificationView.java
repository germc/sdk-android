package com.playhaven.src.publishersdk.metadata;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConfig;
import com.playhaven.src.common.PHCrashReport;
import com.playhaven.src.utils.PHStringUtil;

/** This class is a generic view which manages a dictionary of notification renderers and handles the actual rendering.
 * It uses the java reflection api to allow you add classes extending PHNotificationRender so that you can pick which
 * renderer you want. This also allows the content templates to pick which render they want by setting the "type"
 * parameter in the notification data.
 * This notification view is also attached to a 
 * @author samuelstewart
 *
 */
@SuppressWarnings("rawtypes")
public class PHNotificationView extends View implements PHAPIRequest.Delegate {
	private static HashMap<String, Class> renderMap = new HashMap<String, Class>();
	
	private PHNotificationRenderer notificationRenderer;
	
	private JSONObject notificationData;
	
	private PHPublisherMetadataRequest request;
	
	public PHNotificationView(Context context, String placement) {
		super(context);
		request = new PHPublisherMetadataRequest(context, this, placement);
	}
	
	///////////////////////////////////////////////////////////
	
	public void refresh() {
		if (request != null) return;
		
		
		request.send();
	}
	
	public void clear() {
		this.request = null;
		updateNotificationData(null);
	}

	/** Should call this method to update the view, renderer, etc when notification data changes. */
	private void updateNotificationData(JSONObject data) {
		if (data == null) return;
		
		try {
		this.notificationData = data;
		
		//create the renderer based on the server response..
		notificationRenderer = createRenderer(data);
		
		//re-layout and redraw..
		requestLayout();
		
		invalidate();
		} catch (Exception e) { // swallow all exceptions
			PHCrashReport.reportCrash(e, "PHNotificationView - updateNotificationData", PHCrashReport.Urgency.critical);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Rendering Method ///////////////////////////////
	
	/** Creates a renderer based on the "type" field of the server response. Returns null if no renderer exists.*/
	public PHNotificationRenderer createRenderer(JSONObject data) {
		if (renderMap.size() == 0) PHNotificationView.initRenderers();
		
		String type = data.optString("type", "badge");
		
		PHNotificationRenderer renderer = null;
		try {
			Class render_class = renderMap.get(type);
			renderer = (PHNotificationRenderer)render_class.getConstructor(Resources.class).newInstance(getContext().getResources());
		} catch (Exception e) {
			PHCrashReport.reportCrash(e, "PHNotificationView - createRenderer", PHCrashReport.Urgency.critical);
		}
		
		PHStringUtil.log("Created subclass of PHNotificationRenderer of type: "+type);
		return renderer;
		
	}
	/** Adds a renderer class to the map. We use reflection to ensure it extends the PHNotificationRender.*/
	public static void setRenderer(Class renderer, String type) {
		
		Class superclass = renderer.getSuperclass();
		
		if(superclass != PHNotificationRenderer.class)
			throw new IllegalArgumentException("Cannot create a new renderer of type " + type + " because it does not implement the PHNotificationRenderer interface");
		
		renderMap.put(type, renderer);
	}
	
	public static void initRenderers() {
		renderMap.put("badge", PHNotificationBadgeRenderer.class);
	}
	
	////////////////////////////////////////////////////////////////////////
	/////////////////////////////// View override methods //////////////////
	
	@Override
	protected void onDraw(Canvas canvas) {
		try {
		if (notificationRenderer == null) return;
		
		notificationRenderer.draw(canvas, notificationData);
		} catch (Exception e) { // swallow all errors
			PHCrashReport.reportCrash(e, "PHNotificationView - onDraw", PHCrashReport.Urgency.critical);
		}
	}
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		try {
		//TODO: maybe respect parent bounds and check spec? Won't do much really..
		Rect size = new Rect(0, 0, widthSpec, heightSpec);
		if(notificationRenderer != null)
			size = notificationRenderer.size(notificationData);
		
		this.setMeasuredDimension(size.width(), size.height());
		} catch (Exception e) { // swallow all errors
			PHCrashReport.reportCrash(e, "PHNotificationView - onDraw", PHCrashReport.Urgency.critical);
		}
		
		return;
	}
	
	//////////////////////////////////////////////////
	/////////// Request Overrides ////////////////////
	
	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		this.request = null; // request is done
		
		JSONObject notification = responseData.optJSONObject("notification");
		
		if ( ! JSONObject.NULL.equals(notification) &&
			   notification.length() > 0)
			updateNotificationData(notification);
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		this.request = null; // request is done
		updateNotificationData(null);
	}

}
