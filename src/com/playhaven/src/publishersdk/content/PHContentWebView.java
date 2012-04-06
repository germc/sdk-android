package com.playhaven.src.publishersdk.content;

import android.content.Context;
import android.content.res.Configuration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;

/** The actual webview that does the loading of the advertisments. */
public class PHContentWebView extends WebView {
	private Runnable bounceInCallback;
	
	private Runnable bounceOutCallback;

	private boolean isAnimating;

	/** Constructor which allows us to create our webview */
	public PHContentWebView(Context context) {
		super(context);
		this.getSettings().setJavaScriptEnabled(true);
		this.setHorizontalScrollBarEnabled(false);
	}

	/** Displays bounce in animation then calls callback when done */
	public void bounceIn(Runnable callback) {
		if (!isAnimating) {
			isAnimating = true;
			bounceInCallback = callback;

			AnimationSet bounceIn = new AnimationSet(false);

			// initial part
			AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
			alphaAnim.setInterpolator(new AccelerateInterpolator());
			alphaAnim.setDuration(125L);
			bounceIn.addAnimation(alphaAnim);

			// scale animation
			ScaleAnimation scaleAnim = new ScaleAnimation(0.8f, 1.1f, 0.8f,
					1.1f);
			scaleAnim.setInterpolator(new AccelerateInterpolator());
			scaleAnim.setDuration(125L);
			bounceIn.addAnimation(alphaAnim);

			// continue bounce out...
			ScaleAnimation scaleAnim2 = new ScaleAnimation(1.1f, 1.0f, 1.1f,
					1.0f);
			scaleAnim2.setStartOffset(125L);
			scaleAnim2.setDuration(125L);
			scaleAnim2.setInterpolator(new AccelerateInterpolator());

			scaleAnim2.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationEnd(Animation animation) {
					finishBounceIn();
				}

				public void onAnimationStart(Animation animation) {
					// TODO: dummy method
				}

				public void onAnimationRepeat(Animation animation) {
					// TODO: dummy method
				}

			});
			bounceIn.addAnimation(scaleAnim2);

			this.startAnimation(bounceIn);
		}
	}

	private void finishBounceIn() {
		isAnimating = false;
		bounceInCallback.run(); // synchronous remember. :)
	}

	/** Displays bounce out animation then calls callback when done */
	public void bounceOut(Runnable callback) {
		if(!isAnimating) {
			isAnimating = true;
			bounceOutCallback = callback;
			
			AnimationSet anim = new AnimationSet(false);
			ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f);
			scaleAnim.setDuration(125L);
			anim.addAnimation(anim);
			
			//offset after bounce..
			AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
			alphaAnim.setDuration(125L);
			alphaAnim.setStartOffset(125L);
			anim.addAnimation(alphaAnim);
			
			ScaleAnimation scaleAnim2 = new ScaleAnimation(1.1f, 0.8f, 1.1f, 0.8f);
			scaleAnim2.setDuration(125L);
			scaleAnim2.setStartOffset(125L);
			
			// set the animatino listen to call the callback.
			scaleAnim2.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationEnd(Animation animation) {
					finishBounceOut();
				}

				public void onAnimationStart(Animation animation) {
					// TODO: dummy method
				}

				public void onAnimationRepeat(Animation animation) {
					// TODO: dummy method
				}

			});
			anim.addAnimation(scaleAnim2);
			
			this.startAnimation(anim);
			
		}
		

	}

	/** Clears all the content without having to reload.*/
	public void clearContent() {
		// detailed: http://stackoverflow.com/questions/2933315/clear-uiwebview-content-upon-dismissal-of-modal-view-iphone-os-3-0
		this.loadUrl("javascript:document.open();document.close();");
	}
	private void finishBounceOut() {
		isAnimating = false;
		bounceOutCallback.run(); // synchronous remember. :)
	}

	/**
	 * Tells the webview content we've changed orientation. Call into
	 * javascript.
	 */
	public void updateOrientation(int orientation) {
		String jsonStr = (orientation == Configuration.ORIENTATION_LANDSCAPE ? "PH_LANDSCAPE"
				: "PH_PORTRAIT");
		String javascript = String.format("PlayHaven.updateOrientation(%s)",
				jsonStr);
		this.loadUrl("javascript:" + javascript);
	}
}
