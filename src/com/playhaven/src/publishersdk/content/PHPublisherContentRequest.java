package com.playhaven.src.publishersdk.content;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import android.os.SystemClock;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHCrashReport;
import com.playhaven.src.publishersdk.content.PHContentView.PHButtonState;
import com.playhaven.src.utils.PHStringUtil;

/** Represents a request for an actual advertisement or "content". We handle the close button and actual logistics such as rewards, etc.
 * Each instance makes a request to the server for the content template "data" and then "pushes" (displays) a PHContentView. The PHContentView
 * is an Activity which in turn can display other PHContentView if the content template makes a subrequest, etc. 
 */
public class PHPublisherContentRequest extends PHAPIRequest implements PHContentView.Delegate {
	
	private WeakReference<Context> applicationContext; // should be the main Application context
	
	private WeakReference<Context> activityContext; // should be an activity context
		
	private boolean showsOverlayImmediately = false;
	
	public String placement;
	
	private PHContent content;
	
	public String contentTag; 
	
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
		NoContentTriggered    // Usually on error
	};
	
	private PHRequestState state;
	
	//private BroadcastReceiver contentBroadcastReceiver = null;

	private PHRequestState targetState;
	
	/** Big ol' extra delegate methods that a delegate can implement for more detail. {@link PHPublisherContentRequest} will work just
	 * fine with a regular PHAPIRequest delegate but if you want additional detail, pass a {@link ContentDelegate}.
	 * It is an abstract class to allow the developer to only override methods they wish. 
	 * 
	 * We break the callbacks into smaller classes to emulate optional interfaces as in iOS (an reference different delegates).
	 */	
	public static interface FailureDelegate {
		//these two methods handle the request failing in general, and then the content request failing..
		public void didFail(PHPublisherContentRequest request, String error);
		public void contentDidFail(PHPublisherContentRequest request, Exception e);
	}
	
	public static interface CustomizeDelegate {
		public Bitmap closeButton(PHPublisherContentRequest request, PHButtonState state);
		public int borderColor(PHPublisherContentRequest request, PHContent content);
	}
	
	public static interface RewardDelegate {
		public void unlockedReward(PHPublisherContentRequest request, PHReward reward);
	}
	
	public static interface PurchaseDelegate {
		public void makePurchase(PHPublisherContentRequest request, PHPurchase purchase);
	}

	public static interface ContentDelegate extends PHAPIRequest.Delegate {
		public void willGetContent(PHPublisherContentRequest request);
		public void willDisplayContent(PHPublisherContentRequest request, PHContent content);
		public void didDisplayContent(PHPublisherContentRequest request, PHContent content);
		public void didDismissContent(PHPublisherContentRequest request, PHDismissType type);
	}
	
	public ContentDelegate content_delegate = null;
	
	public RewardDelegate reward_delegate = null;
	
	public PurchaseDelegate purchase_delegate = null;
	
	public CustomizeDelegate customize_delegate = null;
	
	public FailureDelegate failure_delegate = null;
	
	private static ConcurrentLinkedQueue<Long> dismissedStamps = new ConcurrentLinkedQueue<Long>(); // time stamps for dismissal
	
	/** Checks to see if we have dismissed a content view within
	 * the given number of milliseconds.
	 * @return
	 */
	public static boolean didDismissContentWithin(long range) {
		
		long curTime = SystemClock.uptimeMillis();
		long stampTime = 0;
		
		// start at the oldest and throw out any which are too old
		while (curTime - stampTime > range && dismissedStamps.size() > 0) {
			stampTime = dismissedStamps.poll();
		}
		
		return (curTime - stampTime <= range ? true : false);
	}
	
	/** Utility method for PHContentView to log a dismiss*/
	public static void dismissedContent() {
		dismissedStamps.add(SystemClock.uptimeMillis());
	}
	
	/** Set the different content request delegates using the sneaky 'instanceof' operator. It's a bit hacky but works for our purposes.*/
	private void setDelegates(Object delegate) {
		
		if (delegate instanceof RewardDelegate)
			reward_delegate = (RewardDelegate)delegate;
		else {
			reward_delegate = null;
			PHStringUtil.log("*** RewardDelegate is not implemented. If you are using rewards this needs to be implemented.");
		}

		if (delegate instanceof PurchaseDelegate)
			purchase_delegate = (PurchaseDelegate)delegate;
		else {
			purchase_delegate = null;
			PHStringUtil.log("*** PurchaseDelegate is not implemented. If you are using VGP this needs to be implemented.");
		}

		if (delegate instanceof CustomizeDelegate)
			customize_delegate = (CustomizeDelegate)delegate; 
		else {
			customize_delegate = null;
			PHStringUtil.log("*** CustomizeDelegate is not implemented, using Play Haven close button bitmap. Implement to use own close button bitmap.");
		}
		
		if (delegate instanceof FailureDelegate)
			failure_delegate = (FailureDelegate)delegate;
		else {
			failure_delegate = null;
			PHStringUtil.log("*** FailureDelegate is not implemented. Implement if want to be notified of failed content downloads.");
		}
		
		if (delegate instanceof ContentDelegate)
			content_delegate = (ContentDelegate)delegate;
		else {
			content_delegate = null;
			PHStringUtil.log("*** ContentDelegate is not implemented. Implement if want to be notified of content request states.");
		}

	}
	
	public PHPublisherContentRequest(Activity activity, String placement) {
		super(activity);
		
		this.placement = placement;
		
		// we use the Application Context to avoid memory leaks
		this.applicationContext = new WeakReference<Context>(activity.getApplicationContext());
		this.activityContext = new WeakReference<Context>(activity);
		
		registerReceiver();
		
		setState(PHRequestState.Initialized);
	}
	
	public PHPublisherContentRequest(Activity activity, ContentDelegate delegate, String placement) {
		this(activity, placement);
		
		this.delegate = delegate;
		setDelegates(delegate);
	}
	
	@Override
	public String baseURL() {
		return super.createAPIURL("/v3/publisher/content/");
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
	
	
	/////////////////////////////////////////////////
	/////////////////////////////////////////////////
	
	public void preload() {
		targetState = PHRequestState.Preloaded;
		continueLoading();
	}
	
	private void loadContent() {
		setState(PHRequestState.Preloading);
		super.send(); // now actually send the request
		
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
			
			String tag = "PHContentView: " + this.hashCode(); // generate a unique tag for the PHContentView 
			
			// TODO: not sure the content view should do its own pushing?
			contentTag = PHContentView.pushContent(content, activityContext.get(), customClose, tag);

			if(content_delegate != null) 
				content_delegate.didDisplayContent(this, content);
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
		try {
		targetState = PHRequestState.DisplayingContent;
		
		if(content_delegate != null)
			content_delegate.willGetContent(this);
		
		continueLoading();
		
		} catch(Exception e) { // swallow all exceptions
			PHCrashReport.reportCrash(e, "PHPublisherContentRequest - send", PHCrashReport.Urgency.critical);
		}
	}
	
	@Override
	public void finish() {
		setState(PHRequestState.Done);
		
		super.finish();
	}
	
	
	/////////////////////////////////////////////////
	///////// PHAPIRequest Override Methods /////////
	@Override
	public Hashtable<String, String> getAdditionalParams() {
		Hashtable<String, String> table = new Hashtable<String, String>();
		
		table.put("placement_id", (placement != null ? placement : ""));
		table.put("preload", (targetState == PHRequestState.Preloaded ? "1" : "0"));
	
		return table;
	}
	
	@Override
	protected void handleRequestSuccess(JSONObject response) {
		content = new PHContent(response);
		
		setState(PHRequestState.Preloaded);
		
		continueLoading();
	}
	
	/////////////////////////////////////////////////////////////////////
	/////////////////// Broadcast Routing Methods ///////////////////////
	private void registerReceiver() {
		
		if (applicationContext.get() != null) {
			// TODO: not sure if I like this at all. We need to clean this up big time
				applicationContext.get().registerReceiver(new BroadcastReceiver() {
					
					@Override
					public void onReceive(Context context, Intent intent) {
						Bundle extras = intent.getExtras();
						String event  = extras.getString(PHContentView.PHBroadcastKey.Event.getKey());
						String tag    = extras.getString(PHContentView.PHBroadcastKey.Tag.getKey());
						
						if ( ! tag.equals(contentTag)) return; // only process if it is relevant to us
						
						if (event.equals(PHContentView.PHBroadcastEvent.DidShow.getKey()))
							didShow();
						
						else if (event.equals(PHContentView.PHBroadcastEvent.DidLoad.getKey()))
							didLoad();
						
						else if (event.equals(PHContentView.PHBroadcastEvent.DidDismiss.getKey())) {
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
								PHStringUtil.log("Could not parse JSON in PHContentView didSendSubrequest callback.");
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
		//}
	}
	
	//////////////////////////////////////////////////////////////////
	//// PHContentView methods (Called from BroadcastReciever) ///////
	
	public void didShow() {

	}


	public void didLoad() {
		
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

	public void didSendSubrequest(JSONObject context, String callback, PHContentView source) {
		// pass
	}

	@Override
	public void didDismiss(PHDismissType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didFail(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didUnlockReward(PHReward reward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didMakePurchase(PHPurchase purchase) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didSendSubrequest(JSONObject context, String callbace) {
		// TODO Auto-generated method stub
		
	}	
}
