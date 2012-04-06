package com.playhaven.src.publishersdk.purchases;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.publishersdk.content.PHPurchase;

public class PHPublisherIAPTrackingRequest extends PHAPIRequest {

	public static final String PHPurchaseFromStoreGoogleMarketPlace = "GoogleMarketplace";
	public static final String PHPurchaseFromStoreAmazonAppstore = "AmazonAppstore";
	public static final String PHPurchaseFromStorePaypal = "Paypal";
	public static final String PHPurchaseFromStoreCrossmo = "Crossmo";	// China only
	public static final String PHPurchaseFromStoreMotorolaAppstor = "MotorolaAppstore";	// China only

	/*
	 * The Publisher is expected to set these values if they want Play Haven to track and report
	 * IAP content.
	 */
    public String product = "";
    public Integer quantity = 0;
    public String purchasePrice = "0";
    public String store = PHPurchaseFromStoreGoogleMarketPlace;
    public Integer error = 0;
    public Locale purchasePriceLocale = Locale.getDefault();
    public PHPurchase.PHPurchaseResolutionType resolution;

	public PHPublisherIAPTrackingRequest(PHAPIRequestDelegate delegate) {
		super(delegate);
	}

	public PHPublisherIAPTrackingRequest(PHAPIRequestDelegate delegate, Integer sendError) {
		super(delegate);
		error = sendError;
	}

	static private Map<String, String> listOfCookies;

	public static void setConversionCookie(String cookie, String product) {
		listOfCookies.put(product, cookie);
	}

	public String getConversionCookieForProduct(String product) {
		String result = listOfCookies.get(product);
		return result;
	}

	@Override
	public String baseURL() {
		return PHConstants.phURL("/v3/publisher/iap/");
	}

	@Override
	public HashMap<String, String> getAdditionalParams() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("product", this.product);
		params.put("quantity", this.quantity.toString());
		params.put("resolution", this.resolution.toString());
		params.put("price", this.purchasePrice);
		if (error != 0)
			params.put("error", error.toString());
		params.put("price_locale", purchasePriceLocale.getDisplayCountry());
		params.put("store", store);
		params.put("cookie", getConversionCookieForProduct(this.product));
		return params;
	}
}
