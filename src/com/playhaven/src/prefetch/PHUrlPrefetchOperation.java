package com.playhaven.src.prefetch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.playhaven.src.common.PHConstants;
import com.playhaven.src.common.PHConstants.Development;

import android.os.AsyncTask;
import android.os.Environment;

import java.security.*;

public class PHUrlPrefetchOperation extends AsyncTask<String, Integer, String> {

	private static final String AUTHORITY_PATH = "/com.playhaven"; 
	private static final String PREFIX = "/Android/data";
	private static final String CACHE_PATH = "/cache";

	/**
	 * The external application cache directory for temporary files
	 * 
	 * @return the application external cache directory
	 */
	public static File getExternalCacheDir()
	{
		File f = new File(
				Environment.getExternalStorageDirectory().getAbsolutePath() +
				PREFIX + AUTHORITY_PATH + CACHE_PATH);
		f.mkdirs();
		return f;
	}

	public static String cacheKeyForURL(String url) {
		byte[] bytesOfMessage = null;
		byte[] thedigest = null;
		try {
			bytesOfMessage = url.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			thedigest = md.digest(bytesOfMessage);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		StringBuffer sb = new StringBuffer();
        for (int i = 0; i < thedigest.length; i++) {
        	sb.append(Integer.toString((thedigest[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
	}
	
    @Override
    protected String doInBackground(String... url) {
        int count;
        try {
            URL lurl = new URL(url[0]);
	        PHConstants.phLog("prefetching url: " + url[0]);
            URLConnection conexion = lurl.openConnection();
            conexion.connect();
	        
	        InputStream input = new BufferedInputStream(lurl.openStream());

            // TODO: Check internal storage if no external storage. Saw article saying
            // shouldn't use internal storage unless u have too.

	        String cache_path = getExternalCacheDir() + Development.PLAYHAVEN_PREFETCH_CACHE_PATH;
	        PHConstants.phLog("prefetch cache path: " + cache_path);
	        File cacheDir = new File(cache_path);
	        cacheDir.mkdirs();
	        String newLocalUrl = new String("content://com.playhaven.src.prefetch.PHPrefetchLocalContentProvider"+lurl.getPath());
	        String fileCacheName = cacheKeyForURL(newLocalUrl);
	        PHConstants.phLog("prefetch cache file name: " + fileCacheName);
			File newCacheFile = new File(cacheDir, fileCacheName);

            OutputStream output = new FileOutputStream(newCacheFile);

            byte data[] = new byte[1024];

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
	        PHConstants.phLog("PHUrlPrefetchOperation exception: " + e.getMessage());

        }
        return null;
    }
}
