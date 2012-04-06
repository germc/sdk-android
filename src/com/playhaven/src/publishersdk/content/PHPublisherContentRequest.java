package com.playhaven.src.publishersdk.content;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.publishersdk.content.PHContentView.PHButtonState;

/** Represents a request for an actual advertisement or "content". We handle the close button and actual logistics such as rewards, etc.
 * Each instance makes a request to the server for the content template "data" and then "pushes" (displays) a PHContentView. The PHContentView
 * is an Activity which in turn can display other PHContentView if the content template makes a subrequest, etc. 
 */
public class PHPublisherContentRequest extends PHAPIRequest implements PHContentView.PHContentViewDelegate {
	
	private WeakReference<Context> applicationContext; // should be the main Application context
	
	private WeakReference<Context> activityContext; // should be an activity context
	public boolean isAnimated = true;
	
	private boolean showsOverlayImmediately = false;
	
	public String placement;
	
	private PHContent content;
	
	
	public enum PHRequestState {
		Initialized,
		Preloading,
		Preloaded,
		DisplayingContent,
		Done
	};
	

	
	public enum PHDismissType {
		ContentUnitTriggered, // content template dismissal
		CloseButtonTriggered, // called from close button
		ApplicationTriggered, //application state 
		NoContentTriggered // Usually on error
	};
	
	private PHRequestState state;
	
	private PHRequestState targetState;
	
	/** Big ol' extra delegate methods that a delegate can implement for more detail. {@link PHPublisherContentRequest} will work just
	 * fine with a regular PHAPIRequest delegate but if you want additional detail, pass a {@link PHPublisherContentRequestDelegate}.
	 * It is an abstract class to allow the developer to only override methods they wish. 
	 * 
	 * We break the callbacks into smaller classes to emulate optional interfaces as in iOS (an reference different delegates).
	 */	
	public static interface PHFailureDelegate {
		//these two methods handle the request failing in general, and then the content request failing..
		public void didFail(PHPublisherContentRequest request, String error);
		public void contentDidFail(PHPublisherContentRequest request, Exception e);
	}
	
	public static interface PHCustomizeDelegate {
		public Bitmap closeButton(PHPublisherContentRequest request, PHButtonState state);
		public int borderColor(PHPublisherContentRequest request, PHContent content);
	}
	
	public static interface PHRewardDelegate {
		public void unlockedReward(PHPublisherContentRequest request, PHReward reward);
	}
	
	public static interface PHPurchaseDelegate {
		public void makePurchase(PHPublisherContentRequest request, PHPurchase purchase);
	}

	public static interface PHPublisherContentRequestDelegate extends PHAPIRequest.PHAPIRequestDelegate {
		public void willGetContent(PHPublisherContentRequest request);
		public void willDisplayContent(PHPublisherContentRequest request, PHContent content);
		public void didDisplayContent(PHPublisherContentRequest request, PHContent content);
		public void didDismissContent(PHPublisherContentRequest request, PHDismissType type);
	}
	
	private PHPublisherContentRequestDelegate content_delegate = null;
	
	private PHRewardDelegate reward_delegate = null;
	
	private PHPurchaseDelegate purchase_delegate = null;
	
	private PHCustomizeDelegate customize_delegate = null;
	
	private PHFailureDelegate failure_delegate = null;
	
	/** Set the different content request delegates using the sneaky 'instanceof' operator. It's a bit hacky but works for our purposes.*/
	private void setDelegates(Object delegate) {
		if (delegate instanceof PHRewardDelegate)
			reward_delegate = (PHRewardDelegate)delegate;
		else {
			reward_delegate = null;
			PHConstants.phLog("*** PHRewardDelegate is not implemented. If you are using rewards this needs to be implemented.");
		}

		if (delegate instanceof PHPurchaseDelegate)
			purchase_delegate = (PHPurchaseDelegate)delegate;
		else {
			purchase_delegate = null;
			PHConstants.phLog("*** PHPurchaseDelegate is not implemented. If you are using VGP this needs to be implemented.");
		}

		if (delegate instanceof PHCustomizeDelegate)
			customize_delegate = (PHCustomizeDelegate)delegate; 
		else {
			customize_delegate = null;
			PHConstants.phLog("*** PHCustomizeDelegate is not implemented, using Play Haven close button bitmap. Implement to use own close button bitmap.");
		}
		
		if (delegate instanceof PHFailureDelegate)
			failure_delegate = (PHFailureDelegate)delegate;
		else {
			failure_delegate = null;
			PHConstants.phLog("*** PHFailureDelegate is not implemented. Implement is want to be notified of failed content downloads.");
		}
		
		if (delegate instanceof PHPublisherContentRequestDelegate)
			content_delegate = (PHPublisherContentRequestDelegate)delegate;
		else {
			content_delegate = null;
			PHConstants.phLog("*** PHPublisherContentRequestDelegate is not implemented. Implement is want to be notified of content request states.");
		}

	}
	
