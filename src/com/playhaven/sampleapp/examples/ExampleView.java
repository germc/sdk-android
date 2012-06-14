package com.playhaven.sampleapp.examples;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.playhaven.androidsdk.R;
import com.playhaven.sampleapp.DetailAdapter;

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
	
	public LinearLayout topbarLayout;
	
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
		
		setupTopbar();
		
		getListView().addHeaderView(topbarLayout);
		
		adapter = new DetailAdapter<MessageObject>(this, R.layout.row, messages);
		setListAdapter(adapter);
	}
	
	private void setupTopbar() {
		topbarLayout = new LinearLayout(this);
		topbarLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		addTopbarItems(topbarLayout);
	}
	
	// usually overridden by subclasses
	protected void addTopbarItems(LinearLayout topbar) {
		topbar.addView(createSendButton());
	}
	
	private Button createSendButton() {
		Button sendBtn = new Button(this);
		
		sendBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, .3f)); 
		
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRequest();
			}
		});
		
		sendBtn.setText(R.string.start_button_text);
		
		return sendBtn;
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
