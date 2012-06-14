package com.playhaven.sampleapp.examples;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHCrashReport;

/**
 * @class PlayhavenCrashView
 * @author samstewart Simple class which demonstrates the crash reporting
 *         functionality.
 */
public class PlayhavenCrashView extends ExampleView {
	PHCrashReport request;

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

		setTitle("Crash Request");
	}

	@Override
	public void startRequest() {
											// supertype ExampleView

		// TODO: test how a nested stack trace looks (create some
		// objects/methods)
		UnsupportedOperationException exception = new UnsupportedOperationException(
				"Sample exception");

		request = new PHCrashReport(exception, "PlayhavenCrashView - startRequest", PHCrashReport.Urgency.critical);
		request.send();

	}

	// /////////////////////////////////////////////////////////////
	// //////////////////////// Delegate Methods ///////////////////
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		try {
			String formattedJson = responseData.toString(2);
			super.addMessage(String.format("Success with response: %s",
					formattedJson));
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void requestFailed(PHAPIRequest request, Exception e) {
		super.addMessage(String.format("Failed with error: %s", e.toString()));
	}
}
