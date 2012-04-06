package com.playhaven.src.prefetch;

import java.io.*;

import com.playhaven.src.common.PHConstants;
import com.playhaven.src.common.PHConstants.Development;

import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;

public class PHPrefetchLocalContentProvider extends ContentProvider {
   private static final String URI_PREFIX = "content://com.tourizo.android.localfile";

   public static String constructUri(String url) {
       Uri uri = Uri.parse(url);
       return uri.isAbsolute() ? url : URI_PREFIX + url;
   }

   @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) {
        
 		PHConstants.phLog("PHPrefetchLocalContentProvider fetching: " + uri);

        String fileCacheName = PHUrlPrefetchOperation.cacheKeyForURL(uri.toString());
        PHConstants.phLog("prefetch cache file name: " + fileCacheName);

        String cache_path = PHUrlPrefetchOperation.getExternalCacheDir() + Development.PLAYHAVEN_PREFETCH_CACHE_PATH;
        PHConstants.phLog("prefetch cache path: " + cache_path);
        File cacheDir = new File(cache_path);
        cacheDir.mkdirs();
		File newCacheFile = new File(cacheDir, fileCacheName);

		// This shouldn't happen. There are multiple checks before it gets here.
		if (!newCacheFile.exists())
			return null;

        ParcelFileDescriptor parcel = null;
        try {
            parcel = ParcelFileDescriptor.open(newCacheFile, ParcelFileDescriptor.MODE_READ_ONLY);

        } catch (FileNotFoundException e) {
     		PHConstants.phLog("PHPrefetchLocalContentProvider openFile() Exception: " + e.getMessage());
        }
        return parcel;
    }

   @Override
   public boolean onCreate() {
       return true;
   }

   @Override
   public int delete(Uri uri, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public String getType(Uri uri) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Uri insert(Uri uri, ContentValues contentvalues) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

}
