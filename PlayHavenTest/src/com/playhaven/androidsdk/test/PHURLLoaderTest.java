package com.playhaven.androidsdk.test;

import com.playhaven.src.common.PHURLLoader;

import android.test.AndroidTestCase;

public class PHURLLoaderTest extends AndroidTestCase implements PHURLLoader.PHURLLoaderDelegate {
	@Override
	protected void setUp() throws Exception {
		// very simple test (only duplicating iOS test)
		PHURLLoader loader = new PHURLLoader(getContext(), this);
		// will throw exception if there is a problem
		loader.opensFinalURLOnDevice = false;
	}
	
	@Override
	protected void tearDown() throws Exception{
		//TODO: clear out.
	}

	//-------------------
	//PHURLLoader delegate
	@Override
	public void loaderFinished(PHURLLoader loader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loaderFailed(PHURLLoader loader) {
		// TODO Auto-generated method stub
		
	}
}
