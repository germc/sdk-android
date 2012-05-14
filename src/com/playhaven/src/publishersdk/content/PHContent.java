package com.playhaven.src.publishersdk.content;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.playhaven.src.common.PHConstants;

/** Represents some abstract content from the advertising servers. It implements the Parcelable class to ensure
 * that we can pass between activities via the broadcast manager. */
public class PHContent implements Parcelable {
	public enum TransitionType {
		Unknown, Modal, Dialog, Type
	};

	public TransitionType transition = TransitionType.Modal;
	public String closeURL;

	public JSONObject context;

	public Uri url;

	public double closeButtonDelay;

	private HashMap<String, JSONObject> frameDict = new HashMap<String, JSONObject>();

	/** Regular simple constructor */
	public PHContent() {
		closeButtonDelay = 10.0;
		transition = TransitionType.Modal;
	}

	/** Create new PHContent from server json representation */
	public PHContent(JSONObject dict) {
		fromJSON(dict);
	}
	
	/** Creates from Parcel*/
	public PHContent(Parcel in) {
		transition = TransitionType.valueOf(in.readString());
		closeURL = in.readString();
		
		try {
			context = new JSONObject(in.readString());
		} catch (JSONException e) {
			PHConstants.phLog("Error hydrating PHContent JSON context from Parcel: "+e.getLocalizedMessage());
		}
		
		url = Uri.parse(in.readString());
		closeButtonDelay = in.readDouble();
		
		Bundle frameBundle = in.readBundle();
		for (String key : frameBundle.keySet()) {
			try {
				frameDict.put(key, new JSONObject(frameBundle.getString(key)));
			} catch (JSONException e) {
				PHConstants.phLog("Error deserializing frameDict from bundle in PHContent");
			}
			
		}
	}

	/**
	 * Required Static creator (factory) for loading ourselves from a parcel.
	 */
	public static final Parcelable.Creator<PHContent> CREATOR = 
			new Parcelable.Creator<PHContent>() {
				public PHContent createFromParcel(Parcel in) {
					return new PHContent(in);
				}
				
				public PHContent[] newArray(int size) {
					return new PHContent[size];
				}
			};
	/** Attempts to load properties from the specified JSONObject.
	 * @return true on success, or false on failure.
	 * @param dict the actual json response from the server.
	 */
	public boolean fromJSON(JSONObject dict) {
		try {
			if (dict.has("frame") 
					&& dict.has("url") 
					&& dict.has("transition")) {

				Object frame = dict.opt("frame");
				frameDict.clear();
				if (frame instanceof String)
					frameDict.put((String) frame, new JSONObject(String.format("{\"%s\" : \"%s\"}", frame, frame)));
				else if (frame instanceof JSONObject)
					setFrameDict((JSONObject) frame);

				String url = dict.optString("url");
				this.url = (url != null ? Uri.parse(url) : null);

				String transition = dict.optString("transition");
				if (transition != null) {
					if (transition.equals("PH_MODEL"))
						this.transition = TransitionType.Modal;
					else if (transition.equals("PH_DIALOG"))
						this.transition = TransitionType.Dialog;
					else
						this.transition = null;
				}

				this.context = dict.optJSONObject("context");
				 
				this.closeButtonDelay = dict.optDouble("close_delay");
				
				this.closeURL = dict.optString("close_ping");
				
				return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void setFrameDict(JSONObject frame) {
		frameDict.clear();
		
		try {
			//Note: warning is pointless..'keys' is an iterator of Strings
			@SuppressWarnings("unchecked")
			Iterator<String> keys = frame.keys();
			
			while (keys.hasNext()) {
				String key = keys.next();
				// could be JSONObject or String
				frameDict.put(key, (JSONObject)frame.get(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public RectF getFrame(int orientation) {
		String orientKey = (orientation == Configuration.ORIENTATION_LANDSCAPE ? "PH_LANDSCAPE"	: "PH_PORTRAIT");
		
		if (frameDict.containsKey("PH_FULLSCREEN")) {
			// create infinitely large values to signify screen fill
			return new RectF(0,0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		} else if (frameDict.containsKey(orientKey)) {
			float x,y,w,h;
			x = 0f;
			y = 0f;
			w = 0f;
			h = 0f;

			JSONObject dict = (JSONObject)frameDict.get(orientKey);
			if (dict != null) {
				x = (float)dict.optDouble("x");
				y = (float)dict.optDouble("y");
				w = (float)dict.optDouble("w");
				h = (float)dict.optDouble("h");
				return new RectF(x,y,x+w,y+h);
			}

		}

		return new RectF(0.0f,0.0f,0.0f,0.0f);
	}
	
	@Override
	public String toString() {
		String formattedJson = "(NULL)";
		try {
			//convert to indented with 2 spaces for indent
			formattedJson = context.toString(2);
		} catch (JSONException e) {
			e.printStackTrace();
			formattedJson = "(NULL)"; //just in case
		}
		
		
		String message = String.format("Close URL: %s - Close Delay: %.1f - URL: %s\n\n---------------------------------\nJSON Context: %s", 
				closeURL, closeButtonDelay, url, formattedJson);
		return message;
	}
	
	
	
	////////////////////////////////////////////////////////
	/////////////////// Parcelable Methods /////////////////
	
	public int describeContents() {
		return 0; // no files descriptors..
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(transition.name());
		out.writeString(closeURL);
		out.writeString(context.toString());
		out.writeString(url.toString());
		out.writeDouble(closeButtonDelay);
		
		// convert JSONObject to string representation
		Bundle frameBundle = new Bundle();
		for (String key : frameDict.keySet()) {
			frameBundle.putString(key, frameDict.get(key).toString());
		}
		
		out.writeBundle(frameBundle);
	}
}