	public PHPublisherContentRequest(PHPublisherContentRequestDelegate delegate, Activity activity, String placement) {
		this(delegate, activity);
		
		this.placement = placement;
	}
	
	public PHPublisherContentRequest(PHPublisherContentRequestDelegate  delegate, Activity activity) {
		super(delegate);
		setDelegates(delegate);
		
		isAnimated = true;
		// we use the Application Context to avoid memory leaks
		this.applicationContext = new WeakReference<Context>(activity.getApplicationContext());
		this.activityContext = new WeakReference<Context>(activity);
		
		registerReceiver();
		
		setState(PHRequestState.Initialized);
	}
	
	@Override
	public String baseURL() {
		return PHConstants.phURL("/v3/publisher/content/");
	}
	
	public void setOverlayImmediately(boolean doOverlay) {
		this.showsOverlayImmediately = doOverlay;
	}
	
	public void setState(PHRequestState state) {
		if (this.state == null) this.state = state; //guard against null edge case..
		
		//only set state ahead! (if set above, will just ignore)
		if (state.ordinal() > this.state.ordinal()) {
			this.state = state;
		}
	}
	
	public PHRequestState getState() {
		return state;
	}
	
	//------------------
	// Allows optional content delegate (instead of just PHAPIRequestDelegate)
	//////////////////////////////////////////////////
	
	public static PHPublisherContentRequest getExistingRequest(String token, String secret, String placement) {
		for (PHAPIRequest request : PHAPIRequest.getAllRequests()) {
			if (request instanceof PHPublisherContentRequest) {
				PHPublisherContentRequest content_request = (PHPublisherContentRequest)request;
				
				if (content_request.placement.equals(placement) &&
							content_request.token.equals(token) &&
						  content_request.secret.equals(secret)) {
					
					return content_request;
				
				}
			}
		}
		return null;
	}
	
	
	/////////////////////////////////////////////////

	/////////////////////////////////////////////////
	
	public void preload() {
		targetState = PHRequestState.Preloaded;
		continueLoading();
	}
	
	private void loadContent() {
		setState(PHRequestState.Preloading);
		super.send();
		
		if(content_delegate != null)
			content_delegate.willGetContent(this);
		
		
	}
	
	private void showContent() {
		if (targetState == PHRequestState.DisplayingContent || targetState == PHRequestState.Done) {
			
			if (content_delegate != null)
				content_delegate.willDisplayContent(this, content);
			
			setState(PHRequestState.DisplayingContent);
			
			BitmapDrawable inactive = null;
			BitmapDrawable active = null;
			HashMap<String, Bitmap> customClose = new HashMap<String, Bitmap>();
			if (customize_delegate != null) {
				inactive = new BitmapDrawable(customize_delegate.closeButton(this, PHButtonState.Up));
				active = new BitmapDrawable(customize_delegate.closeButton(this, PHButtonState.Up));

				customClose.put(PHButtonState.Up.name(), inactive.getBitmap());
				customClose.put(PHButtonState.Down.name(), active.getBitmap());
			}
			
			PHContentView.pushContent(content, activityContext.get(), customClose);
		}
	}
	
	private void continueLoading() {
		switch (state) {
			case Initialized:
				loadContent();
				break;
			case Preloaded:
				showContent();
				break;
		}
	}
	
	public boolean getOverlayImmediately() {
		return this.showsOverlayImmediately;
	}
	
	@Override
	public void send() {
		targetState = PHRequestState.DisplayingContent;
		
		//TODO: worry about overlay?
		
		if(content_delegate != null)
			content_delegate.willGetContent(this);
		
		//TODO: show some sort of dialog with close option
		
		continueLoading();
	}
	
	@Override
	public void finish() {
		setState(PHRequestState.Done);
		
		
		//TODO: hide overlay
		//TODO: hide close dialog (see `send()`)
		
		super.finish();
	}
	
	
	/////////////////////////////////////////////////
	///////// PHAPIRequest Override Methods /////////
	@Override
	public HashMap<String, String> getAdditionalParams() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("placement_id", placement);
		if (targetState == PHRequestState.Preloaded) {
			map.put("preload", "1");
		}
		else {
			map.put("preload", "0");
		}

