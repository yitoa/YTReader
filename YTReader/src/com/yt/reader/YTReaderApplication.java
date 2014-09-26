package com.yt.reader;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.yt.reader.service.ZyScreenService;

public class YTReaderApplication extends Application {

	private Bitmap curPageBitmap1, curPageBitmap2;
	private Bitmap nextPageBitmap1, nextPageBitmap2;

	private Canvas curPageCanvas1, curPageCanvas2;
	private Canvas nextPageCanvas1, nextPageCanvas2;

	private boolean isLandscape;

	private int pageNo;// 页码

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// 注册屏保信息
		Intent mService = new Intent(this, ZyScreenService.class);
		mService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(mService);

	}

	public void setBitmap(Bitmap curPageBitmap, Bitmap nextPageBitmap,
			boolean isLandscape) {
		if (isLandscape) {// 横屏
			curPageBitmap1 = curPageBitmap;
			curPageCanvas1 = new Canvas(curPageBitmap1);
			nextPageBitmap1 = nextPageBitmap;
			nextPageCanvas1 = new Canvas(nextPageBitmap1);
		} else {// 竖屏
			curPageBitmap2 = curPageBitmap;
			curPageCanvas2 = new Canvas(curPageBitmap2);
			nextPageBitmap2 = nextPageBitmap;
			nextPageCanvas2 = new Canvas(nextPageBitmap2);
		}
		this.isLandscape = isLandscape;
	}

	public Bitmap getCurPageBitmap() {
		if (isLandscape) {
			return curPageBitmap1;
		}
		return curPageBitmap2;
	}

	public Bitmap getNextPageBitmap() {
		if (isLandscape) {
			return nextPageBitmap1;
		}
		return nextPageBitmap2;
	}

	public boolean initialized(boolean isLandscape) {
		this.isLandscape = isLandscape;
		if (isLandscape) {
			return null != curPageBitmap1;
		}
		return null != curPageBitmap2;
	}

	public Canvas getCurPageCanvas() {
		if (isLandscape) {
			return curPageCanvas1;
		}
		return curPageCanvas2;
	}

	public Canvas getNextPageCanvas() {
		if (isLandscape) {
			return nextPageCanvas1;
		}
		return nextPageCanvas2;
	}

}
