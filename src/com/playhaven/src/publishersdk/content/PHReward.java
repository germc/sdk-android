package com.playhaven.src.publishersdk.content;

import android.os.Parcel;
import android.os.Parcelable;

/** Simple container class to hold reward meta data. It is parcelable so that we can pass between activities.*/
public class PHReward implements Parcelable {
	public String name;
	
	public String quantity;
	
	public String receipt;
	
	public PHReward() {
		//Default constructor
	}
	
	////////////////////////////////////////////////////
	////////////////// Parcelable Methods //////////////
	public static final Parcelable.Creator<PHReward> CREATOR = new Creator<PHReward>() {
		
		@Override
		public PHReward[] newArray(int size) {
			return new PHReward[size];
		}
		
		@Override
		public PHReward createFromParcel(Parcel source) {
			return new PHReward(source);
		}
	};
	
	public PHReward(Parcel in) {
		this.name = in.readString();
		this.quantity = in.readString();
		this.receipt = in.readString();
	}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeString(quantity);
		out.writeString(receipt);
	}
}
