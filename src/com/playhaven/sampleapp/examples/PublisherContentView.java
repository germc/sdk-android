package com.playhaven.sampleapp.examples;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.playhaven.androidsdk.R;
import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.publishersdk.content.PHContent;
import com.playhaven.src.publishersdk.content.PHContentView;
import com.playhaven.src.publishersdk.content.PHContentView.PHButtonState;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest;
import com.playhaven.src.publishersdk.content.PHReward;
import com.playhaven.src.publishersdk.metadata.PHNotificationView;
import com.playhaven.src.utils.PHStringUtil;

/** Simple view used for testing content view.*/
public class PublisherContentView extends ExampleView implements PHPublisherContentRequest.ContentDelegate,
																 PHPublisherContentRequest.FailureDelegate,
																 PHPublisherContentRequest.RewardDelegate,
																 PHPublisherContentRequest.CustomizeDelegate {	
	private PHPublisherContentRequest request;

	private EditText placementTxt;
	
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		
		setTitle("Content Request");
	}
	
	@Override
	protected void addTopbarItems(LinearLayout topbar) {
		
		placementTxt = new EditText(this);
		placementTxt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, .9f)); // smaller weight means bigger?
		placementTxt.setHint(R.string.default_placement);
		//TODO: debugging only
		placementTxt.setText("featured_test");
		
		topbar.addView(placementTxt);
		
		super.addTopbarItems(topbar); // will add start button on the right
	}
	@Override
	public void startRequest() {
		super.startRequest();
		
		//testing the badge
		PHNotificationView notifyView = new PHNotificationView(this, "more_games");
		notifyView.setBackgroundColor(0xFF020AFF);
		notifyView.refresh();
		
		super.addMessage("Notification View: ", notifyView);
		

		// pass ourselves as the delegate AND the context
		request = new PHPublisherContentRequest(this, placementTxt.getText().toString());
		request.send();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (PHPublisherContentRequest.didDismissContentWithin(2000)) { // can actually be less than 2 seconds, all we want is enough time for onResume to be called
			PHStringUtil.log("Resumed after displaying content");
			return; 
		}
		
		PHStringUtil.log("Resumed PHPublisherView regularly");
	}
	
	///////////////////////////////////////////////////////////
	/////////// PHPublisherContent Request Delegate ///////////
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		//TODO: do nothing here since we're a content delegate and don't care about these.
		
	}

	public void requestFailed(PHAPIRequest request, Exception e) {
		//TODO: do nothing here since we're a content delegate and don't care about these.		
	}

	public void willGetContent(PHPublisherContentRequest request) {
		PHStringUtil.log("Will get content...");
		super.addMessage("Starting content request...");
	}

	public void willDisplayContent(PHPublisherContentRequest request, PHContent content) {
		String message = String.format("Recieved content: %s. \n-------\npreparing for display", content);
		super.addMessage(message);
	}

	public void didDisplayContent(PHPublisherContentRequest request, PHContent content) {
		String message = String.format("Displayed Content: %s", content);
		super.addMessage(message);		
	}

	public void didDismissContent(PHPublisherContentRequest request, PHPublisherContentRequest.PHDismissType type) {
		String message = String.format("User dismissed request: %s of type: %s", request, type.toString());
		super.addMessage(message);
		
	}

	public void didFail(PHPublisherContentRequest request, String error) {
		String message = String.format(" Failed with error: %s", error);
		super.addMessage(message);
	}

	
	public void contentDidFail(PHPublisherContentRequest request, Exception e) {
		String message = String.format(" Content failed with error; %s", e);
		super.addMessage(message);
		
	}
	
 	/// PHContentView Delegate methods (just for testing) //
	////////////////////////////////////////////////////////
	
	public int borderColor(PHPublisherContentRequest request, PHContent content) {
		//just use default..
		return -1;
	}

	public void didShow(PHContentView view) {
		// TODO Auto-generated method stub
		
	}
	
	public void didLoad(PHContentView view) {
		// TODO Auto-generated method stub
		
	}

	public void didDismiss(PHContentView view) {
		// TODO Auto-generated method stub
		
	}

	public void didFail(PHContentView view, String error) {
		// TODO Auto-generated method stub
		
	}

	public void unlockedReward(PHPublisherContentRequest request, PHReward reward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bitmap closeButton(PHPublisherContentRequest request, PHButtonState state) {
		// TODO Auto-generated method stub
		return null;
	}

}
