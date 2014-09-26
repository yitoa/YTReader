package com.yt.reader.base;

import com.yt.reader.utils.HerVer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * 除MainActivity外所有Activity都要直接或间接继承该类，以处理未捕获的异常。
 * 
 * @author lsj
 * 
 */
public class YTReaderActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		HerVer hv = new HerVer(this);// 横竖屏
		hv.setHerOrVer();
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
	}
}
