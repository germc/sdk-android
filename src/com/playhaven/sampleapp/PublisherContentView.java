package com.playhaven.sampleapp;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;

import com.playhaven.androidsdk.R;
import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.publishersdk.content.PHContent;
import com.playhaven.src.publishersdk.content.PHContentView;
import com.playhaven.src.publishersdk.content.PHContentView.PHButtonState;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest;
import com.playhaven.src.publishersdk.content.PHReward;
import com.playhaven.src.publishersdk.metadata.PHNotificationView;

/** Simple view used for testing content view.*/
public class PublisherContentView extends ExampleView implements PHPublisherContentRequest.PHPublisherContentRequestDelegate,
																 PHPublisherContentRequest.PHFailureDelegate,
																 PHPublisherContentRequest.PHRewardDelegate,
																 PHPublisherContentRequest.PHCustomizeDelegate {	
	private PHPublisherContentRequest request;

	@Override
	public void onCreate(Bundle savedInstance) {
		
		bShowPlacementEditText = true;
		
		// make sure list adapter is setup..
		super.onCreate(savedInstance);
	
		//TODO: debug to force screen orientation
		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setTitle("Content Request");
	}
	
	@Override
	public void startRequest() {
		super.startRequest();
		
		PHConstants.findDeviceInfo(this);
		
		//testing the badge
		PHNotificationView notifyView = new PHNotificationView(this, "more_games");
		notifyView.setBackgroundColor(0xFF020AFF);
		super.addMessage("Notification View", notifyView);
		notifyView.refresh();
		
		EditText placementEditText = (EditText) findViewById(R.id.editTextPlacementID);
		String placementValue = placementEditText.getText().toString();
		if (placementValue.length() == 0)
			placementValue = "level_complete";

		// pass ourselves as the delegate AND the context
		request = new PHPublisherContentRequest(this, this);
		request.placement = placementValue;
		PHConstants.phLog("Starting content request...");
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
	
	// Content request delegate methods..
	// ----------------------
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		//TODO: do nothing here since we're a content delegate and don't care about these.
		
	}

	public void requestFailed(PHAPIRequest request, Exception e) {
		//TODO: do nothing here since we're a content delegate and don't care about these.		
	}

	public void willGetContent(PHPublisherContentRequest request) {
		PHConstants.phLog("Will get content...");
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
