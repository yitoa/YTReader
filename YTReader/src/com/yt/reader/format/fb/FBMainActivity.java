package com.yt.reader.format.fb;

import java.io.IOException;

import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.image.ZLImageManager;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;

import com.yt.reader.BaseBookActivity;
import com.yt.reader.R;
import com.yt.reader.base.BookOnTouchListener;
import com.yt.reader.base.BookView;

public class FBMainActivity extends BaseBookActivity {
	public BookView widget; // 自定义view
	private ProgressDialog pDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("ytreader_fb", "fb_onCreate");
		super.onCreate(savedInstanceState);
		pagefactory = new FBPageFactory(screenWidth, screenHeight, this);

		// 创建窗口
		createUI();
		// FBPageFactory fbPage = (FBPageFactory)pagefactory;
		widget.setBitmaps(application.getCurPageBitmap(),
				application.getCurPageBitmap());
		// 画板灰色
		Paint myGraphPaint = new Paint();
		myGraphPaint.setColor(Color.GRAY);
		application.getCurPageCanvas().drawRect(0, 0, screenWidth,
				screenHeight, myGraphPaint);
		// 图片管理器
		if (null == ZLImageManager.Instance()) {
			new ZLAndroidImageManager();
		}
		// 关键库
		if (null == ZLibrary.Instance()) {
			new ZLAndroidLibrary(this.getApplication());
		}
		// 传递activity,目的是获取main中的Widget
		getLibrary().setActivity(this.getApplicationContext());
		getLibrary().setWidget(widget);

		// 显示Dialog
		pDialog = new ProgressDialog(this);
		pDialog.setMessage(getString(R.string.open_progress));
		pDialog.show();
		try {
			pagefactory.openbook(book);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void cancleDialog() {
		if (null != pDialog) {
			pDialog.dismiss();
			pDialog = null;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i("ytreader_fb", "fb_onNewIntent");
		super.onNewIntent(intent);
	}

	@Override
	protected void onPause() {
		Log.i("ytreader_fb", "fb_onPause");
		super.onPause();
	}

	@Override
	protected void onPostResume() {
		Log.i("ytreader_fb", "fb_onPostResume");
		super.onPostResume();
	}

	@Override
	protected void onRestart() {
		Log.i("ytreader_fb", "fb_onRestart");
		super.onRestart();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i("ytreader_fb", "fb_onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		Log.i("ytreader_fb", "fb_onResume");
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i("ytreader_fb", "fb_onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		Log.i("ytreader_fb", "fb_onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.i("ytreader_fb", "fb_onStop");
		super.onStop();
	}

	private void createUI() {
		bookView = new BookView(this, pagefactory,
				application.getCurPageCanvas(), application.getNextPageCanvas());
		bookView.setOnTouchListener(new BookOnTouchListener(FBMainActivity.this,bookView,pagefactory) );		
		widget = bookView;
		contentLayout.addView(bookView);
		contentLayout.setBackgroundColor(Color.WHITE);
		contentLayout.addView(bookinfoView);
		setContentView(contentLayout);
	}

	protected void onDestroy() {
		Log.i("ytreader_fb", "fb_onDestroy");
		FBView fbTextView = (FBView) getLibrary().getCurrentView();
		if (null != fbTextView) {
			fbTextView.clearFindResults(); // 释放内存
		}
		cancleDialog();
		FBPageFactory page = (FBPageFactory) pagefactory;
		page.recycle();
		pagefactory = null;
		widget = null;
		bookView = null;
		super.onDestroy();
	}

	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
	}

}
