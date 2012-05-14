package com.playhaven.src.common;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;

/** Represents a simple loading dialog which displays an spinner while loading from a url.
 * 
 * Note: while it works, I think the coupling between the view and "model" is a bit too tight.
 * It doesn't seem good to start a new thread from the view.
 * 
 * The delegate you specify gets all the {@link PHAsyncRequest#delegate} messages.
 */
public class PHURLLoaderView extends ProgressDialog implements PHURLLoader.PHURLLoaderDelegate {
	private Uri targetUri;
	
	public PHURLLoader.PHURLLoaderDelegate delegate;
	
	/** We use a PHURLLoader so that we can open the final url on the device.*/
	private PHURLLoader loader;
	
	/** Maintain a copy of the json context.*/
	private JSONObject json_context;
	
	private Context context;
	
	/** forward to super class*/	
	public PHURLLoaderView(PHURLLoader.PHURLLoaderDelegate delegate, Context context) {
		super(context, ProgressDialog.STYLE_SPINNER);
		this.delegate = delegate;
		this.context = context;
		
		setMessage("Loading..");
	}
	
	///////////////////////////////////////////////////////////
	////////////////////// Accessors //////////////////////////

	public void setJSONContext(JSONObject context) {
		json_context = context;
		if (loader != null) loader.setJSONContext(context);
	}

	public JSONObject getJSONContext() {
		return (loader != null ? loader.getJSONContext() : json_context);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		//TODO: possible customization in the future
	}
	
	@Override
	public void show() {
		if(targetUri == null) {
			return;
		}
		
		// start async loader
		loader = new PHURLLoader(context, this);
		loader.setJSONContext(json_context);
		loader.targetURI = targetUri.toString();
		loader.open();
		
		super.show();
	}
	
	public void setTargetURL(String target) {
		targetUri = Uri.parse(target);
	}
	//------------------------
	//PHURLLoader callbacks

	public void loaderFinished(PHURLLoader loader) {
		delegate.loaderFinished(loader); //forward on...
		
		dismiss();
	}

	public void loaderFailed(PHURLLoader loader) {
		delegate.loaderFailed(loader); //forward on..
		
		dismiss();
	}

	@Override
	public void redirectMarketURL(String url) {
		delegate.redirectMarketURL(url); //forward on..
		
	}
	
	
	
}
