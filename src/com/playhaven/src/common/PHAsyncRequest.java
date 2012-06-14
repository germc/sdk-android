package com.playhaven.src.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import com.playhaven.src.utils.PHStringUtil;

/**
 * Represents an asynchronous network request and mostly used in {@link PHAPIRequest} and {@link PHURLLoader}.
 * 
 * You can set the http parameters, uri, and add post parameters. Make sure you
 * set the delegate when using this class.
 * 
 * You can also control how many times we handle redirects. After the set amount of redirects,
 * we behave exactly like we just failed. We have two separate set of redirects tracked. The ones happening
 * in the {@link #PHHttpConn} and the ones happening in the {@link #PHAsyncRequest}. There are very few occasions
 * where we actually redirect in PHAsyncRequest, but we do have the capability. Thus, methods like {@link #getLastRedirect()}
 * always give priority to the PHHttpCon client.
 * 
 * You can also utilize basic http auth using the {@link setUsername} and {@link setPassword}.
 * @author samuelstewart
 * 
 */
public class PHAsyncRequest extends AsyncTask<Uri, Integer, ByteBuffer> {
	
	public static final int INFINITE_REDIRECTS = Integer.MAX_VALUE;
	
	// for unit testing
	private CountDownLatch signal;

	public Uri url;

	public enum RequestType {
		Post, Get, Put, Delete
	};

	public RequestType request_type;

	private Exception lastError;

	private int responseCode;
	
	private ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
	
	private boolean isDownloading = true;
		
	private PHHttpConn client;

	@SuppressWarnings("unused")
	private String username;
	
	@SuppressWarnings("unused")
	private String password;

	public HttpParams params;

	private long requestStart;
	
	/** Simple class that provides our http connection. We use it to divorce dependencies
	 * and for unit testing (Dependancy Injection). PHHttpConn supports basic HTTP Auth as well.
	 */
	public static class PHHttpConn {
		protected DefaultHttpClient client;
		
		private int max_redirects = INFINITE_REDIRECTS;
		
		private String username;
		
		private String password;
		
		private int totalRedirects = 0;
		
		private ArrayList<String> redirectUrls = new ArrayList<String>();
		
		private HttpUriRequest cur_request;
		
		/** Our custom redirect handler. On some android versions, this doesn't work
		 * which is why we move our main logic to {@link shouldRedirect} so clients can call
		 * directly.
		 * @author samuelstewart
		 *
		 */
		private class PHRedirectHandler extends DefaultRedirectHandler {
			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
				URI redirectURI = super.getLocationURI(response, context);
				
				addRedirectUrl(redirectURI.toString());
				
