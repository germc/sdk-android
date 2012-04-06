package com.playhaven.sampleapp;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.playhaven.androidsdk.R;

/** Represents a single abstract base class which allows you to make a sample
 * request to the server and post back with results.
 * 
 * You can extend this class to test various server requests. You can either log a simple string as a message
 * or you can log an entire view for testing.
 * @author samuelstewart
 *
 */
public class ExampleView extends ListActivity {
	private ArrayList<MessageObject> messages;
	
	private DetailAdapter<MessageObject> adapter;
	
	/** Simple class for holding messages*/
	private class MessageObject implements DetailAdapter.DetailObject {
		public String message;
		public String title;
		public View customView;
		
		public MessageObject(String msg, String tlt) {
			this.message = msg;
			this.title = tlt;
			this.customView = null;
		}
		public MessageObject(String msg, String tlt, View view) {
			this.message = msg;
			this.title = tlt;
			this.customView = view;
		}
		//------------
		// DetailObject Methods
		public String getTitle() {
			return title;
		}

		public String getDetail() {
			return message;
		}
		
		public View getView() {
			return customView;
		}
	}

	public boolean bShowPlacementEditText = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		messages = new ArrayList<MessageObject>();
		
		setupTopBar();
		
		adapter = new DetailAdapter<MessageObject>(this, R.layout.row, messages);
		setListAdapter(adapter);
	}
	/** Creates the top bar with send button, etc.*/
	private void setupTopBar() {
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.example_top, null);
		
		if (!bShowPlacementEditText) {
			EditText placementText = (EditText)view.findViewById(R.id.editTextPlacementID);
			placementText.setVisibility(View.GONE);
		}
		
		Button sendBtn = (Button)view.findViewById(R.id.sendRequestBtn);
		sendBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startRequest();
			}
		});
		
		// add to top bar
		getListView().addHeaderView(view);
	}
	
	/** Adds another message onto the list (end of list)*/
	protected void addMessage(String message) {
		messages.add(new MessageObject(message, null)); // we leave the title null so it doesn't show up
		adapter.notifyDataSetChanged();
	}
	/** Adds a message onto the list but includes the view specified..*/
	protected void addMessage(String message, View testingView) {
		messages.add(new MessageObject(message, null, testingView)); //we don't have a title so we set it to null.
	}
	
	/** Starts the request ands adds a default message to the queue.*/
	protected void startRequest() {
		adapter.clear();
		
		addMessage("Started Request");
	}
}
