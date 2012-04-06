package com.playhaven.src.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Dictionary;
import java.util.UUID;
import java.util.Formatter;

import android.text.TextUtils;
import android.util.Base64;

/** String utility class for various tasks. Used mostly for server requests so most methods are actually just static.
 * 
 * Unlike the iOS we get a lot of the functionality for free which is pretty cool.
 * 
 */
public class PHStringUtil {
	public static String urlEncode(String in) {
		return URLEncoder.encode(in);
	}
	/** Does weak url encoding. Doesn't encode characters such as: ":", ";", etc.
	 * Behaves like Cocoa's stringByEscaping function. We mostly use this to ensure
	 * server compatibility but ideally the server would grow up. :)*/
	public static String weakUrlEncode(String in) {
		// make sure you don't have a duplicate character in both lists or it will break.
		String[] reserved = {";" ,"?" , " ",
                                 "&" , "=" ,
                                "$" , "," , "[" , "]",
                                "#", "!", "'", "(", 
                                ")", "*"};

		String[] replacements= {"%3B" ,"%3F" ,
                                "+" ,  "%26",
                                "%3D" , "%24" ,
                                "%2C" , "%5B" , "%5D", 
                                 "%21", "%27",
                                "%28", "%29", "%2A"};


		StringBuilder inBuff = new StringBuilder(in);
		//cycle through and keep replacing (bit inefficient, but sue me!)
		for(int i=0; i < Math.min(reserved.length, replacements.length);i++) {
			String res = reserved[i];
			String rep = replacements[i];
			
			// find all occurrences...
			int index = inBuff.indexOf(res);
			while(index != -1) {
				inBuff.replace(index, index+res.length(), rep);
				index = inBuff.indexOf(res);
			}
		}
		return inBuff.toString();
	}
	
	public static String urlDecode(String in) {
		return URLDecoder.decode(in);
	}
	
	public static String hexEncode(byte[] in) {
		StringBuilder builder = new StringBuilder(in.length*2);
		
		Formatter formatter = new Formatter(builder);
		for (byte inByte : in) {
			formatter.format("%02x", inByte);
		}
		
		return builder.toString();
	}
	public static String hexDigest(String input) {
		String hexDigest = hexEncode(dataDigest(input));
		return hexDigest;
	}
	public static String base64Digest(String input) {
		String b64digest = base64Encode(dataDigest(input));
		// we trim off last character due to a weird encoding error
		// Note: check for a solution in the future.
		return b64digest.substring(0, b64digest.length()-1);
	}
	
	public static String base64Encode(byte[] in) {
		String encoded = null;
		try {
			encoded = new String(Base64.encode(in, Base64.URL_SAFE|Base64.NO_PADDING), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encoded;
	}
	
	public static byte[] dataDigest(String in) {
		byte[] sum = null;
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] bytes = in.getBytes("UTF8");
			sum = md.digest(bytes);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sum;
	}
	
	public static Dictionary<String, String> dictFromQuery(String query) {
		// TODO: implement this method (doesn't seem to be currently used)
		return null;
	}
	
	public static String queryFromDic(Dictionary<String, String> query) {
		//TODO: implement this method (doesn't seem to be currently used)
		return null;
	}
	
	/** Generates unique but random UUID*/
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}
	
	public static String encodeHtml(String to_encode) {
		return TextUtils.htmlEncode(to_encode);
	}
	
}