				return redirectURI; // return same url but we've added it to the list
			}
			@Override
			public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
				return shouldRedirect(response);
			}
		}
		protected void setMaxRedirect(int max) {
			max_redirects = max;
		}
		
		///////////////////////////
		public PHHttpConn() {
			client = new DefaultHttpClient(enableRedirecting(null));
			//set our hook into the redirect handler
			client.setRedirectHandler(new PHRedirectHandler());
		}
		public PHHttpConn(HttpParams params) {
			client = new DefaultHttpClient();
			client.setParams(enableRedirecting(params));
		}

		////////////////////////////////////////
		/////// Redirect Methods ///////////////
		public String getLastRedirect() {
			if(redirectUrls.size() == 0) return null;
			
			return redirectUrls.get(redirectUrls.size()-1);
		}
		
		public void addRedirectUrl(String url) {
			redirectUrls.add(url);
		}
		
		public void clearRedirects() {
			redirectUrls.clear();
		}
		
		/** gets the redirect location from response. If not redirect response, returns null.*/
		public String getRedirectLocation(HttpResponse response) {
			if(isRedirectResponse(response.getStatusLine().getStatusCode())) {
				Header[] headers = response.getHeaders("Location");
				
				if(headers != null && headers.length != 0) {
					return headers[0].getValue();
				}
			}
			return null;
		}
		
		/** Checks to see if status code is redirect request or not.*/
		private boolean isRedirectResponse(int code) {
			return (code >= 300 && code <= 307);
		}
		
		/** 
		 * Should we redirect or have we hit the maximum number of redirects?
		 * @param response the http response from the server
		 * @return true if we should redirect, false otherwise
		 */
		public boolean shouldRedirect(HttpResponse response) {
			if(isRedirectResponse(response.getStatusLine().getStatusCode()))
				return (++totalRedirects <= max_redirects);
			
			// not a redirect response anyway..
			return false;
		}
		
		/** Turn on redirecting using existing parameters or new ones*/
		private HttpParams enableRedirecting(HttpParams params) {
			if(params == null)
				params = new BasicHttpParams();

			params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			params.setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true); //force circular redirects...
			HttpClientParams.setRedirecting(params, true);
			return params;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		
		public void setPassword(String password) {
			this.password = password;
		}
		
		/** Wrapper method so that we can mock if necessary*/
		public HttpResponse start(HttpUriRequest request) throws IOException {
			cur_request = request;

			totalRedirects = 0;
			clearRedirects();
			
			// use http auth if available
			if (username != null && password != null) {
				String encodedCredentials = Base64.encodeToString((username+":"+password).getBytes(), Base64.DEFAULT);
				String authStr = String.format("Basic %s", encodedCredentials);
				
				request.setHeader("Authorization", "Basic "+authStr);
			}
			
			return client.execute(request);
		}
		
		public void cancel() {
			synchronized (this) {
				try {
					if (cur_request != null)
						cur_request.abort();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					client.getConnectionManager().shutdown();
					cur_request = null;
				}
			}
			
		}
	}
	
	/** Delegate interface. All calls will be on the main UI thread */
	public static interface Delegate {
		public void requestFinished(ByteBuffer response);

		public void requestFailed(Exception e);
		
		public void requestResponseCode(int responseCode);
		
		/** Called when request progress updates. Passes in progress with a value between
		 * 0 and 100.
		 * @param progress
		 */
		public void requestProgressUpdate(int progress);
	}

	private Delegate delegate;

	public PHAsyncRequest(Delegate delegate) {
		this.delegate = delegate;
		client = new PHHttpConn();
		request_type = RequestType.Get;
	}
	
	public void setMaxRedirects(int max) {
		client.setMaxRedirect(max);
	}
	
	protected void addPostParam(String key, String value) {
		postParams.add(new BasicNameValuePair(key, value));
	}
	
	protected void addPostParams(Hashtable<String, String> params) {
		if (params == null) return;
		
		postParams.clear();
		
		for (Map.Entry<String, String> entry : params.entrySet())
			postParams.add(new BasicNameValuePair(entry.getKey(), 
												  entry.getValue()));
		
	}
	
	/** Get the final url we arrived at */
	public String getLastRedirectURL() {
		return client.getLastRedirect();
	}
	
	/** We only take the first uri, so don't bother passing in more than one. */
	protected ByteBuffer doInBackground(Uri... urls) {
		return execRequest(urls);
	}
	
	/** Moved into supporting method so that we can call ourselves recursively on redirects.*/
	private ByteBuffer execRequest(Uri... urls) {
		ByteBuffer buffer = null;
		
		synchronized (this) {
		try { // this block swallows *all* worst case exceptions
			isDownloading = true;
			lastError = null;
			
			requestStart = System.currentTimeMillis();
			
			client.clearRedirects();
			
			if (urls.length > 0) {
				Uri url = urls[0];
				
				// always prefer the url explicitly set
				if(!url.equals(this.url) && this.url != null)
					url = this.url;
				
				HttpResponse response = null;
				try {
					if (isCancelled()) return null;
					
					// convert to java.net.uri (b/c we already have escaped the url and Http*** will encode it again.
					String net_uri = url.toString();
					
					// decide what time of connection this is
					if (request_type == RequestType.Post) {
						HttpPost request = new HttpPost(net_uri);
						// set the post fields..
						request.setEntity(new UrlEncodedFormEntity(postParams));

						response = client.start(request);

					} else if (request_type == RequestType.Get) {
						HttpGet request = new HttpGet(net_uri);
						response = client.start(request);
					} else {
						HttpGet request = new HttpGet(net_uri);
						response = client.start(request);
					}

					// try to grab http response entity (maybe json or image?)
					HttpEntity entity = response.getEntity();
					
					// grab the response code
					responseCode = response.getStatusLine().getStatusCode();
					
					// notify delegate with response code (-1 is the flag for response code)
					publishProgress(-1);
					
					if (isCancelled()) return null;
					
					if (entity != null) {
						InputStream in_stream = entity.getContent();

						buffer = readStream(in_stream);

						in_stream.close();
					}

				} catch (IOException e) {
					lastError = e;
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			PHCrashReport.reportCrash(e, "PHAsyncRequest - doInBackground", PHCrashReport.Urgency.critical);
		}
		}
		
		return buffer;
	}
	
	public void setUsername(String username) {
		this.username = username;
		client.setUsername(username);
	}
	
	public void setPassword(String password) {
		this.password = password;
		client.setPassword(password);
	}
	/** Allows for dependency injection of custom http connection.
	 * (basically for unit tests)*/
	public void setHttpClient(PHHttpConn client) {
		this.client = client;
	}
	
	/** Sets the count down signal latch. Mostly used for unit testing.*/
	public void setCountDownLatch(CountDownLatch signal) {
		synchronized(this) {
			if(!isDownloading)
				this.signal = signal;
		}
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		if(progress.length > 0) {
			int prog = progress[0];
			
			// we handle either passing true progress update or just HTTP response code.
			//TODO: this is somewhat strange, should be refactored to just pass in response code?
			// or signal (-1) should be refactored into constant
			if(prog == -1) {
				// post response code
				delegate.requestResponseCode(responseCode);
			} else if(prog > 0) {
				// TODO: handle progress update..
			}
		}
		
	}

	@Override
	protected void onCancelled() {
		isDownloading = false;
		client.cancel();
	}
	
	@Override
	protected void onPostExecute(ByteBuffer result) {
		
		try { // swallow *all* exceptions (safety)
		super.onPostExecute(result);
		isDownloading = false;
		
		if(signal != null) {
			signal.countDown();
		}
		
		long elapsedTimeMillis = System.currentTimeMillis() - requestStart;
		String outTime = "PHAsyncRequest elapsed time (ms) = " + elapsedTimeMillis;
		PHStringUtil.log(outTime);
		
		if(lastError != null)
			delegate.requestFailed(lastError);
		else
			delegate.requestFinished(result);
		
		} catch (Exception e) {
			PHCrashReport.reportCrash(e, "PHAsyncRequest - onPostExecute", PHCrashReport.Urgency.critical);
		}
		
	}

	/** public, static utility method for converting input stream to ByteBuffer */
	public static ByteBuffer readStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			
			output.write(buffer, 0, len);
		}

		output.flush();
		return ByteBuffer.wrap(output.toByteArray());
	}
	public static String streamToString(InputStream inputStream) throws IOException, UnsupportedEncodingException {
		return new String(readStream(inputStream).array(), "UTF8");
	}
}
