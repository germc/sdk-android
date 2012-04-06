package com.playhaven.src.publishersdk.metadata;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import com.playhaven.src.additions.ObjectExtensions;
import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;

/** This class is a generic view which manages a dictionary of notification renderers and handles the actual rendering.
 * It uses the java reflection api to allow you add classes extending PHNotificationRender so that you can pick which
 * renderer you want. This also allows the content templates to pick which render they want by setting the "type"
 * parameter in the notification data.
 * This notification view is also attached to a 
 * @author samuelstewart
 *
 */
@SuppressWarnings("rawtypes")
public class PHNotificationView extends View implements PHAPIRequest.PHAPIRequestDelegate {
	private static HashMap<String, Class> renderMap = new HashMap<String, Class>();
	
	private PHNotificationRenderer notificationRenderer;
	
	private String placement;
	
	private String token;
	
	private String secret;
	
	private JSONObject notificationData;
	
	private PHPublisherMetadataRequest request;
	
	public PHNotificationView(Context context, String placement) {
		 super(context);
		 this.token = PHConstants.getToken();
		 this.secret = PHConstants.getSecret();
		 
		 this.placement = placement;
	}
	
	public PHNotificationView(Context context, String token, String secret, String placement) {
		super(context);
		this.token = token;
		this.secret = secret;
		this.placement = placement;
	}
	
	//Section: Request methods
	//======================================
	public void refresh() {
		if (request != null) return;
		
		request = new PHPublisherMetadataRequest(this, token, secret, placement);
		request.send();
	}
	
	public void clear() {
		this.request = null;
		updateNotificationData(null);
	}
	
	public void test() {

		JSONObject TestingNotificationData = new JSONObject();
		try {
			TestingNotificationData.putOpt("badge", "type");
			TestingNotificationData.putOpt("1", "value");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    	this.notificationData = TestingNotificationData;		 
	}
	/** Should call this method to update the view, renderer, etc when notification data changes.
	 * Simulates iOS observation NSNotification/KVO center.
	 */
	private void updateNotificationData(JSONObject data) {
		this.notificationData = data;
		
		//create the renderer based on the server response..
		notificationRenderer = createRenderer(data);
		
		//re-layout and redraw..
		requestLayout();
		
		invalidate();
	}
	
	//////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Rendering Method ///////////////////////////////
	
	/** Creates a renderer based on the "type" field of the server response. Returns null if no renderer exists.*/
	public PHNotificationRenderer createRenderer(JSONObject data) {
		if (renderMap.size() == 0) PHNotificationView.initRenderers();
		
		String type = ObjectExtensions.JSONExtensions.getJSONString(data, "type");
		if(type == null || type.equals("")) type = "badge"; //(rely on short-circuit) default to badge renderer if none available..
		
		Class render_class = renderMap.get(type);
		
		PHNotificationRenderer renderer = null;
		try {
			renderer = (PHNotificationRenderer)render_class.getConstructor(Resources.class).newInstance(getContext().getResources());
		} catch (Exception e) {
			e.printStackTrace();
		}
		PHConstants.phLog("New renderer: "+renderer);
		return renderer;
		
	}
	/** Adds a renderer class to the map. We use reflection to ensure it extends the PHNotificationRender.*/
	public static void setRenderer(Class renderer, String type) {
		Class superclass = renderer.getSuperclass();
		if(superclass != PHNotificationRenderer.class) {
			PHConstants.phLog("Renderer needs to extend PHNotificationRenderer");
			return;
		}
		renderMap.put(type, renderer);
	}
	
	public static void initRenderers() {
		renderMap.put("badge", PHNotificationBadgeRenderer.class);
	}
	
	////////////////////////////////////////////////////////////////////////
	/////////////////////////////// View override methods //////////////////
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (notificationRenderer == null) return;
		
		notificationRenderer.draw(canvas, notificationData);
	}
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		//TODO: maybe respect parent bounds and check spec? Won't do much really..
		Rect size = new Rect(0,0,widthSpec,heightSpec);
		if(notificationRenderer != null) {
			size = notificationRenderer.size(notificationData);
		}
		
		this.setMeasuredDimension(size.width(), size.height());
		return;
	}
	
	//Section: Request override methods..
	//======================================
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		this.request = null;
		PHConstants.phLog("Metadata request succeeded: "+responseData);
		updateNotificationData(ObjectExtensions.JSONExtensions.getJSONObject(responseData, "notification"));
		PHConstants.phLog("Notification data: "+notificationData);
	}

	public void requestFailed(PHAPIRequest request, Exception e) {
		this.request = null;
		updateNotificationData(null);
	}
}
