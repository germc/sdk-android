package com.playhaven.src.publishersdk.purchases;

import java.util.Currency;
import java.util.Hashtable;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHError;
import com.playhaven.src.publishersdk.content.PHPurchase;

public class PHPublisherIAPTrackingRequest extends PHAPIRequest {
	
	public enum PHPurchaseOrigin {
		Google			 ("GoogleMarketplace"),
		Amazon		 	 ("AmazonAppstore"),
		Paypal 			 ("Paypal"),
		Crossmo			 ("Crossmo"),
		Motorola		 ("MotorolaAppstore");
		
		private String originStr;
		
		private PHPurchaseOrigin(String originStr) {
			this.originStr = originStr;
		}
		
		public String getOrigin() {
			return this.originStr;
		}
	}
	
	//////////////////////////////////////////////////////
	//////////////// Member Variables ////////////////////
	
	/**
	 * Publisher should set these values.
	 */
    public String product = ""; // make sure not null to avoid error
    
    public int quantity = 0;
    
    public double price = 0; // Note: this is currently ignored by the server
    
    public PHPurchaseOrigin store = PHPurchaseOrigin.Google;
    
    public PHError error;
    
    public Currency currencyLocale; // Note: this is currently ignored by the serve
    
    public PHPurchase.Resolution resolution = PHPurchase.Resolution.Cancel; // default to cancel?

    static private Hashtable<String, String> cookies = new Hashtable<String, String>();
    
    public PHPublisherIAPTrackingRequest(Context context) {
    	super(context);
    }
    
	public PHPublisherIAPTrackingRequest(Context context, Delegate delegate) {
		super(context, delegate);
		this.error = null;
	}

	public PHPublisherIAPTrackingRequest(Context context, PHError error) {
		super(context);
		this.error = error;
	}
	
	public PHPublisherIAPTrackingRequest(Context context,String product_id, int quantity, PHPurchase.Resolution resolution) {
		super(context);
		
		this.product = product_id;
		this.quantity = quantity;
		this.resolution = resolution;
		this.error = null;
	}
	
	
	//////////////////////////////////////////////////////////////
	//////////////////////// Cookie Method ///////////////////////
	
	public static void setConversionCookie(String product, String cookie) {
		if (JSONObject.NULL.equals(cookie) || cookie.length() == 0) return;
		
		cookies.put(cookie, product);
	}

	public String getConversionCookie(String product) {
		String cookie =  cookies.get(product);
		cookies.remove(product); // use once and self destruct!
		return cookie; 
	}

	//////////////////////////////////////////////////////////////
	////////////////////////// Overrides /////////////////////////
	@Override
	public String baseURL() {
		return super.createAPIURL("/v3/publisher/iap/");
	}

	@Override
	public Hashtable<String, String> getAdditionalParams() {
		// always refresh locale
		currencyLocale = Currency.getInstance(Locale.getDefault()); // gotta love Java?
		
		// unfortunately, we have to ensure not-null for all values. Should we warn publishers?
		Hashtable<String, String> purchaseData = new Hashtable<String, String>();
		
		purchaseData.put("product", (product != null ? product : ""));
		purchaseData.put("quantity", Integer.toString(this.quantity));
		purchaseData.put("resolution", (resolution != null ? resolution.getType() : ""));
		purchaseData.put("price", Double.toString(this.price)); // why are we even sending this?
		
		if (error != null && error.getErrorCode() != 0)
			purchaseData.put("error", Integer.toString(error.getErrorCode()));
		
		purchaseData.put("price_locale", (currencyLocale != null ? currencyLocale.getCurrencyCode() : ""));
		purchaseData.put("store", (store != null ? store.getOrigin() : null));
		
		String cookie = getConversionCookie(this.product);
		purchaseData.put("cookie", (cookie != null ? cookie : "")); // avoid a null value
		
		return purchaseData;
	}
}
