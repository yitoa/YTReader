package com.yt.reader.optionmenu;

import android.content.res.Configuration;
import android.util.Log;

import com.yt.reader.base.YTReaderActivity;

public class BaseOptionActivity  extends YTReaderActivity  {

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config); 
		Log.v("config","oriention changed");
		//BaseOptionActivity.this.finish();
	}
}