		return map;
	}
	
	@Override
	protected void handleRequestSuccess(JSONObject response) {
		PHConstants.phLog("Main content request succeeded: "+response.toString());
		content = new PHContent();
		boolean success = content.fromJSON(response);
		PHConstants.phLog("Parsing initial content request: "+success);
		
		// we want to call another handler and don't want to call
		// the default success handler on the delegate. (since we still have the 
		// sub-request to make)
		if(success) {
			if(content_delegate != null)
				content_delegate.willDisplayContent(this, content);
			
			setState(PHRequestState.Preloaded);
			
			continueLoading();
		} else {
			delegate.requestFailed(this, new JSONException("Couldn't parse respone into PHContent"));
			
		}
	}

	
	/////////////////////////////////////////////////////////////////////
	/////////////////// Broadcast Routing Methods ///////////////////////
	private void registerReceiver() {
		if (applicationContext.get() != null) {
			applicationContext.get().registerReceiver(new BroadcastReceiver() {
				//TODO: actually pass real arguments back..
				@Override
				public void onReceive(Context context, Intent intent) {
					Bundle extras = intent.getExtras();
					String event = extras.getString(PHContentView.PHBroadcastKey.Event.getKey());
					
					if (event.equals(PHContentView.PHBroadcastEvent.DidShow.getKey())) {
						didShow(null);
					} else if (event.equals(PHContentView.PHBroadcastEvent.DidLoad.getKey())) {
						didLoad(null);
					} else if (event.equals(PHContentView.PHBroadcastEvent.DidDismiss.getKey())) {
						PHDismissType type = PHDismissType.valueOf(extras.getString(PHContentView.PHBroadcastKey.CloseType.getKey()));
						
						didDismiss(null, type);
					} else if (event.equals(PHContentView.PHBroadcastEvent.DidFail.getKey())) {
						String error = extras.getString(PHContentView.PHBroadcastKey.Error.getKey());
						
						didFail(null, error);
					} else if (event.equals(PHContentView.PHBroadcastEvent.DidSendSubrequest.getKey())) {
						String callback = extras.getString(PHContentView.PHBroadcastKey.Callback.getKey());
						String jsonContextStr = extras.getString(PHContentView.PHBroadcastKey.Context.getKey());
						JSONObject jsonContext = null;
						
						try {
							jsonContext = new JSONObject(jsonContextStr);
						} catch (JSONException e) {
							PHConstants.phLog("Could not parse JSON in PHContentView didSendSubrequest callback.");
						}
						
						didSendSubrequest(jsonContext, callback, null);
						
					} else if (event.equals(PHContentView.PHBroadcastEvent.DidUnlockReward.getKey())) {
						PHReward reward = extras.getParcelable(PHContentView.PHBroadcastKey.Reward.getKey());
						
						didUnlockReward(null, reward);
					} else if (event.equals(PHContentView.PHBroadcastEvent.DidMakePurchase.getKey())) {
						PHPurchase purchase = extras.getParcelable(PHContentView.PHBroadcastKey.Purchase.getKey());
						
						didMakePurchase(null, purchase);
					}
					
				}
			}, new IntentFilter(PHContentView.PHBroadcastKey.Action.getKey()));
		}
	}
	
	//////////////////////////////////////////////////////////////////
	//// PHContentView methods (Called from BroadcastReciever) ///////
	
	public void didShow(PHContentView view) {
		// TODO Auto-generated method stub
		
	}


	public void didLoad(PHContentView view) {
		if(content_delegate != null) 
			content_delegate.didDisplayContent(this, view.getContent());
	}

	public void didDismiss(PHContentView view, PHDismissType type) {
		
		if(content_delegate != null) 
			content_delegate.didDismissContent(this, type);
		
	}

	public void didFail(PHContentView view, String error) {
		
		if(failure_delegate != null) 
			failure_delegate.didFail(this, error);
	}

	public void didUnlockReward(PHContentView view, PHReward reward) {
		if (reward_delegate != null) 
			reward_delegate.unlockedReward(this, reward);
		
	}

	public void didMakePurchase(PHContentView view, PHPurchase purchase) {
		if (purchase_delegate != null) 
			purchase_delegate.makePurchase(this, purchase);
		
	}

	public void didSendSubrequest(JSONObject context, String callback,
			PHContentView source) {
		// TODO Auto-generated method stub
		PHConstants.phLog("Did send subrequest!");
		
	}	
}
