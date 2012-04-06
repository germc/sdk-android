package com.playhaven.sampleapp;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.playhaven.androidsdk.R;
import com.playhaven.src.common.PHConstants;

public class SampleApp extends ListActivity {
	/** Simple class for holding a request title and url type.*/
	public static class DemoRequest implements DetailAdapter.DetailObject {
		public String title;
		public String requestURL;
		
		public DemoRequest(String title, String requestURL) {
			this.title = title;
			this.requestURL = requestURL;
		}
		
		//-------------------
		// Detail Adapter Methods
		public String getTitle() {
			return title;
		}
		
		public String getDetail() {
			return requestURL;
		}
		public View getView() {
			return null;
		}
	}
	
	private ArrayList<DemoRequest> requests;

	/** Creates the top bar with token and secret key edittext, etc.*/
	private void setupTopBar() {
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.sample_header, null);
		// add to top bar
		getListView().addHeaderView(view);
	}

	private void setupTokenAndKey() {
		EditText tokenEditText = (EditText) findViewById(R.id.editTextToken);
		String tokenValue = tokenEditText.getText().toString();
		EditText secretKeyEditText = (EditText) findViewById(R.id.editTextSecretKey);
		String secretValue = secretKeyEditText.getText().toString();
		if (secretValue.length() == 0 && tokenValue.length() == 0)
			PHConstants.setKeys("", "");
		else
			PHConstants.setKeys(tokenValue, secretValue);
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle("Playhaven SDK");
        
        setupTopBar();

        // setup debug environment
        PHConstants.Development dev_environ = new PHConstants.Development();

    	TextView versionText = (TextView) findViewById(R.id.versionText);
    	String sdkVersion = PHConstants.getSDKVersion();
    	versionText.setText("SDK Version: "+sdkVersion);

        createDemoRequests();
        
        setListAdapter(new DetailAdapter<DemoRequest>(this, R.layout.row, requests));
        
        getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				itemTapped(position);
			}
		});
    }
    
    
    /** called when the users taps a request row*/
	private void itemTapped(int position) {
		DemoRequest request = requests.get(position - 1);	// subtract 1 because of new header add for token/secret key
		if(request.title.equals("Open")) {
			setupTokenAndKey();
			Intent intent = new Intent(this, PublisherOpenView.class);
			startActivity(intent);
		} else if(request.title.equals("Content")) {
			setupTokenAndKey();
			Intent intent = new Intent(this, PublisherContentView.class);
			startActivity(intent);
		}
	}
    private void createDemoRequests() {
    	// create the demo requests
        requests = new ArrayList<DemoRequest>();
        requests.add(new DemoRequest("Open", "/publisher/open/"));
        requests.add(new DemoRequest("Content", "/publisher/content/"));

    }
}