package com.playhaven.src.publishersdk.content;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.playhaven.resources.PHResourceManager;
import com.playhaven.resources.files.PHCloseActiveImageResource;
import com.playhaven.resources.files.PHCloseImageResource;
import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConfig;
import com.playhaven.src.common.PHCrashReport;
import com.playhaven.src.common.PHURLLoader;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest.PHDismissType;
import com.playhaven.src.publishersdk.purchases.PHPublisherIAPTrackingRequest;
import com.playhaven.src.utils.PHConversionUtils;
import com.playhaven.src.utils.PHStringUtil;
import com.samstewart.hadaly.Selector;
import com.samstewart.hadaly.jstest.UITest;

/**
 * A separate activity for actually displaying the content after the
 * PHPublisherContentRequest has finished loading. When starting this activity,
 * you should attach a "content" via "putExtra" using the CONTENT_START_KEY.
 * 
 * @author samstewart
 * 
 */
public class PHContentView extends Activity implements PHURLLoader.PHURLLoaderDelegate, 
													   PHAPIRequest.Delegate {

	private String tag;
	
	public PHContent content;

	public boolean showsOverlayImmediately;

	private View overlayView;
	
	private RelativeLayout rootView;
	
	private WebView webview;
	
	private boolean isBackBtnCancelable;
	
	private boolean isTouchCancelable;
  
    private final String JAVASCRIPT_CALLBACK_TEMPLATE 	  = "javascript:PlayHaven.nativeAPI.callback(\"%s\", %s, %s)";

    private final String JAVASCRIPT_SET_PROTOCOL_TEMPLATE = "javascript:window.PlayHavenDispatchProtocolVersion = %d";
    
	///////////////////////////////////////////////////////////////
	//////////////////////// Broadcast Constants //////////////////
	
	// argument keys for starting PHContentView Activity
	public static enum PHContentViewArgument {
		CustomCloseBtn	("custom_close"),
		Content			("init_content_contentview"),
		Tag				("content_tag");
		
		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHContentViewArgument(String key) {
			this.key = key;
		}
	}
	
	public static enum PHBroadcastEvent {
		DidShow					("didShow"),
		DidLoad					("didLoad"),
		DidDismiss				("didDismiss"),
		DidFail					("didFail"),
		DidUnlockReward			("didUnlockReward"),
		DidMakePurchase			("didMakePurchase"),
		DidSendSubrequest		("didSendSubrequest");

		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHBroadcastEvent(String key) {
			this.key = key;
		}
	}

	// general broadcast keys
	public static enum PHBroadcastKey {
		Action		("com.playhaven.src.publishersdk.content.PHContentViewEvent"),
		Event		("event_contentview"),
		CloseType	("closetype_contentview"),
		Callback	("callback_contentview"),
		Context		("context_contentview"),
		Error		("error_contentview"),
		Reward		("reward_contentview"),
		Purchase	("purchase_contentview"),
		Tag			("content_tag");
		
		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHBroadcastKey(String key) {
			this.key = key;
		}
	}

	public static enum PHBroadcastReceiverEvent {
		ContentViewsPurchaseSendCallback	("contentViewsPurchaseSendCallback"),
		PurchaseResolution					("purchaseResolution");

		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHBroadcastReceiverEvent(String key) {
			this.key = key;
		}
	}

	public static enum PHBroadcastReceiverKey {
		Action	("com.playhaven.src.publishersdk.content.PHContentViewReceiverEvent"),
		Event	("event_report_purchase_resolution");
		
		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHBroadcastReceiverKey(String key) {
			this.key = key;
		}
	}

	private PHRequestRouter mRouter;
	
	// maximum margin for close button
	private final float CLOSE_MARGIN = 10.0f;

	private static final int CLOSE_BTN_TIMEOUT = 4000; // in milliseconds

	private Handler closeBtnDelay;

	private Runnable closeBtnDelayRunnable;

	private ImageButton closeBtn;

	public enum PHRewardKey {
		IDKey			("reward"), 
		QuantityKey		("quantity"), 
		ReceiptKey		("receipt"), 
		SignatureKey	("signature");

		private final String keyName;

		private PHRewardKey(String key) {
			this.keyName = key; // one time assignment
		}

		public String key() {
			return keyName;
		}
	};

	public enum PHPurchaseKey {
		ProductIDKey	("product"),
		NameKey			("name"),
		QuantityKey		("quantity"),
		ReceiptKey		("receipt"),
		SignatureKey	("signature"),
		CookieKey		("cookie");
		
		private final String keyName;
		
		private PHPurchaseKey(String key) {
			this.keyName = key; //one time assignment
		}
		
		public String key() {
			return keyName;
		}
	};

	public static enum PHButtonState {
		Down(android.R.attr.state_pressed),
		Up(android.R.attr.state_enabled);
		
		private int android_state;
		
		private PHButtonState(int android_state) {
			this.android_state = android_state;
		}
		
		public int getAndroidState() {
			return this.android_state;
		}
		
	};
	
	private HashMap<String, Bitmap> customCloseStates = new HashMap<String, Bitmap>();
	
	/**
	 * Extends WebChromeClient just for logging purposes. (have to if greater
	 * than Android 2.1)
	 */
	private class PHWebViewChrome extends WebChromeClient {
		
		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
			String fname = Uri.parse(consoleMessage.sourceId())
					.getLastPathSegment();
			PHStringUtil.log("Javascript: " + consoleMessage.message()
					+ " at line (" + fname + ") :"
					+ consoleMessage.lineNumber());
			return true;
		}
	}

	/**
	 * Our own personal webviewclient so that we can handle the callbacks
	 * (delegate calls). We have to be careful about the implicit inner
	 * reference to the enclosing context.
	 */
	private class PHWebViewClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView webview, String url) {
			try {
			/*
			 * TODO: make sure that we eventually call the callback Issue:
			 * webview.loadUrl forks another page request if(delegate != null)
			 * delegate.didLoad(PHContentView.this);
			 */
			if (delegate != null)
				delegate.didLoad();
			
			} catch (Exception e) { // swallow all exceptions
				PHCrashReport.reportCrash(e, "PHWebViewClient - onPageFinished()", PHCrashReport.Urgency.critical);
			}
		}

		@Override
		public void onLoadResource(WebView view, String url) { 
			/*
			 * Note: We have to listen in both 'onLoadResource' and 'shouldOverrideUrlLoading'
			 * because Android does not call 'shouldOverrideUrlLoading' after the initial 
			 * iframe 'src' parameter is set. Since we use iframes for making the request
			 * to the native SDK, this is somewhat problematic. We need iframes to ensure 
			 * the timers in the content template are respected so modification was not an option.
			 * However, to avoid slowdown, we filter for "ph://" urls here since *every* resource
			 * request is routed through this point. Here's to hoping the Android dev team fixes this!
			 * 
			 * Most of the requests appear to route through the shouldOverrieUrlLoading
			 * handler. However, requests such as the ph://closeButton only route
			 * through the onLoadResource handler. We avoid "double intercepting" the requests
			 * routed through shouldOverrideUrlLoading because they were 'overridden'.
			 */
			
			if (url.startsWith("ph://")) routePlayhavenCallback(url);
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			
			// just blank out the webview
			view.loadUrl("");
			
			PHCrashReport.reportCrash(new RuntimeException(description), "PHWebViewClient - onRecievedError", PHCrashReport.Urgency.low);
			
			PHStringUtil.log(String.format("Error loading template at url: %s Code: %d Description: %s", failingUrl, errorCode, description));
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView webview, String url) {
			return routePlayhavenCallback(url);
		}
		
		private boolean routePlayhavenCallback(String url) {
			PHStringUtil.log("Received webview callback: " + url);
			
			try {
				if (mRouter.hasRoute(url)) {
					mRouter.route(url);
					return true;
				}
					
			} catch (Exception e) { // swallow all errors
				PHCrashReport.reportCrash(e, "PHWebViewClient - url routing", PHCrashReport.Urgency.critical);
			}
			
			return false;
		}
	}

	/**
	 * This used to public, but now we simply use it for piping internally to
	 * broadcast. In the future, if other components besides {@see
	 * PHPublisherContentRequest} need to utilize this class we can make this
	 * delegate public.
	 */
	private Delegate delegate;

	///////////////////////////////////////////////////////////
	//////////////// Broadcast (Delegate) /////////////////////

	public static interface Delegate {
		public void didShow();

		public void didLoad();

		public void didDismiss(PHDismissType type);

		public void didFail(String error);

		public void didUnlockReward(PHReward reward);

		public void didMakePurchase(PHPurchase purchase);

		public void didSendSubrequest(JSONObject context, String callbace);
	}

	/**
	 * Adapter bridge between actual code and Manager. Since we are an
	 * activity, we cannot simply call delegate methods directly.
	 * TODO: This is just plain stupid. Far overkill, we should just be sending the broadcasts ourself.
	 */
	public class PHContentViewDelegateBroadcaster implements
			Delegate {
		private void sendStateUpdate(String state) {
			sendBroadcast(createBaseIntent(state));
		}

		private Intent createBaseIntent(String event) {
			Intent intent = new Intent(PHBroadcastKey.Action.getKey());

			intent.putExtra(PHBroadcastKey.Tag.getKey(), tag); // originally created by 'pushContent'
			intent.putExtra(PHBroadcastKey.Event.getKey(), event);
			return intent;
		}

		public void didUnlockReward(PHReward reward) {
			Intent intent = createBaseIntent(PHBroadcastEvent.DidUnlockReward.getKey());
			intent.putExtra(PHBroadcastKey.Reward.getKey(), reward);

			sendBroadcast(intent);
		}

		public void didMakePurchase(PHPurchase purchase) {
			
			Intent intent = createBaseIntent(PHBroadcastEvent.DidMakePurchase.getKey());
			intent.putExtra(PHBroadcastKey.Purchase.getKey(), purchase);

			sendBroadcast(intent);
		}

		public void didSendSubrequest(JSONObject context, String callback) {
			Intent intent = createBaseIntent(PHBroadcastEvent.DidSendSubrequest.getKey());
			intent.putExtra(PHBroadcastKey.Context.getKey(), context.toString());
			intent.putExtra(PHBroadcastKey.Callback.getKey(), callback);
			intent.putExtra(PHBroadcastKey.Tag.getKey(), tag);

			sendBroadcast(intent);
		}

		public void didShow() {
			sendStateUpdate(PHBroadcastEvent.DidShow.getKey());
		}

		public void didLoad() {
			//sendStateUpdate("didLoad");
			sendStateUpdate(PHBroadcastEvent.DidLoad.getKey());
		}

		public void didDismiss(PHDismissType type) {
			Intent intent = createBaseIntent(PHBroadcastEvent.DidDismiss.getKey());
			intent.putExtra(PHBroadcastKey.CloseType.getKey(), type.name());
			
			sendBroadcast(intent);
		}

		public void didFail(String error) {
			Intent intent = createBaseIntent(PHBroadcastEvent.DidFail.getKey());
			intent.putExtra(PHBroadcastKey.Error.getKey(), error);

			sendBroadcast(intent);
		}
	}

	//TODO: this probably should be cleaned up and checked..
	// Yes, this is garbage, we shouldn't be listening for this sort of nonsense
	private void registerBroadcastReceiver() {
		if (getApplicationContext() != null) {
			getApplicationContext().registerReceiver(new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					try {
					Bundle extras = intent.getExtras();
					String event = extras.getString(PHContentView.PHBroadcastReceiverKey.Event.getKey());
					String resolution = extras.getString(PHContentView.PHBroadcastReceiverEvent.PurchaseResolution.getKey());
					
					if (event.equals(PHContentView.PHBroadcastReceiverEvent.ContentViewsPurchaseSendCallback.getKey())) {
					    JSONObject purchaseResolutionDict = null;
						try {
							purchaseResolutionDict = new JSONObject(resolution);
							contentViewsPurchaseSendCallback(purchaseResolutionDict);
						} catch (JSONException e) {
							PHCrashReport.reportCrash(e, "PHContentView registerBroadcastReciever", PHCrashReport.Urgency.low);
							e.printStackTrace();
						}
					}					
					} catch (Exception e) { // swallow all exceptions
						PHCrashReport.reportCrash(e, "PHContentView registerBroadcastReciever", PHCrashReport.Urgency.critical);
					}
				}
			}, new IntentFilter(PHContentView.PHBroadcastReceiverKey.Action.getKey()));
		}
	}

	/////////////////////////////////////////////////
	///// PHPurchase BroadcastReceiver method ///////
	public void contentViewsPurchaseSendCallback(JSONObject response) {			
		sendCallback(response.optString    ( "callback" ),
					 response.optJSONObject( "response" ),
					 response.optJSONObject( "error"    ));

	}

	/////////////////////////////////////////////////
	/////////////////// Close Button ////////////////

	/** Creates the {@see closeBtn} if it doesn't already exist and returns. */
	private ImageButton getCloseBtn() {
		if (closeBtn == null) {

			StateListDrawable states = new StateListDrawable();

			BitmapDrawable inactive = (customCloseStates.get(PHButtonState.Up.name()) != null ? new BitmapDrawable(customCloseStates.get(PHButtonState.Up.name())) : null);
			BitmapDrawable active = (customCloseStates.get(PHButtonState.Down.name()) != null ? new BitmapDrawable(customCloseStates.get(PHButtonState.Down.name())) : null);

			if (inactive == null) {
				PHCloseImageResource inactiveRes = (PHCloseImageResource) PHResourceManager
						.sharedResourceManager().getResource("close_inactive");
				inactive = new BitmapDrawable(inactiveRes.loadImage(PHConfig.screen_density_type));
			}

			if (active == null) {
				PHCloseActiveImageResource active_res = (PHCloseActiveImageResource) PHResourceManager
						.sharedResourceManager().getResource("close_active");
				active = new BitmapDrawable(active_res.loadImage(PHConfig.screen_density_type));
			}

			states.addState(new int[] { PHButtonState.Down.getAndroidState() },
					active);
			states.addState(new int[] { PHButtonState.Up.getAndroidState() },
					inactive);

			closeBtn = new ImageButton(this);
			closeBtn.setVisibility(View.INVISIBLE); // not View.GONE to ensure
													// proper layout

			// TODO: perhaps set the layout params (WRAP_CONTENT) ourselves to
			// ensure consistency?

			closeBtn.setBackgroundDrawable(null); // make transparent
			closeBtn.setImageDrawable(states);

			// TODO: should be handled by density scaling?
			// set scaling method
			closeBtn.setScaleType(ImageView.ScaleType.FIT_XY);

			closeBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						buttonDismiss();
					} catch (Exception e) { // 
						PHCrashReport.reportCrash(e, "closeBtn - onClick", PHCrashReport.Urgency.critical);
					}
				}
			});

		}
		return closeBtn;
	}

	private void buttonDismiss() {
		PHStringUtil.log("User dismissed " + this.toString());
		
		if (delegate != null) {
			delegate.didDismiss(PHDismissType.CloseButtonTriggered);
			delegate = null;
		}

		super.finish();
	}

	/** Places the close button onscreen. */
	private void placeCloseButton() {
		// TODO: register for orientation notifications to re-adjust
		// positioning..

		// unlike the iOS SDK, we use margins since we use the
		// ALIGN_PARENT_RIGHT property
		float marginRight = 0;
		float marginTop = 0;

		// no need to worry about orientation as on iOS since we use fluid
		// ALIGN_PARENT_RIGHT

		marginRight = PHConversionUtils.dipToPixels(CLOSE_MARGIN);
		marginTop   = PHConversionUtils.dipToPixels(CLOSE_MARGIN);


		ImageButton close = getCloseBtn();
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
											RelativeLayout.LayoutParams.WRAP_CONTENT,
											RelativeLayout.LayoutParams.WRAP_CONTENT);
				
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		// TODO: convert center x,y coordinates to padding
		params.setMargins(0, (int) marginTop, (int) marginRight, 0);
		close.setLayoutParams(params);

		// TODO: rotate image since user may provide a custom image.

		// make sure close btn isn't attached
		if (close.getParent() != null) {
			ViewGroup detailParent = (ViewGroup) close.getParent();
			detailParent.removeView(close);
		}

		getRootView().addView(close);
	}

	/** Displays button after timeout. */
	private void showCloseAfterTimeout() {
		if (closeBtnDelay == null)
			closeBtnDelay = new Handler();

		// we'll need this reference for canceling
		closeBtnDelayRunnable = new Runnable() {
			public void run() {
				showCloseButton();
			}
		};
		
		closeBtnDelay.postDelayed(closeBtnDelayRunnable, CLOSE_BTN_TIMEOUT);
	}

	/**
	 * Display the close button. Usually called after a timeout if the content
	 * view doesn't show it.
	 */
	private void showCloseButton() {
		// check for null since we are called from showCloseAfterTimeout and we
		// may not be around.
		if (closeBtn != null)
			closeBtn.setVisibility(View.VISIBLE);
	}

	private void hideCloseButton() {
		if (closeBtnDelay != null)
			closeBtnDelay.removeCallbacks(closeBtnDelayRunnable);
		// TODO: stop worrying about notifications
		closeBtn.setVisibility(View.GONE);

	}

	////////////////////////////////////////////////////////
	//////////////// Activity Management////////////////////
	
	public static String pushContent(PHContent content, Context context, HashMap<String, Bitmap> customCloseImages, String tag) {
		if (context != null) {
			Intent contentViewIntent = new Intent(context, PHContentView.class);
			
			// attach the content object
			contentViewIntent.putExtra(PHContentView.PHContentViewArgument.Content.getKey(), content);
			
			// attach custom closeImages
			if (customCloseImages != null && customCloseImages.size() > 0)
				contentViewIntent.putExtra(PHContentView.PHContentViewArgument.CustomCloseBtn.getKey(), customCloseImages);
			
			contentViewIntent.putExtra(PHContentView.PHContentViewArgument.Tag.getKey(), tag);
			
			context.startActivity(contentViewIntent);
			
			return tag;
		}
		
		return null;
	}
	
	////////////////////////////////////////////////////////
	//////////////// Activity Callbacks ////////////////////

	@Override
	public void onBackPressed() {
		if (this.getIsBackBtnCancelable()) {
			PHStringUtil.log("The content unit was dismissed by the user using back button");
			
			if (delegate != null) {
				delegate.didDismiss(PHDismissType.CloseButtonTriggered);
				delegate = null;
			}

			super.onBackPressed();
		}

		// otherwise, simply consume and ignore..
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		PHPublisherContentRequest.dismissedContent(); // log that PHContentView is no longer visible
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (webview != null) {
			// make sure we don't leak any memory
			webview.setWebChromeClient(null);
			webview.setWebViewClient(null);
		}

		// stop close button
		if (closeBtnDelay != null)
			closeBtnDelay.removeCallbacks(closeBtnDelayRunnable);

		hideCloseButton();
	}
	
	private void setupWebviewRoutes() {
		mRouter.addRoute("ph://dismiss", new Runnable() {
			@Override
			public void run() {
				
				handleDismiss();
				
			}
		});
		
		mRouter.addRoute("ph://launch", new Runnable() {
			@Override
			public void run() {
				
				handleLaunch();
				
			}
		});
		
		mRouter.addRoute("ph://loadContext", new Runnable() {
			@Override
			public void run() {
				
				handleLoadContext();
				
			}
		});
		
		mRouter.addRoute("ph://reward", new Runnable() {
			@Override
			public void run() {
				
				handleRewards();
				
			}
		});
		
		mRouter.addRoute("ph://purchase", new Runnable() {
			@Override
			public void run() {
				
				handlePurchases();
				
			}
		});
		
		mRouter.addRoute("ph://subcontent", new Runnable() {
			@Override
			public void run() {
				
				handleSubrequest();
				
			}
		});
		
		mRouter.addRoute("ph://closeButton", new Runnable() {
			@Override
			public void run() {
				
				handleCloseButton();
				
			}
		});
	}
	

	/**
	 * 
	 * Note; If the Activity doesn't have a frame we must dismiss
	 * from here or the system won't honor it.
	 */
	@Override
	public void onAttachedToWindow() {
		try {
		super.onAttachedToWindow();

		// we can only dismiss once the window is visible..
		if (!hasOrientationFrame()) {
			dismiss("The content you requested was not able to be shown because it is missing required orientation data.");
			return;
		}
		
		// make window transparent
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		} catch (Exception e) {
			PHCrashReport.reportCrash(e, "PHContentView - onAttachedToWindow()", PHCrashReport.Urgency.critical);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		try {
		super.onCreate(savedInstanceState);
		
		mRouter = new PHRequestRouter();
		
		// create Broadcast delegate
		delegate = new PHContentViewDelegateBroadcaster();

		// load content from intent
		content = getIntent().getParcelableExtra(PHContentViewArgument.Content.getKey());
		
		// tag to send back to PHPublisherContentRequest
		tag = getIntent().getStringExtra(PHContentViewArgument.Tag.getKey());
		
		if (getIntent().hasExtra(PHContentViewArgument.CustomCloseBtn.getKey())) {
			customCloseStates = (HashMap<String, Bitmap>)getIntent().getSerializableExtra(PHContentViewArgument.CustomCloseBtn.getKey());
		}
		
		// by default, we are cancelable only via back button (false=touch, back btn=true)
		this.setCancelable(false, true);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		setupWebviewRoutes();
		
		} catch (Exception e) { // swallow all exceptions...
			PHCrashReport.reportCrash(e, PHCrashReport.Urgency.critical);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent evt) {
		//DEBUG ONLY!
		PHStringUtil.log("User touched content view: = new PointF("+evt.getX()+"f, "+evt.getY()+"f);");
		super.dispatchTouchEvent(evt);
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		try {
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			if (this.getIsTouchCancelable()) finish();
			return true;
		}
		} catch (Exception e) {
			PHCrashReport.reportCrash(e, "PHContentView - onTouchEvent()", PHCrashReport.Urgency.critical);
		}
		return false;
	}
	///////////////////// Accessors ////////////////////////
	///////////////////////////////////////////////////////

	public void setCancelable(boolean touchCancel, boolean backCancel) {
		this.isTouchCancelable = touchCancel;
		this.isBackBtnCancelable = backCancel;
	}

	public boolean setIsBackBtnCancelable(boolean backCancel) {
		return this.isBackBtnCancelable = backCancel;
	}

	public boolean getIsTouchCancelable() {
		return this.isTouchCancelable;
	}
	
	public boolean getIsBackBtnCancelable() {
		return this.isBackBtnCancelable;
	}

	public PHContent getContent() {
		return content;
	}

	public RelativeLayout getRootView() {
		return rootView;
	}

	public void setContent(PHContent content) {
		if (content != null) {
			this.content = content;
		}
	}

	
	///////////////////////////////////////////////////////////////
	//////////// Overlay and Close button methods /////////////////
	
	/**
	 * Returns the overlay view if it exists, otherwise we create a default
	 * overlay view.
	 */
	public View getOverlayView() {
		if (overlayView == null) {

		}
		return overlayView;
	}

	public void setOverlayView(View overlay) {
		this.overlayView = overlay;
	}


	/**
	 * Provide content view immediately so that we can place close button (even
	 * if we dismiss in onAttachedToWindow()). TODO: is this the best place for
	 * this code? Perhaps it should be moved to `onAttachedToWindow` or
	 * `onCreate`?
	 */
	@Override
	public void onStart() {
		super.onStart();
		try {
		rootView = new RelativeLayout(getApplicationContext());
		
		setContentView(rootView, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT));

		sizeToOrientation(); // must be called *after* setContentView

		webview = new WebView(this);
		
		webview.setBackgroundColor(Color.TRANSPARENT);

		setWebViewCachingDisabled( ! PHConfig.cache);
		
		//TODO: all webview setup should be moved to a method called setupWebView

		// Enables scaling via the HTML <viewport> tag in the WebView
		// Ensure the scaling works correctly
		webview.getSettings().setUseWideViewPort(true);
		webview.getSettings().setSupportZoom(true);
		webview.getSettings().setLoadWithOverviewMode(true);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setInitialScale(0);

		// TODO: in the future the *webview* frame will change instead of the
		// entire dialog frame..
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		webview.setWebViewClient(new PHWebViewClient());
		webview.setWebChromeClient(new PHWebViewChrome());
		webview.setLayoutParams(params);
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); // avoid strip on side of webview

		rootView.addView(webview);

		if (hasOrientationFrame()) {
			if (delegate != null)
				delegate.didShow();

			loadTemplate();
		}

		registerBroadcastReceiver();
		
		setWebviewProtocolVersion(); // need to tell the webview what callbacks are supported
		
		// position closeBtn in case the content view never shows..
		placeCloseButton();

		showCloseAfterTimeout();

		// TODO: signup for orientation notifications
		} catch(Exception e) { //swallow all exceptions
			PHCrashReport.reportCrash(e, "PHContentView - onStart()", PHCrashReport.Urgency.critical);
		}
	}

	/** Enables/disables webview caching. Refactored into another method to increase code clarity since so many
	 * stupid extra settings
	 */
	private void setWebViewCachingDisabled(boolean isDisabled) {
		if (isDisabled)
			webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		else {
			//TODO: cleanup formatting and remove unnecessary methods
			webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
			webview.getSettings().setAppCacheMaxSize(PHConfig.cache_size);                         
			String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath(); // TODO: not sure I like this
			webview.getSettings().setAppCachePath(appCachePath);
			webview.getSettings().setAllowFileAccess(true);
			webview.getSettings().setAppCacheEnabled(true);
			webview.getSettings().setDomStorageEnabled(true);
			webview.getSettings().setDatabaseEnabled(true);
		}
	}
	
	/**
	 * Checks to see if we have a specified frame for the current orientation.
	 * TODO: refactor to return frame or null instead? May make it more useful.
	 */
	private boolean hasOrientationFrame() {
		if (content == null)
			return false;

		int orientation = getResources().getConfiguration().orientation;

		RectF contentFrame = content.getFrame(orientation);

		// will handle fullscreen as well..
		return (contentFrame.right != 0.0 && contentFrame.bottom != 0.0);
	}

	private void sizeToOrientation() {
		// TODO: perhaps use a transform matrix to rotate/scale

		int orientation = getResources().getConfiguration().orientation;

		// The coordinates have been calculted for us by the server (in our
		// coordinates)
		RectF contentFrame = content.getFrame(orientation);

		if (contentFrame.right == Integer.MAX_VALUE && contentFrame.bottom == Integer.MAX_VALUE) {
			contentFrame.right = WindowManager.LayoutParams.FILL_PARENT;
			contentFrame.bottom = WindowManager.LayoutParams.FILL_PARENT;
			contentFrame.top = 0.0f;
			contentFrame.left = 0.0f;

			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}

		// TODO: handle x,y offset as well

		getWindow().setLayout((int) contentFrame.width(), (int) contentFrame.height());
	}

	// simple display helpers
	private void loadTemplate() {
		webview.stopLoading();

		try {
		    PHStringUtil.log("Template URL: " + content.url);
		    webview.loadUrl(content.url.toString());
		
		} catch (Exception e) {
			//TODO: crash report?
			e.printStackTrace();
		}

	}

	////////////////////////////////////////////////////////
	//////////// Dismiss Methods ///////////////////////////

	public void dismiss() {
		//TODO: far too many dismiss handlers!!
		closeView();
	}

	public void dismiss(String error) {
		if (delegate == null)
			return;

		closeView();
	}

	private void closeView() {

		if (webview != null) {
			webview.setWebChromeClient(null);
			webview.setWebViewClient(null);

			webview.stopLoading();
		}

		// TODO: do something with the animate parameter?

		// TODO: remove from orientation notification

		finalDismiss();

		// for now, just dismiss the activity..
		super.finish();
	}

	private void finalDismiss() {
		prepareForReuse();

		if (delegate != null) {
			delegate.didDismiss(PHDismissType.ApplicationTriggered);
			delegate = null;
		}

	}
	
	private void setWebviewProtocolVersion() {
		if (webview == null) return;
		
		String javascriptCommand = String.format(
												JAVASCRIPT_SET_PROTOCOL_TEMPLATE,
												PHConfig.protocol
												);
		
		PHStringUtil.log("Setting protocol: " + javascriptCommand);
		
		webview.loadUrl(javascriptCommand);
	}
	
	private void clearWebView() {
		// detailed: http://stackoverflow.com/questions/2933315/clear-uiwebview-content-upon-dismissal-of-modal-view-iphone-os-3-0
		if (webview != null)
			webview.loadUrl("javascript:document.open();document.close();");
	}
	
	
	private void prepareForReuse() {
		// I think this might be a bit stupid since we don't reuse them
		content = null;
		
		clearWebView();

		// TODO: remove ourself from any events we subscribed to?
		PHURLLoader.invalidateLoaders(this); // cancel any url loaders still running
											

	}

	/////////////////////////////////////////////////////////
	///////////////// Reward Validation /////////////////////

	public boolean validateReward(JSONObject data) throws NoSuchAlgorithmException, 
														  UnsupportedEncodingException {
		String reward 			= data.optString(PHRewardKey.IDKey.			key(), "");
		String quantity 		= data.optString(PHRewardKey.QuantityKey.	key(), "");
		String receipt 			= data.optString(PHRewardKey.ReceiptKey.	key(), "");
		String signature 		= data.optString(PHRewardKey.SignatureKey.	key(), "");
		
		String generatedSig		= PHStringUtil.hexDigest(String.format(
										"%s:%s:%s:%s:%s", 
										reward,
										quantity, 
										PHConfig.device_id, 
										receipt,
										PHConfig.secret
										));
		
		PHStringUtil.log("Checking reward signature:  " + signature + " against: " + generatedSig);
		
		return (generatedSig.equalsIgnoreCase(signature));
	}

	/////////////////////////////////////////////////////////
	///////////////// Purchase Validation ///////////////////
	
	public boolean validatePurchase(JSONObject data) throws NoSuchAlgorithmException, 
															UnsupportedEncodingException {
		if (data == null)
			return false;

		String productID 		  = data.optString(PHPurchaseKey.ProductIDKey.	key(), "");
		String name 			  = data.optString(PHPurchaseKey.NameKey.	  	key(), "");
		String quantity 		  = data.optString(PHPurchaseKey.QuantityKey. 	key(), "");
		String receipt 			  = data.optString(PHPurchaseKey.ReceiptKey.  	key(), "");
		String signature 		  = data.optString(PHPurchaseKey.SignatureKey.	key(), "");
		
		String generatedSig		  = PHStringUtil.hexDigest(
										String.format("%s:%s:%s:%s:%s:%s",	productID,
																			name,
																			quantity,
																			PHConfig.device_id,
																			receipt,
																			PHConfig.secret
													  )
									);
		
		PHStringUtil.log("Checking purchase signature:  " + signature + " against: " + generatedSig);
		
		return (generatedSig.equals(signature));
	}

	////////////////////////////////////////////////
	/////////// *Sub* request delegate /////////////

	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		try {
		PHContent content = new PHContent(responseData);
		
		// we know it's a sub content request
		PHPublisherSubContentRequest sub_request = (PHPublisherSubContentRequest) request;
		
		if (content.url != null) { // should be showing yet another 'sub content' like "More Games"
			
			// display the subcontent
			PHContentView.pushContent(content, this, null, tag); // chain our own tag through since part of same request..

			// Note:
			// We notify the content template via a javascript callback. The content template
			// usually then makes a dismiss call which hides us to show the underlying "sub content"
			// Thus, the *template* dismisses us *after* we have already displayed the new content
			
			// TODO: There does appear to be some delay however, resulting in no content units showing temporarily during the transiotn
			// might want to fix this in the future?
			
			sub_request.source.sendCallback(sub_request.callback, responseData, null);
		} else {
			try {
				JSONObject error_dict = new JSONObject();
				error_dict.put("error", "1");
				sub_request.source.sendCallback(sub_request.callback,
						responseData, error_dict);

				if (delegate != null) {
					delegate.didDismiss(PHDismissType.ApplicationTriggered);
					delegate = null;
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		} catch (Exception e) { //swallow all exceptions
			PHCrashReport.reportCrash(e, "PHContentView - requestSucceeded(request, responseData)", PHCrashReport.Urgency.critical);
		}
	}

	public void requestFailed(PHAPIRequest request, Exception e) {
		try {
			JSONObject error = new JSONObject();
			error.putOpt("error", "1");
			PHPublisherSubContentRequest sub_request = (PHPublisherSubContentRequest) request;

			sub_request.source.sendCallback(sub_request.callback, null, error);
		} catch (JSONException ex) {
			PHCrashReport.reportCrash(ex,  "PHContentView - requestFailed(request, responseData)", PHCrashReport.Urgency.low);
		} catch (Exception exc) {
			PHCrashReport.reportCrash(exc, "PHContentView - requestFailed(request, responseData)", PHCrashReport.Urgency.critical);
		}
	}
	

	////////////////////////////////////////////////////////
	//////// ph:// handlers for PHRequestRouter ////////////
	
	//TODO: probably could break these out into a new handler object to reduce file length
	
	/**
	 * Simple utility method for parsing the JSON context
	 * the webview passes us.
	 * @return
	 */
	private JSONObject getRequestContext() {
		try {
			String contextStr = PHRequestRouter.getCurrentQueryVar("context");
			
			JSONObject context = new JSONObject(contextStr != null ?
												contextStr 		   :
												"{}"			   );
			
			if ( ! JSONObject.NULL.equals(context) && 
					context.length() > 0 			 ) 
				return context;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public void handleSubrequest() {
		
		JSONObject context = getRequestContext();
		if (context == null) return;
		
		PHPublisherSubContentRequest request = new PHPublisherSubContentRequest(this, this);
	
		request.setBaseURL	 (context.optString("url", ""));
		request.callback 	= PHRequestRouter.getCurrentQueryVar("callback");
		request.source 		= this;

		request.send();
	}

	public void handleDismiss() {
		
		JSONObject context = getRequestContext();
		
		PHURLLoader loader = new PHURLLoader(this, this);
		
		loader.openFinalURL 	= false;
		loader.setTargetURL		(context != null 			  ? 
								context.optString("ping", "") : 
								null);
		
		loader.delegate			= this;
		loader.setCallback		(null);
		
		loader.open();

	}

	public void handleRewards() {
		try {
			
		JSONObject context = getRequestContext();
		if (context == null) return;
		
		JSONArray rewards = (context.isNull("rewards") ? 
							 new JSONArray()           : 
						     context.optJSONArray("rewards"));
		
		for (int i = 0; i < rewards.length(); i++) {
			
			JSONObject reward = rewards.optJSONObject(i);

			if ( ! JSONObject.NULL.equals(reward) && 
				   validateReward(reward)		    ) {

				PHReward r  = new PHReward();
				
				r.name 		= reward.optString(PHRewardKey.IDKey		.key(), "");
				r.quantity 	= reward.optString(PHRewardKey.QuantityKey	.key(), "");
				r.receipt 	= reward.optString(PHRewardKey.ReceiptKey	.key(), "");

				if (delegate != null)
					delegate.didUnlockReward(r);
			}

		}
		
		sendCallback(PHRequestRouter.getCurrentQueryVar("callback"), null, null);
		
		} catch (Exception e) {  // swallow all exceptions and report
			PHCrashReport.reportCrash(e, "PHContentView - handleRewards", PHCrashReport.Urgency.low);
		}
	}

	public void handlePurchases() {
		try {
			
		JSONObject context = getRequestContext();
		if (context == null) return;
			
		JSONArray purchases = (context.isNull("purchases") ? 
							   new JSONArray()             : 
				               context.optJSONArray("purchases"));
		
		
		for (int i = 0; i< purchases.length(); i++) {
			
			JSONObject purchase = purchases.optJSONObject(i);
	
			if ( ! JSONObject.NULL.equals(purchase) &&
				   validatePurchase(purchase)		  ) {
				
				PHPurchase p 	= new PHPurchase();
				p.product 		= purchase.optString	(PHPurchaseKey.ProductIDKey	.key(), "");
				p.name 			= purchase.optString	(PHPurchaseKey.NameKey		.key(), "");
				p.quantity 		= purchase.optInt		(PHPurchaseKey.QuantityKey	.key(), -1);
				p.receipt 		= purchase.optString	(PHPurchaseKey.ReceiptKey	.key(), "");
				p.callback	    = PHRequestRouter       .getCurrentQueryVar("callback");

				String cookie 	= purchase.optString(PHPurchaseKey.CookieKey.key());
	            PHPublisherIAPTrackingRequest.setConversionCookie(p.product, cookie);

				if (delegate != null)
					delegate.didMakePurchase(p);
			}
			
		}
		
		sendCallback(PHRequestRouter.getCurrentQueryVar("callback"), null, null);
		
		} catch (Exception e) { // swallow all exceptions
			PHCrashReport.reportCrash(e, "PHContentView - handlePurchase", PHCrashReport.Urgency.critical);
		}
	}

	public void handleCloseButton() {
		try {
		
		JSONObject context = getRequestContext();
		if (context == null) return;
		
		if (closeBtnDelay != null) // stop from "double closing"
			closeBtnDelay.removeCallbacks(closeBtnDelayRunnable);

		String shouldHide = context.optString("hidden");
		
		// should we hide the button?
		
		if ( ! JSONObject.NULL.equals(shouldHide) && 
			   shouldHide.length() > 0 				)
			closeBtn.setVisibility((Boolean.parseBoolean(shouldHide) ? View.GONE : View.VISIBLE));

		
		JSONObject response = new JSONObject();
		response.put("hidden", (closeBtn.getVisibility() == View.VISIBLE ? "false" : "true")); // ping template back
		
		sendCallback(PHRequestRouter.getCurrentQueryVar("callback"), response, null);
		
		} catch (Exception e) { // swallow all exceptions
			PHCrashReport.reportCrash(e, "PHContentView - handleCloseButton", PHCrashReport.Urgency.critical);
		}
	}

	public void handleLaunch() {
		JSONObject context = getRequestContext();
		if (context == null) return;
		
		PHURLLoader loader 		= new PHURLLoader(this, this);
			
		loader.setTargetURL		(context.optString("url", ""));
		loader.setCallback		(PHRequestRouter.getCurrentQueryVar("callback"));

		loader.open();
	}
			
	public void handleLoadContext() {		
		
		// bounce right back with the appropriate template content
		sendCallback(PHRequestRouter.getCurrentQueryVar("callback"), content.context, null);
	}

	
	/////////////////////////////////////////////////////////
	/////////// Sends a Callback to the WebView /////////////
	public void sendCallback(String callback, JSONObject response, JSONObject error) {
		String callbackCommand = String.format(
									JAVASCRIPT_CALLBACK_TEMPLATE,
									(callback != null ? callback 					: "null"),
									(response != null ? response	.toString() 	: "null"), 
									(error 	  != null ? error		.toString() 	: "null")
								);
		
		PHStringUtil.log("Sending JavaScript callback to webview: '" + callbackCommand);
		webview.loadUrl(callbackCommand); 
	}

	//////////////////////////////////////////////////////
	////////////// PHURLLoader callback methods //////////
	
	public void loaderFinished(PHURLLoader loader) {
		
		if (loader.getCallback() != null) { // need to notify webview (subcontent request?)
		try {
			JSONObject r = new JSONObject();
			r.put("url", loader.getTargetURL());
			
			sendCallback(loader.getCallback(), r, null);
			
		} catch (JSONException e) {
			PHCrashReport.reportCrash(e, "PHContentView - loaderFinished", PHCrashReport.Urgency.critical);
		} catch (Exception e) { //swallow all exceptions
			PHCrashReport.reportCrash(e, "PHContentView - loaderFinished", PHCrashReport.Urgency.critical);
		}
		}
		
		if (delegate != null)
			delegate.didDismiss(PHDismissType.ContentUnitTriggered);
	
		dismiss();
	}

	public void loaderFailed(PHURLLoader loader) {
		
		if (loader.getCallback() != null) {
		try {
			JSONObject response = new JSONObject();
			JSONObject error 	= new JSONObject();

			error.put			("error", 	"1");
			response.put		("url", loader.getTargetURL());

			sendCallback(loader.getCallback(), response, error);
		} catch (JSONException e) {
			PHCrashReport.reportCrash(e, "PHContentView - loaderFailed", PHCrashReport.Urgency.critical);
		} catch (Exception e) { //swallow all exceptions
			PHCrashReport.reportCrash(e, "PHContentView - loaderFailed", PHCrashReport.Urgency.critical);
		}
		}
		
		if (delegate != null)
			delegate.didDismiss(PHDismissType.NoContentTriggered);
		
		// TODO: should we have a try catch block?
		dismiss();
	}

}
