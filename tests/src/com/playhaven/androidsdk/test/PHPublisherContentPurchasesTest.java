package com.playhaven.androidsdk.test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playhaven.sampleapp.PublisherContentView;
import com.playhaven.src.additions.ObjectExtensions;
import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.common.PHStringUtil;
import com.playhaven.src.publishersdk.content.PHContent;
import com.playhaven.src.publishersdk.content.PHContentView;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest.PHDismissType;
import com.playhaven.src.publishersdk.content.PHContentView.PHPurchaseKey;

import junit.framework.Assert;
import android.test.ActivityInstrumentationTestCase2;

public class PHPublisherContentPurchasesTest extends ActivityInstrumentationTestCase2<PublisherContentView> implements PHPublisherContentRequest.PHPublisherContentRequestDelegate {

	public PublisherContentView contentView;

	public PHPublisherContentPurchasesTest() {  
		super("com.playhaven.sampleapp.PublisherContentView", PublisherContentView.class);  
	}  

	protected void setUp() throws Exception {
		super.setUp();

		contentView = getActivity();
		PHConstants.findDeviceInfo(contentView);
        PHConstants.setKeys("zombie7", "haven1");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testValidation() {

		String product = "com.playhaven.slappycoins";
		String quantity = "1234";
		String receipt = "102930193";
		String name = "Coins";
		String generatedSigString = String.format("%s:%s:%s:%s:%s:%s", product, name, quantity,
				PHConstants.getUniqueID(), receipt, PHConstants.getSecret());
		PHConstants.phLog("Generated signature for purchase: "+generatedSigString);
		String signature = PHStringUtil.hexDigest(generatedSigString);

		try {
			JSONObject purchaseDict = new JSONObject();
			purchaseDict.putOpt("product", product);
			purchaseDict.putOpt("name", name);
			purchaseDict.putOpt("quantity", quantity);
			purchaseDict.putOpt("receipt", receipt);
			purchaseDict.putOpt("signature", signature);

			PHContentView view = new PHContentView();
			Assert.assertTrue(view.validatePurchase(purchaseDict));			

			JSONObject badPurchaseDict = new JSONObject();
			purchaseDict.putOpt("product", product);
			badPurchaseDict.putOpt("name", name);
			badPurchaseDict.putOpt("quantity", quantity);
			badPurchaseDict.putOpt("receipt", receipt);
			badPurchaseDict.putOpt("signature", "BAD_SIGNATURE_RARARA");
			Assert.assertFalse(view.validatePurchase(badPurchaseDict));			

		} catch(JSONException ex) {
			ex.printStackTrace();
		}
	}

	public void testPurchaseDispatch() {
		// test purchases dispatch
		PHContentView contentView = new PHContentView();
		contentView.redirectRequest("ph://purchase", this, "handlePurchases");
	}

	public void testPurchaseReportResolution() {
		// *** Need to Implement. Also, need to simulate the network like iOS does before do this ***
		// test reporting a purchase resolution. Verify PHContentView is BroadcastReceiver
		// ContentViewsPurchaseSendCallback() is called
	}

	public boolean validatePurchase(JSONObject data) {
		String purchase = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.ProductIDKey.key());
		String name = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.NameKey.key());
		String quantity = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.QuantityKey.key());
		String receipt = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.ReceiptKey.key());
		PHConstants.phLog("Receipt for purchase unlock: "+receipt);
		String signature = ObjectExtensions.JSONExtensions.getJSONString(data, PHPurchaseKey.SignatureKey.key());
		String generatedSigString = String.format("%s:%s:%s:%s:%s", purchase,
																	name,
																	quantity,
																	PHConstants.getUniqueID(),
																	receipt,
																	PHConstants.getSecret());
		
		PHConstants.phLog("Generated signature for purchase: "+generatedSigString);
		
		String generatedSig = PHStringUtil.hexDigest(generatedSigString);
		
		//check that the signature passed in matches ours.
		return (generatedSig.equalsIgnoreCase(signature));
	}

	public void handlePurchases(JSONObject context, String callback, PHContentView source) {
		JSONArray purchasesArray = ObjectExtensions.JSONExtensions.getJSONArray(context, "purchases");
		for (int i =0; i<purchasesArray.length(); i++) {
			JSONObject purchaseData = ObjectExtensions.JSONExtensions.getJSONObject(purchasesArray, i);
			Assert.assertTrue(validatePurchase(purchaseData));			
		}
	}

	@Override
	public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestFailed(PHAPIRequest request, Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void willGetContent(PHPublisherContentRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void willDisplayContent(PHPublisherContentRequest request,
			PHContent content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didDisplayContent(PHPublisherContentRequest request,
			PHContent content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didDismissContent(PHPublisherContentRequest request,
			PHDismissType type) {
		// TODO Auto-generated method stub
		
	}

}
