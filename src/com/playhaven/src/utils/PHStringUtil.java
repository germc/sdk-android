package com.playhaven.src.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.playhaven.src.common.PHConfig;

public class PHStringUtil extends Object {
	public static String decodeURL(String url) {
		throw new UnsupportedOperationException("This method is not yet implemented");
	}
	
	public static String encodeURL(String url) {
		throw new UnsupportedOperationException("This method is not yet implemented");
	}
	
	/** Creates a simple HashMapmapping keys to values from the specified query.*/
	public static HashMap<String, String> createQueryDict(String query) {
		if (query == null) return null;
		HashMap<String, String> queryComps = new HashMap<String, String>();
		
		String[] parts = query.split("&|\\?");
		for(String part : parts) {
			String[] kv = part.split("=");
			
			if(kv.length < 2) continue;
			
			String key = PHStringUtil.urlDecode(kv[0]);
			String val = PHStringUtil.urlDecode(kv[1]);
			
			queryComps.put(key, val);
		}
		
		return queryComps;
	}
	
	/** Simple logger */
	public static void log(String message) {
		Log.i(String.format("|Playhaven-%s|", PHConfig.sdk_version), message);
	}
	
	/** 
	 * Opposite of {@link PHStringUtil#createQueryDict(String)} and creates a url
	 * from the keys and values of the specified Hashmap. We use a hashmap
	 * to gracefully manage nulls.
	 */
	public static String createQuery(HashMap<String, String> dict) {
		if (dict == null) return null;
		
		StringBuilder query = new StringBuilder();
		
		for (Map.Entry<String, String>  entry : dict.entrySet()) {
			
			String key   = entry.getKey();
			String value = entry.getValue();
			
			if (key   == null ||
				value == null) continue; 
				
			key   = PHStringUtil.urlEncode(key);
			value = PHStringUtil.weakUrlEncode(value); // weak encode url for server side compatibility
			
			// only append '&' if not first key/value pair
			query.append(String.format(
						query.length() == 0 ? "%s=%s" 
											: "&%s=%s", 
						key, 
						value)
						);
			
		}
		
		return query.toString();
	}
	
	/** Grabs query component only from url. If no query component, returns null.*/
	public static String queryComponent(String url) {
		int queryStart = url.indexOf("?");
		
		if(queryStart == -1) return null;
		
		return url.substring(queryStart+1);
	}

	public static String urlEncode(String in) {
		return URLEncoder.encode(in);
	}
	
	/** 
	 * Doesn't encode characters such as: ":", ";", etc.
	 * Behaves like Cocoa's stringByEscaping function. We mostly use this to ensure
	 * server compatibility but ideally the server would grow up. :)
	 */
	public static String weakUrlEncode(String url) {
		if (url == null) return null;
		
		// make sure you don't have a duplicate character in both lists or it will break.
		String[] reserved    = {
							 ";",  "?",  " ",
                             "&",  "=",  "$", 
                             ",",  "[",  "]",
                             "#",  "!",  "'", 
                             "(",  ")",  "*"
                             };

		String[] escaped     = {
							 "%3B", "%3F", "+" ,  
							 "%26", "%3D", "%24" ,
                             "%2C", "%5B", "%5D", 
                             "%21", "%27", "%28", 
                             "%28", "%29", "%2A"
                             };


		StringBuilder encUrl = new StringBuilder(url);
		
		//cycle through and keep replacing (bit inefficient, but sue me!)
		for(int i = 0; i < escaped.length; i++) {
			String res = reserved[i];
			String esc = escaped [i];
			
			// replace all occurrences
			int index = encUrl.indexOf(res);
			while(index != -1) {
				encUrl.replace(index, 
							   index + res.length(), 
							   esc);
				
				index = encUrl.indexOf(res);
			}
		}
		return encUrl.toString();
	}
	
	public static String urlDecode(String in) {
		return URLDecoder.decode(in);
	}
	
	private static String hexEncode(byte[] in) {
		StringBuilder builder = new StringBuilder(in.length*2);
		
		Formatter formatter = new Formatter(builder);
		for (byte inByte : in)
			formatter.format("%02x", inByte);
		
		
		return builder.toString();
	}
	
	/** First encrypts with SHA1 and then spits the result out as a b64 string*/
	public static String hexDigest(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return hexEncode(dataDigest(input));
	}
	
	public static String base64Digest(String input) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		String b64digest = base64Encode(dataDigest(input));

		// we trim off last character due to a weird encoding error
		// Note: check for a solution in the future.
		return b64digest.substring(0, b64digest.length() - 1);
	}
	
	public static String base64Encode(byte[] in) throws UnsupportedEncodingException {
		if (in == null) return null;
		
		return new String(Base64.encode(in, Base64.URL_SAFE | Base64.NO_PADDING), "UTF8");
	}
	
	private static byte[] dataDigest(String in) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		if (in == null) return null;
	
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		return 		  md.digest(in.getBytes("UTF8"));
	}
	
	/** Generates unique but random UUID*/
	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
	
	/** Encodes unicode characters as HTML entities */
	public static String encodeHtml(String to_encode) {
		return TextUtils.htmlEncode(to_encode);
	}
	
}
