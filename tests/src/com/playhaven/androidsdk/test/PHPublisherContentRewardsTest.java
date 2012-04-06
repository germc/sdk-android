package com.playhaven.androidsdk.test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playhaven.src.additions.ObjectExtensions;
import com.playhaven.src.common.PHAPIRequest;
import com.playhaven.src.common.PHConstants;
import com.playhaven.src.common.PHStringUtil;
import com.playhaven.src.publishersdk.content.PHContent;
import com.playhaven.src.publishersdk.content.PHContentView;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest;
import com.playhaven.src.publishersdk.content.PHPublisherContentRequest.PHDismissType;
import com.playhaven.src.publishersdk.content.PHContentView.PHRewardKey;

import junit.framework.Assert;
import android.test.AndroidTestCase;

public class PHPublisherContentRewardsTest extends AndroidTestCase implements PHPublisherContentRequest.PHPublisherContentRequestDelegate {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testValidation() {

		String reward = "SLAPPY_COINS";
		String quantity = "1234";
		String receipt = "102930193";
		String generatedSigString = String.format("%s:%s:%s:%s:%s", reward, quantity,
													PHConstants.getUniqueID(), receipt, PHConstants.getSecret());
		PHConstants.phLog("Generated signature for reward: "+generatedSigString);
		String signature = PHStringUtil.hexDigest(generatedSigString);

		try {
			JSONObject rewardDict = new JSONObject();
			rewardDict.putOpt("reward", reward);
			rewardDict.putOpt("quantity", quantity);
			rewardDict.putOpt("receipt", receipt);
			rewardDict.putOpt("signature", signature);

		    PHConstants.setKeys("zombie7", "haven1");
			PHContentView view = new PHContentView();
			Assert.assertTrue(view.validateReward(rewardDict));			

			JSONObject badRewardDict = new JSONObject();
			badRewardDict.putOpt("reward", reward);
			badRewardDict.putOpt("quantity", quantity);
			badRewardDict.putOpt("receipt", receipt);
			badRewardDict.putOpt("signature", "BAD_SIGNATURE_RARARA");
			Assert.assertFalse(view.validateReward(badRewardDict));			

		} catch(JSONException ex) {
			ex.printStackTrace();
		}
	}

	public void testRewardDispatch() {
		// test rewards dispatch
		PHContentView contentView = new PHContentView();
		contentView.redirectRequest("ph://reward", this, "handleRewards");
	}
	
	public boolean validateReward(JSONObject data) {
		String reward = ObjectExtensions.JSONExtensions.getJSONString(data, PHRewardKey.IDKey.key());
		String quantity = ObjectExtensions.JSONExtensions.getJSONString(data, PHRewardKey.QuantityKey.key());
		String receipt = ObjectExtensions.JSONExtensions.getJSONString(data, PHRewardKey.ReceiptKey.key());
		PHConstants.phLog("Receipt for reward unlock: "+receipt);
		String signature = ObjectExtensions.JSONExtensions.getJSONString(data, PHRewardKey.SignatureKey.key());
		String generatedSigString = String.format("%s:%s:%s:%s:%s", reward,
																	quantity,
																	PHConstants.getUniqueID(),
																	receipt,
																	PHConstants.getSecret());
		
		PHConstants.phLog("Generated signature for reward: "+generatedSigString);
		
		String generatedSig = PHStringUtil.hexDigest(generatedSigString);
		
		//check that the signature passed in matches ours.
		return (generatedSig.equalsIgnoreCase(signature));
	}

	public void handleRewards(JSONObject context, String callback, PHContentView source) {
		JSONArray rewardsArray = ObjectExtensions.JSONExtensions.getJSONArray(context, "rewards");
		for (int i =0; i<rewardsArray.length(); i++) {
			JSONObject rewardData = ObjectExtensions.JSONExtensions.getJSONObject(rewardsArray, i);
			Assert.assertTrue(validateReward(rewardData));			
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
