package com.playhaven.src.publishersdk.content;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/** Simple container class to hold purchase meta data. It is parcelable so that we can pass between activities.*/
public class PHPurchase implements Parcelable {
	
    public String product;
    
    public String name;
    
    public int quantity;
    
    public String receipt;
    
    public String callback;

    
    public enum Resolution {
    	
    	Buy 	("buy"),
    	Cancel  ("cancel"),
    	Error 	("error");
    	
    	private String type;
    	
    	private Resolution(String type) {
    		this.type = type;
    	}
    	
    	public String getType() {
    		return type;
    	}
    	
	}
	
	public PHPurchase() {
		//Default constructor
	}

	/**
	 * @warning THIS IS CRAP! There is no clear encapsulation and it gives a dumb data object far too much power.
	 * This needs to be refactored *out* of this class into some logic controller somewhere. God.
	 * @param resolution
	 */
    public void reportResolution(Resolution resolution) {
		JSONObject response = new JSONObject();
		JSONObject callbackData = new JSONObject();

		try {
			response.put("resolution", resolution.getType());
			callbackData.put("callback", this.callback);
			callbackData.put("response", response.opt("response"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

        Intent broadcastPurchaseResolution = new Intent();
        broadcastPurchaseResolution.setAction(PHContentView.PHBroadcastReceiverKey.Action.getKey());
        broadcastPurchaseResolution.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastPurchaseResolution.putExtra(PHContentView.PHBroadcastReceiverKey.Event.getKey(), PHContentView.PHBroadcastReceiverEvent.ContentViewsPurchaseSendCallback.getKey());
        broadcastPurchaseResolution.putExtra(PHContentView.PHBroadcastReceiverEvent.PurchaseResolution.getKey(), callbackData.toString());
        //TODO: will not be renabled until we figure out a better way of doing this! This is a dumb data object and should have no reference to it's parent..
        //PHContentView.getContext().sendBroadcast(broadcastPurchaseResolution);
    }

    ////////////////////////////////////////////////////
	////////////////// Parcelable Methods //////////////
	public static final Parcelable.Creator<PHPurchase> CREATOR = new Creator<PHPurchase>() {
		
		@Override
		public PHPurchase[] newArray(int size) {
			return new PHPurchase[size];
		}
		
		@Override
		public PHPurchase createFromParcel(Parcel source) {
			return new PHPurchase(source);
		}
	};
	
	public PHPurchase(Parcel in) {
		this.product = in.readString();
		this.name = in.readString();
		this.quantity = in.readInt();
		this.receipt = in.readString();
		this.callback = in.readString();
	}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(product);
		out.writeString(name);
		out.writeInt(quantity);
		out.writeString(receipt);
		out.writeString(callback);
	}
}

