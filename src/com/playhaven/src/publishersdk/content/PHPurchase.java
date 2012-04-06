package com.playhaven.src.publishersdk.content;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;
import com.playhaven.src.additions.ObjectExtensions;

/** Simple container class to hold purchase meta data. It is parcelable so that we can pass between activities.*/
public class PHPurchase implements Parcelable {
    public String productIdentifier;
    public String name;
    public String quantity;
    public String receipt;
    public String callback;

    public enum PHPurchaseResolutionType
    {
    	PHPurchaseResolutionBuy,
    	PHPurchaseResolutionCancel,
    	PHPurchaseResolutionError
	}
	
	public PHPurchase() {
		//Default constructor
	}
	
    public String stringForResolution(PHPurchaseResolutionType resolution) {
        String result = "error";
        switch (resolution) {
            case PHPurchaseResolutionBuy:
                result = "buy";
                break;
                
            case PHPurchaseResolutionCancel:
                result = "cancel";
                break;
                
            default:
                result = "error";
                break;
        }
        
        return result;
    }

    public void reportResolution(PHPurchaseResolutionType resolution) {
		JSONObject response = new JSONObject();
		ObjectExtensions.JSONExtensions.setJSONString(response, "resolution", this.stringForResolution(resolution));
		
		JSONObject callbackDictionary = new JSONObject();
		ObjectExtensions.JSONExtensions.setJSONString(callbackDictionary, "callback", this.callback);
		ObjectExtensions.JSONExtensions.setJSONString(callbackDictionary, "response", ObjectExtensions.JSONExtensions.getJSONString(response, "response"));

        Intent broadcastPurchaseResolution = new Intent();
        broadcastPurchaseResolution.setAction(PHContentView.PHBroadcastReceiverKey.Action.getKey());
        broadcastPurchaseResolution.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastPurchaseResolution.putExtra(PHContentView.PHBroadcastReceiverKey.Event.getKey(), PHContentView.PHBroadcastReceiverEvent.ContentViewsPurchaseSendCallback.getKey());
        broadcastPurchaseResolution.putExtra(PHContentView.PHBroadcastReceiverEvent.PurchaseResolution.getKey(), callbackDictionary.toString());
        PHContentView.getContext().sendBroadcast(broadcastPurchaseResolution);
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
		this.productIdentifier = in.readString();
		this.name = in.readString();
		this.quantity = in.readString();
		this.receipt = in.readString();
		this.callback = in.readString();
	}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(productIdentifier);
		out.writeString(name);
		out.writeString(quantity);
		out.writeString(receipt);
		out.writeString(callback);
	}
}

