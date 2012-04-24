package com.playhaven.src.publishersdk.content;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.playhaven.src.additions.ObjectExtensions;
import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.common.PHConstants.Development;
import com.playhaven.src.common.PHStringUtil;
import com.playhaven.src.common.PHURLLoader;
import com.playhaven.src.common.PHURLLoaderView;
import com.playhaven.src.common.jsbridge.PHInvocation;
import com.playhaven.src.common.jsbridge.PHJavascriptBridge;
import com.playhaven.src.common.jsbridge.PHJavascriptStub;
import com.playhaven.src.prefetch.PHUrlPrefetchOperation;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest.PHDismissType;

import com.playhaven.src.publishersdk.purchases.PHPublisherIAPTrackingRequest;

/**
 * A separate activity for actually displaying the content after the
 * PHPublisherContentRequest has finished loading. When starting this activity,
 * you should attach a "content" via "putExtra" using the CONTENT_START_KEY.
 * 
 * @author samstewart
 * 
 */
public class PHContentView extends Activity implements
		PHURLLoader.PHURLLoaderDelegate, PHAPIRequest.PHAPIRequestDelegate {

	public PHContent content;

	public boolean showsOverlayImmediately;

	private View overlayView;

	private RelativeLayout rootView;

	private PHContentWebView webview;

	private boolean isBackBtnCancelable;
	
	private boolean isTouchCancelable;

    private static Context context = null;
  
    public static Context getContext() {
        return context;
    }

	///////////////////////////////////////////////////////////////
	//////////////////////// Broadcast Constants //////////////////
	
	// argument keys for starting PHContentView Activity
	public static enum PHContentViewArgument {
		CustomCloseBtn("custom_close"),
		Content("init_content_contentview");

		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHContentViewArgument(String key) {
			this.key = key;
		}
	}
	
	public static enum PHBroadcastEvent {
		DidShow("didShow"),
		DidLoad("didLoad"),
		DidDismiss("didDismiss"),
		DidFail("didFail"),
		DidUnlockReward("didUnlockReward"),
		DidMakePurchase("didMakePurchase"),
		DidSendSubrequest("didSendSubrequest");

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
		Action("com.playhaven.src.publishersdk.content.PHContentViewEvent"),
		Event("event_contentview"),
		CloseType("closetype_contentview"),
		Source("source_contentview"),
		Callback("callback_contentview"),
		Context("context_contentview"),
		Error("error_contentview"),
		Reward("reward_contentview"),
		Purchase("purchase_contentview");
		
		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHBroadcastKey(String key) {
			this.key = key;
		}
	}

	public static enum PHBroadcastReceiverEvent {
		ContentViewsPurchaseSendCallback("contentViewsPurchaseSendCallback"),
		PurchaseResolution("purchaseResolution");

		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHBroadcastReceiverEvent(String key) {
			this.key = key;
		}
	}

	// general broadcastreceiver keys
	public static enum PHBroadcastReceiverKey {
		Action("com.playhaven.src.publishersdk.content.PHContentViewReceiverEvent"),
		Event("event_report_purchase_resolution");
		
		private String key;
		
		public String getKey() {
			return key;
		}
		
		private PHBroadcastReceiverKey(String key) {
			this.key = key;
		}
	}

	/** List of methods we can call using Reflection (like dynamic dispatch) */
	private HashMap<String, PHInvocation> redirects = new HashMap<String, PHInvocation>();

	// maximum margin for close button
	private final int CLOSE_MARGIN = 10;

	private static final int CLOSE_BTN_TIMEOUT = 4000; // in milliseconds

	// handler for showing close button after a certain delay
	private Handler closeBtnDelay;

	// runnable for closeBtnDelay
	private Runnable closeBtnDelayRunnable;

	private ImageButton closeBtn;

	public enum PHRewardKey {
		IDKey("reward"), 
		QuantityKey("quantity"), 
		ReceiptKey("receipt"), 
		SignatureKey("signature");

		private final String keyName;

		private PHRewardKey(String key) {
			this.keyName = key; // one time assignment
		}

		public String key() {
			return keyName;
		}
	};

	public enum PHPurchaseKey {
		ProductIDKey("product"),
		NameKey("name"),
		QuantityKey("quantity"),
		ReceiptKey("receipt"),
		SignatureKey("signature"),
		CookieKey("cookie");
		
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
			PHConstants.phLog("Javascript: " + consoleMessage.message()
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
			PHConstants.phLog("Page load finished setting protocol: " + url);

			// inform the template what version we're using
			String javascriptCommand = String.format(
					"javascript:window.PlayHavenDispatchProtocolVersion = %d",
					PHConstants.getProtocolVersion());
			webview.loadUrl(javascriptCommand);

			/*
			 * TODO: make sure that we eventually call the callback Issue:
			 * webview.loadUrl forks another page request if(delegate != null)
			 * delegate.didLoad(PHContentView.this);
			 */
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// just blank the webview
			view.loadUrl("");

			PHConstants.phLog(String.format("Error loading template at url: %s Code: %d Description: %s", failingUrl, errorCode, description));
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView webview, String url) {
			PHConstants.phLog("Webview override url loading called: " + url);
			if (url.startsWith("ph://")) {
				PHConstants.phLog("Calling PH://" + url + " callback");
				// parse into dictionary..
				Dictionary<String, String> queryComps = ObjectExtensions.StringExtensions.queryComponents(url);

				// now we need to strip out the query so we're left with just
				// the path.
				Uri uri = Uri.parse(url);
				url = String.format("%s://%s%s", uri.getScheme(), uri.getHost(), uri.getPath());
				String callback = queryComps.get("callback");

				// load JSON context (encoded as a large query parameter)
				String jsonContextStr = queryComps.get("context");
				PHConstants.phLog("JSON context: " + jsonContextStr + " for request url: " + url);
				JSONObject jsonContext = null;

				try {
					jsonContext = (jsonContextStr != null && !jsonContextStr.contains("undefined") ? new JSONObject(jsonContextStr) : new JSONObject());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				PHConstants.phLog("PH callback: " + callback + " parsed context: " + jsonContext);
				PHInvocation handler = redirects.get(url);
				if (handler != null) {
					PHConstants.phLog("Found handler for url: " + url);
					try {
						// pass in parameters based on number that is accepted
						switch (handler.method.getParameterTypes().length) {
						case 1:
							handler.method.invoke(handler.instance, new Object[] { jsonContext });
							break;
						case 2:
							handler.method.invoke(handler.instance, new Object[] { jsonContext, callback });
							break;
						case 3:
							handler.method.invoke(handler.instance, new Object[] { jsonContext, callback, PHContentView.this});
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return true;
				} else {
					PHConstants.phLog("Could not find handler for url: " + url);
				}

			} else if (url.startsWith("javascript:")) {
				PHConstants.phLog("Executing javascript..");
				return false; // let browser handle javascript requests..
			} else if (url.startsWith("market:")) {
				PHConstants.phLog("Got a market:// URL, verifying store exists");
				Context context = PHContentView.this;
				final PackageManager packageManager = context.getPackageManager();
				//String packagename = PHContentView.this.getPackageName();
				//String mUrl = "market://details?id=" + packagename;
				Intent marketplaceIntent = new Intent(Intent.ACTION_VIEW);
				marketplaceIntent.setData(Uri.parse(url));
				List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(marketplaceIntent,PackageManager.MATCH_DEFAULT_ONLY);
				if (resolveInfo.size() > 0)
				{
					//PHConstants.phLog("Found Marketplace and Starting Intent to load it with url...");
					//startActivity(marketplaceIntent);
					PHConstants.phLog("Found Marketplace continue loading url...");
					webview.loadUrl(url);
				}
				else
				{
					PHConstants.phLog("Marketplace NOT found, updating url...");
					Uri uri = Uri.parse(url);
					url = String.format("%s://%s%s", "https", "market.android.com", uri.getPath());
					PHConstants.phLog("New Marketplace url = " + url);
					webview.loadUrl(url);
				}

			} else {
				PHConstants.phLog("Webview redirecting...");
				webview.loadUrl(url); // handle redirect...
				return true;
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
	private PHContentViewDelegate delegate;

	public WeakReference<Object> creator; // used for PHContentView recycling

	View targetView;

	///////////////////////////////////////////////////////////
	//////////////// Broadcast (Delegate) /////////////////////
	/** Our own delegate interface that all delegates must implement. */
	public static interface PHContentViewDelegate {
		public void didShow(PHContentView view);

		public void didLoad(PHContentView view);

		public void didDismiss(PHContentView view, PHDismissType type);

		public void didFail(PHContentView view, String error);

		public void didUnlockReward(PHContentView view, PHReward reward);

		public void didMakePurchase(PHContentView view, PHPurchase purchase);

		public void didSendSubrequest(JSONObject context, String callback,
				PHContentView source);
	}

	/**
	 * Adapter bridge between actual code and 
Manager. Since we are an
	 * activity, we cannot simply call delegate methods directly.
	 */
	public class PHContentViewDelegateBroadcaster implements
			PHContentViewDelegate {
		private void sendStateUpdate(String state) {
			sendBroadcast(createBaseIntent(state));
		}

		private Intent createBaseIntent(String event) {
			Intent intent = new Intent(PHBroadcastKey.Action.getKey());

			intent.putExtra(PHBroadcastKey.Source.getKey(), PHContentView.this.hashCode()); // used to represent ourselves
			intent.putExtra(PHBroadcastKey.Event.getKey(), event);
			return intent;
		}

		public void didUnlockReward(PHContentView view, PHReward reward) {
			Intent intent = createBaseIntent(PHBroadcastEvent.DidUnlockReward.getKey());
			intent.putExtra(PHBroadcastKey.Reward.getKey(), reward);

			sendBroadcast(intent);
		}

		public void didMakePurchase(PHContentView view, PHPurchase purchase) {
			
			Intent intent = createBaseIntent(PHBroadcastEvent.DidMakePurchase.getKey());
			intent.putExtra(PHBroadcastKey.Purchase.getKey(), purchase);

			sendBroadcast(intent);
		}

		public void didSendSubrequest(JSONObject context, String callback,
				PHContentView source) {
			Intent intent = createBaseIntent(PHBroadcastEvent.DidSendSubrequest.getKey());
			intent.putExtra(PHBroadcastKey.Context.getKey(), context.toString());
			intent.putExtra(PHBroadcastKey.Callback.getKey(), callback);
			intent.putExtra(PHBroadcastKey.Source.getKey(), source.hashCode());

			sendBroadcast(intent);
		}

		public void didShow(PHContentView view) {
			sendStateUpdate(PHBroadcastEvent.DidShow.getKey());
		}

		public void didLoad(PHContentView view) {
			sendStateUpdate("didLoad");
		}

		public void didDismiss(PHContentView view, PHDismissType type) {
			Intent intent = createBaseIntent(PHBroadcastEvent.DidDismiss.getKey());
			intent.putExtra(PHBroadcastKey.CloseType.getKey(), type.name());
			
			sendBroadcast(intent);
		}

		public void didFail(PHContentView view, String error) {
			Intent intent = createBaseIntent(PHBroadcastEvent.DidFail.getKey());
			intent.putExtra(PHBroadcastKey.Error.getKey(), error);

			sendBroadcast(intent);
		}
	}

	private void registerBroadcastReceiver() {
		if (getApplicationContext() != null) {
			getApplicationContext().registerReceiver(new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					Bundle extras = intent.getExtras();
					String event = extras.getString(PHContentView.PHBroadcastReceiverKey.Event.getKey());
					String resolution = extras.getString(PHContentView.PHBroadcastReceiverEvent.PurchaseResolution.getKey());
					
					if (event.equals(PHContentView.PHBroadcastReceiverEvent.ContentViewsPurchaseSendCallback.getKey())) {
					    JSONObject purchaseResolutionDict = null;
						try {
							purchaseResolutionDict = new JSONObject(resolution);
							contentViewsPurchaseSendCallback(purchaseResolutionDict);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}					
				}
			}, new IntentFilter(PHContentView.PHBroadcastReceiverKey.Action.getKey()));
		}
	}

	/////////////////////////////////////////////////
	//PHPurchase BroadcastReceiver method.
	public void contentViewsPurchaseSendCallback(JSONObject response) {			
		sendCallback(ObjectExtensions.JSONExtensions.getJSONString(response, "callback"),
									 ObjectExtensions.JSONExtensions.getJSONObject(response, "response"),
									 ObjectExtensions.JSONExtensions.getJSONObject(response, "error"));

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
				inactive = new BitmapDrawable(inactiveRes.loadImage(PHConstants
						.getScreenDensityType()));
			}

			if (active == null) {
				PHCloseActiveImageResource active_res = (PHCloseActiveImageResource) PHResourceManager
						.sharedResourceManager().getResource("close_active");
				active = new BitmapDrawable(active_res.loadImage(PHConstants
						.getScreenDensityType()));
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
					buttonDismiss();
				}
			});

		}
		return closeBtn;
	}

	private void buttonDismiss() {
		PHConstants.phLog("The content unit was dismissed by the user");
		
		delegate.didDismiss(this, PHDismissType.CloseButtonTriggered);

		this.finish();
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

		marginRight = PHConstants.dipToPixels(CLOSE_MARGIN);
		marginTop = PHConstants.dipToPixels(CLOSE_MARGIN);


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
		// TODO: bit hacky?
		if (close.getParent() != null) {
			ViewGroup detailParent = (ViewGroup) close.getParent();
			detailParent.removeView(close);
		}

		getRootView().addView(close);
	}

	/** Displays button after timeout. */
	private void showCloseAfterTimeout(int timeout) {
		if (closeBtnDelay == null)
			closeBtnDelay = new Handler();

		// post delayed message to the queue
		closeBtnDelay.postDelayed(new Runnable() {

			public void run() {
				showCloseButton();
			}

		}, timeout);
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
	
	public static void pushContent(PHContent content, Context context, HashMap<String, Bitmap> customCloseImages) {
		if (context != null) {
			Intent contentViewIntent = new Intent(context, PHContentView.class);
			
			// attach the content object
			contentViewIntent.putExtra(PHContentView.PHContentViewArgument.Content.getKey(), content);
			
			// attach custom closeImages
			if (customCloseImages != null && customCloseImages.size() > 0)
				contentViewIntent.putExtra(PHContentView.PHContentViewArgument.CustomCloseBtn.getKey(), customCloseImages);
			
			context.startActivity(contentViewIntent);
		}
	}
	////////////////////////////////////////////////////////
	//////////////// Activity Callbacks ////////////////////

	@Override
	public void onBackPressed() {
		if (this.getIsBackBtnCancelable()) {
			PHConstants.phLog("The content unit was dismissed by the user using back button");
			
			delegate.didDismiss(this, PHDismissType.CloseButtonTriggered);

			super.onBackPressed();
		}

		// otherwise, simply consume and ignore..
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (webview != null) {
			// make sure we don't leak any memory
			webview.setWebChromeClient(null);
			webview.setWebViewClient(null);
		}

		PHAPIRequest.cancelRequests(this);
		
		// TODO: make sure to cleanup and remove ourselves from static
		// collection

		// TODO: call the appropriate delegate method

		// stop close button
		if (closeBtnDelay != null)
			closeBtnDelay.removeCallbacks(closeBtnDelayRunnable);

		hideCloseButton();
	}

	/**
	 * Override to dismiss if we don't have frame. (you can't dismiss from
	 * onStart() as the system won't honor it)
	 */
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		// we can only dismiss once the window is visible..
		if (!hasOrientationFrame()) {
			dismiss("The content you requested was not able to be shown because it is missing required orientation data.");
			return;
		}

		// make window transparent
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// create Broadcast delegate
		delegate = new PHContentViewDelegateBroadcaster();

		// load content from intent
		content = getIntent().getParcelableExtra(PHContentViewArgument.Content.getKey());
		
		if (getIntent().hasExtra(PHContentViewArgument.CustomCloseBtn.getKey())) {
			customCloseStates = (HashMap<String, Bitmap>)getIntent().getSerializableExtra(PHContentViewArgument.CustomCloseBtn.getKey());
		}
		
		// by default, we are cancelable only via back button (false=touch, back btn=true)
		this.setCancelable(false, true);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		setup();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			if (this.getIsTouchCancelable()) finish();
			return true;
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

	public void setCreator(Object creator) {
		this.creator = new WeakReference<Object>(creator);
	}

	public void setContent(PHContent content) {
		if (content != null) {
			this.content = content;
		}
	}

	private void setup() {
		// setup all the redirect handles
		Class<? extends PHContentView> cls = this.getClass();
		try {
			Method dismiss = cls.getMethod("handleDismiss", new Class[] { JSONObject.class });
			Method launch = cls.getMethod("handleLaunch", new Class[] {JSONObject.class, String.class });
			Method loadCntxt = cls.getMethod("handleLoadContext", new Class[] {JSONObject.class, String.class });
			Method reward = cls.getMethod("handleRewards", new Class[] {JSONObject.class, String.class, PHContentView.class});
			Method purchase = cls.getMethod("handlePurchases", new Class[] {JSONObject.class, String.class, PHContentView.class});
			Method subcontent = cls.getMethod("sendSubrequest", new Class[] {JSONObject.class, String.class, PHContentView.class});
			Method closeButton = cls.getMethod("handleCloseButton", new Class[] {JSONObject.class, String.class, PHContentView.class});
			
			redirects.put("ph://dismiss", new PHInvocation(this, dismiss));
			redirects.put("ph://launch", new PHInvocation(this, launch));
			redirects.put("ph://loadContext", new PHInvocation(this, loadCntxt));
			redirects.put("ph://reward", new PHInvocation(this, reward));
			redirects.put("ph://purchase", new PHInvocation(this, purchase));
			redirects.put("ph://subcontent", new PHInvocation(this, subcontent));
			redirects.put("ph://closeButton", new PHInvocation(this, closeButton));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Allows external client to hook into ph:// callback. */
	public void redirectRequest(String url, Object target, String method) {
		if (target != null) {
			// we create the Method instance
			@SuppressWarnings("rawtypes")
			Class cls = target.getClass();
			// loop through until we can find the matching method
			Method[] methods = cls.getMethods();
			Method meth = null;

			for (int i = 0; i < methods.length; i++) {
				meth = methods[i];
				if (meth.getName().equals(method)) {
					// this is our method so we create it
					PHInvocation invoke = new PHInvocation(target, meth);
					redirects.put(url, invoke);
					break;
				}
			}
		} else {
			redirects.put(url, null); // we want an error to trigger
		}
	}

	// Section: Overlay and Close button methods
	// ======================================
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

	/** Testing method for javascript bridge. */
	@SuppressWarnings("unused")
	private void testJSBridge(WebView webview) {
		PHJavascriptBridge bridge = new PHJavascriptBridge(webview);
		class PlayhavenCallback extends PHJavascriptStub {
			public void callback(String callback, String response, String error) {
				super.forwardMethod(callback, response, error);
			}
		}
		PlayhavenCallback stub = new PlayhavenCallback();
		bridge.addJavascriptStub("Playhaven.native", stub);

		stub.callback("hi!", "test", "test");
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

		context = getApplicationContext();
		   
		rootView = new RelativeLayout(getApplicationContext());
		
		setContentView(rootView, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT));

		sizeToOrientation(); // must be called *after* setContentView

		webview = new PHContentWebView(this);
		// TODO: debug only!!
		// testJSBridge(webview);

		// debugging only
		//webview.setBackgroundColor(0xFFF7C66A);

		if (!PHConstants.shouldCacheWebView())
			webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		else {
			webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
			webview.getSettings().setAppCacheMaxSize(Development.MAX_CACHE_SIZE);                         
			webview.getSettings().setAppCachePath(Development.APP_CACHE_PATH);
			webview.getSettings().setAllowFileAccess(true);
			webview.getSettings().setAppCacheEnabled(true);
			webview.getSettings().setDomStorageEnabled(true);
			
		}

		// TODO: find more elegant method of fixing the scaling issue?
		hackEnableScaling();

		// TODO: in the future the *webview* frame will change instead of the
		// entire dialog frame..
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		webview.setWebViewClient(new PHWebViewClient());
		webview.setWebChromeClient(new PHWebViewChrome());
		webview.setLayoutParams(params);
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); // avoid
																	// strip on
																	// side

		rootView.addView(webview);

		if (hasOrientationFrame()) {
			if (delegate != null)
				delegate.didShow(this);

			loadTemplate();
		}

		registerBroadcastReceiver();

		// position closeBtn in case the content view never shows..
		placeCloseButton();

		showCloseAfterTimeout(CLOSE_BTN_TIMEOUT);

		// TODO: signup for orientation notifications
	}

	/** Enables scaling via the HTML <viewport> tag in the WebView (hacky) */
	private void hackEnableScaling() {
		// Hack to ensure the scaling works correctly
		webview.getSettings().setUseWideViewPort(true);
		webview.getSettings().setSupportZoom(true);
		webview.getSettings().setLoadWithOverviewMode(true);
		webview.setInitialScale(0);
	}

	/**
	 * Checks to see if we have a specified frame for the current orientation.
	 * TODO: refactor to return frame or null instead? May make it more useful.
	 */
	private boolean hasOrientationFrame() {
		if (content == null)
			return false;

		int orientation = PHConstants
				.getDeviceOrientation(getApplicationContext());

		RectF contentFrame = content.getFrame(orientation);

		// will handle fullscreen as well..
		return (contentFrame.right != 0.0 && contentFrame.bottom != 0.0);
	}

	private void sizeToOrientation() {
		// TODO: perhaps use a transform matrix to rotate/scale

		int orientation = PHConstants.getDeviceOrientation(getApplicationContext());

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
		//if (!PHConstants.shouldCacheWebView())
		//	webview.clearCache(true); // TODO: debug only!!!!

		webview.stopLoading();

		try {

	        Uri url = PHConstants.getTemplateURL(content); // different depending on
														// dev, staging, prod...
	        String cache_path = Environment.getExternalStorageDirectory() + Development.PLAYHAVEN_PREFETCH_CACHE_PATH;
	        PHConstants.phLog("prefetch cache path: " + cache_path);
	        File cacheDir = new File(cache_path);
	        cacheDir.mkdirs();
	        String newLocalUrl = new String("content://com.playhaven.src.prefetch.PHPrefetchLocalContentProvider"+url.getPath());
	        String fileCacheName = PHUrlPrefetchOperation.cacheKeyForURL(newLocalUrl);
	        PHConstants.phLog("prefetch cache file name: " + fileCacheName);
			File newCacheFile = new File(cacheDir, fileCacheName);
	
			if (newCacheFile.exists()) {
		        PHConstants.phLog("prefetching local url: " + newLocalUrl);
		        webview.loadUrl(newLocalUrl);
			}
			else {
		        PHConstants.phLog("Loading url in intial webview load: " + url);
				webview.loadUrl(url.toString());
				
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	////////////////////////////////////////////////////////
	//////////// Dismiss Methods ///////////////////////////

	public void dismiss() {
		closeView();
	}

	public void dismiss(String error) {
		if (delegate == null)
			return;

		/**
		 * TODO: make sure the appropriate delegate callbacks are being called only once
		 * 		// we do this to avoid redundant api callbacks
		PHContentViewDelegate oldndelegate;
		delegate = null;

		closeView(isAnimated); // will null delegate..

		// TODO: make sure we don't call didFial method twice
		oldDelegate.didFail(this, error);
		 */
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
			delegate.didDismiss(this, PHDismissType.ApplicationTriggered);
			delegate = null;
		}

	}

	private void resetRedirects() {
		// remove all redirects except the ones we added...
		for (String key : redirects.keySet()) {
			PHInvocation invoke = redirects.get(key);
			if (invoke.instance == this) {
				redirects.remove(invoke);
			}
		}

	}

	private void prepareForReuse() {
		content = null;
		resetRedirects();
		if (webview != null)
			webview.clearContent();

		// TODO: remove ourself from any events we subscribed to?
		PHURLLoader.invalidateLoaders(this); // cancel any url loaders still
												// running...

	}

	/////////////////////////////////////////////////////////
	///////////////// Reward Validation /////////////////////

	public boolean validateReward(JSONObject data) {
		String reward = ObjectExtensions.JSONExtensions.getJSONString(data,
				PHRewardKey.IDKey.key());
		String quantity = ObjectExtensions.JSONExtensions.getJSONString(data,
				PHRewardKey.QuantityKey.key());
		String receipt = ObjectExtensions.JSONExtensions.getJSONString(data,
				PHRewardKey.ReceiptKey.key());
		PHConstants.phLog("Receipt for reward unlock: " + receipt);
		String signature = ObjectExtensions.JSONExtensions.getJSONString(data,
				PHRewardKey.SignatureKey.key());
		String generatedSigString = String.format("%s:%s:%s:%s:%s", reward,
				quantity, PHConstants.getUniqueID(), receipt,
				PHConstants.getSecret());

		PHConstants.phLog("Generated signature for reward: "
				+ generatedSigString);

		String generatedSig = PHStringUtil.hexDigest(generatedSigString);

		// check that the signature passed in matches ours.
		return (generatedSig.equalsIgnoreCase(signature));
	}

	/////////////////////////////////////////////////////////
	///////////////// Purchase Validation /////////////////////
	
	public boolean validatePurchase(JSONObject data) {
		if (data == null)
			return false;

		String productID = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.ProductIDKey.key());
		String name = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.NameKey.key());
		String quantity = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.QuantityKey.key());
		String receipt = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.ReceiptKey.key());
		PHConstants.phLog("Receipt for purchase: "+receipt);
		String signature = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.SignatureKey.key());
		String generatedSigString = String.format("%s:%s:%s:%s:%s:%s",	productID,
																		name,
																		quantity,
																		PHConstants.getUniqueID(),
																		receipt,
																		PHConstants.getSecret());
		
		PHConstants.phLog("Generated signature for purchase: "+generatedSigString);
		
		String generatedSig = PHStringUtil.hexDigest(generatedSigString);
		
		//check that the signature passed in matches ours.
		return (generatedSig.equals(signature));
	}

	////////////////////////////////////////////////
	/////////// Sub request delegate ///////////////

	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		PHContent content = new PHContent();
		boolean success = content.fromJSON(responseData);
		PHConstants.phLog("Parse content success: " + success);
		// we know it's a sub content request
		PHPublisherSubContentRequest sub_request = (PHPublisherSubContentRequest) request;
		PHConstants.phLog("Subrequest succeeded...");
		
		if (success) {
			PHContentView.pushContent(content, this, null);
			// tell the webview (through javascript) the sub request has
			// succeeded.
			sub_request.source.sendCallback(sub_request.callback, responseData, null);
		} else {
			// inform the webview through javascript the subrequest failed
			try {
				JSONObject error_dict = new JSONObject();
				error_dict.put("error", "1");
				sub_request.source.sendCallback(sub_request.callback,
						responseData, error_dict);

				delegate.didDismiss(this, PHDismissType.ApplicationTriggered);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void requestFailed(PHAPIRequest request, Exception e) {
		try {
			JSONObject error = new JSONObject();
			error.putOpt("error", "1");
			PHPublisherSubContentRequest sub_request = (PHPublisherSubContentRequest) request;

			sub_request.source.sendCallback(sub_request.callback, null, error);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////
	/////////////// ph:// handlers /////////////////////////

	public void sendSubrequest(JSONObject context, String callback, PHContentView source) {
		PHPublisherSubContentRequest request = new PHPublisherSubContentRequest(this);
		
		request.setBaseURL(ObjectExtensions.JSONExtensions.getJSONString(context, "url"));
		request.callback = callback;
		request.source = source;

		request.send();
	}

	public void handleDismiss(JSONObject context) {
		String pingPath = context.optString("ping");

		if (pingPath != null && !pingPath.equalsIgnoreCase("")) {
			PHURLLoader loader = new PHURLLoader(this);
			loader.opensFinalURLOnDevice = false;
			loader.targetURI = pingPath;

			loader.open();
		}
		dismiss();

	}

	public void handleRewards(JSONObject context, String callback,
			PHContentView source) {
		JSONArray rewardsArray = ObjectExtensions.JSONExtensions.getJSONArray(
				context, "rewards");
		for (int i = 0; i < rewardsArray.length(); i++) {
			JSONObject rewardData = ObjectExtensions.JSONExtensions
					.getJSONObject(rewardsArray, i);

			if (validateReward(rewardData)) {
				PHReward reward = new PHReward();
				reward.name = ObjectExtensions.JSONExtensions.getJSONString(
						rewardData, PHRewardKey.IDKey.key());
				reward.quantity = ObjectExtensions.JSONExtensions
						.getJSONString(rewardData,
								PHRewardKey.QuantityKey.key());
				reward.receipt = ObjectExtensions.JSONExtensions.getJSONString(
						rewardData, PHRewardKey.ReceiptKey.key());

				delegate.didUnlockReward(this, reward);
			}

		}
		source.sendCallback(callback, null, null);
	}

	public void handlePurchases(JSONObject context, String callback, PHContentView source) {
		JSONArray purchasesArray = ObjectExtensions.JSONExtensions.getJSONArray(context, "purchases");
		if (purchasesArray== null)
			return;
		for (int i =0; i<purchasesArray.length(); i++) {
			JSONObject purchaseData = ObjectExtensions.JSONExtensions.getJSONObject(purchasesArray, i);
	
		if (validatePurchase(purchaseData)) {
				PHPurchase purchase = new PHPurchase();
				purchase.productIdentifier = ObjectExtensions.JSONExtensions.getJSONString(purchaseData, PHPurchaseKey.ProductIDKey.key());
				purchase.name = ObjectExtensions.JSONExtensions.getJSONString(purchaseData, PHPurchaseKey.NameKey.key());
				purchase.quantity = ObjectExtensions.JSONExtensions.getJSONString(purchaseData, PHPurchaseKey.QuantityKey.key());
				purchase.receipt = ObjectExtensions.JSONExtensions.getJSONString(purchaseData, PHPurchaseKey.ReceiptKey.key());
				purchase.callback = callback;

				String cookie = ObjectExtensions.JSONExtensions.getJSONString(purchaseData, PHPurchaseKey.CookieKey.key());
	            PHPublisherIAPTrackingRequest.setConversionCookie(cookie, purchase.productIdentifier);

            	delegate.didMakePurchase(this, purchase);
			}
			
		}
		source.sendCallback(callback, null, null);
	}

	public void handleCloseButton(JSONObject context, String callback, PHContentView source) {
		if (closeBtnDelay != null)
			closeBtnDelay.removeCallbacks(closeBtnDelayRunnable);

		String shouldHide = ObjectExtensions.JSONExtensions.getJSONString(context, "hidden");
		PHConstants.phLog("Close button should hide: " + shouldHide);
		if (shouldHide != null)
			closeBtn.setVisibility((Boolean.parseBoolean(shouldHide) ? View.GONE : View.VISIBLE));

		JSONObject response = new JSONObject();

		// decide if hidden or not (don't just pass in shouldHide value, we want
		// to ensure the change worked)
		// TODO: are we passing back the correct format for booleans?
		ObjectExtensions.JSONExtensions.setJSONString(response, "hidden", (closeBtn.getVisibility() == View.VISIBLE ? "false" : "true"));
		source.sendCallback(callback, response, null);
	}

	public void handleLaunch(JSONObject context, String callback) {
		// attempt to grab the url to launch
		String urlPath = ObjectExtensions.JSONExtensions.getJSONString(context, "url");

		if (urlPath != null) {
			PHURLLoaderView view = new PHURLLoaderView(this, this);
			view.delegate = this;
			view.setTargetURL(urlPath);

			JSONObject callbackContext = new JSONObject();
			ObjectExtensions.JSONExtensions.setJSONString(callbackContext, "callback", callback);

			view.setJSONContext(callbackContext);

			view.show();
		}
	}

	public void handleLoadContext(JSONObject context, String callback) {
		PHConstants.phLog("PHLoadContext has called: " + callback + "Query " + context);
		sendCallback(callback, content.context, null);
	}

	public void sendCallback(String callback, JSONObject response, JSONObject error) {
		String callbackStr = "null";
		String responseStr = "null";
		String errorStr = "null";

		if (callback != null)
			callbackStr = callback;
		if (response != null)
			responseStr = response.toString();
		if (error != null)
			errorStr = error.toString();

		String callbackCommand = String.format(
				"javascript:PlayHaven.nativeAPI.callback(\"%s\",%s,%s)",
				callbackStr, responseStr, errorStr);
		
		PHConstants.phLog("Callback command being called on webview: "
				+ callbackCommand);
		webview.loadUrl(callbackCommand); // execute javascript

	}

	//////////////////////////////////////////////////////
	////////////// PHURLLoader callback methods //////////
	public void loaderFinished(PHURLLoader loader) {
		JSONObject jsonContext = loader.getJSONContext();
		JSONObject response = new JSONObject();

		ObjectExtensions.JSONExtensions.setJSONString(response, "url",
				loader.targetURI);

		sendCallback(ObjectExtensions.JSONExtensions.getJSONString(jsonContext,
				"callback"), response, null);

	}

	public void loaderFailed(PHURLLoader loader) {
		JSONObject jsonContext = loader.getJSONContext();
		JSONObject response = new JSONObject();
		JSONObject error = new JSONObject();

		ObjectExtensions.JSONExtensions.setJSONString(error, "error", "1");
		ObjectExtensions.JSONExtensions.setJSONString(response, "url",
				loader.targetURI);

		sendCallback(ObjectExtensions.JSONExtensions.getJSONString(jsonContext,
				"callback"), response, error);

	}

}
