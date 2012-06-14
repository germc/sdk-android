package com.playhaven.sampleapp;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.playhaven.androidsdk.R;
import com.playhaven.sampleapp.examples.ExampleView;
import com.playhaven.sampleapp.examples.PublisherContentView;
import com.playhaven.sampleapp.examples.PublisherIAPView;
import com.playhaven.sampleapp.examples.PublisherOpenView;
import com.playhaven.src.common.PHConfig;

public class SampleApp extends ListActivity {
	/** Simple class for holding a request title and url type. Static class to avoid grabbing
	 * reference to activity and causing memory leak*/
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle("Playhaven SDK: " + PHConfig.sdk_version);
       
        createDemoRequests();
        
        setListAdapter(new DetailAdapter<DemoRequest>(this, R.layout.row, requests));
        
        getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				itemTapped(position);
			}
		});
    }
    
    
	private void itemTapped(int position) {
		DemoRequest request = requests.get(position);
		
		if(request.title.equals("Open"))
			startExampleActivity(PublisherOpenView.class);
		
		else if(request.title.equals("Content"))
			startExampleActivity(PublisherContentView.class);
		
		else if (request.title.equals("IAP"))
			startExampleActivity(PublisherIAPView.class);
		
	}
	
	private void startExampleActivity(Class<? extends ExampleView> cls) {
		PHConfig.token  = "your token";
        PHConfig.secret = "your secret";
        PHConfig.api = "http://api2.playhaven.com";
        
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}
	
    private void createDemoRequests() {
    	// create the demo requests
        requests = new ArrayList<DemoRequest>();
        requests.add(new DemoRequest("Open", "/publisher/open/"));
        requests.add(new DemoRequest("Content", "/publisher/content/"));
        requests.add(new DemoRequest("IAP", "/publisher/iap/"));

    }
}