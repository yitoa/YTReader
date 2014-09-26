package com.yt.reader.format.txt;

import java.io.File;
import java.io.IOException;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yt.reader.BaseBookActivity;
import com.yt.reader.R;
import com.yt.reader.base.BookOnTouchListener;
import com.yt.reader.base.BookView;
import com.yt.reader.utils.FileUtils;

public class TXTActivity extends BaseBookActivity {

	private ProgressDialog progressDialog;
	private static final int PROGRESS_DIALOG = 0;
	private ProgressThread progressThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pagefactory = new TXTPageFactory(screenWidth, screenHeight, this);
		createUI();
		bookView.setBitmaps(application.getCurPageBitmap(),
				application.getCurPageBitmap());
		// 画板灰色
		Paint myGraphPaint = new Paint();
		myGraphPaint.setColor(Color.GRAY);
		application.getCurPageCanvas().drawRect(0, 0, screenWidth,
				screenHeight, myGraphPaint);
		String charset = FileUtils.getTXTCharset(new File(book.getPath() + "/"
				+ book.getName()));
		if (!charset.equals("UTF-8")) {// 非UTF-8格式，需要花一定的时间转换成UTF-8
			showDialog(PROGRESS_DIALOG);
		} else {
			try {
				pagefactory.openbook(book);
				pagefactory.onDraw(application.getCurPageCanvas(), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void createUI() {
		bookView = new BookView(this, pagefactory,
				application.getCurPageCanvas(), application.getNextPageCanvas());
		bookView.setOnTouchListener(new BookOnTouchListener(TXTActivity.this,bookView,pagefactory) );			
		contentLayout.addView(bookView);
		contentLayout.setBackgroundColor(Color.WHITE);
		bookNameView.setText(book.getRealName());
		contentLayout.addView(bookinfoView);
		setContentView(contentLayout);
	}

	@Override
	protected void onDestroy() {// TODO
		super.onDestroy();
		if (null != progressDialog) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		Log.v("onDestroy", book.getPath() + "/" + book.getName());
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(TXTActivity.this);
			progressDialog.setMessage(getString(R.string.open_progress));
			return progressDialog;
		default:
			return null;
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case PROGRESS_DIALOG:
			progressDialog.setProgress(0);
			progressThread = new ProgressThread(handler);
			progressThread.start();
		}
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			int total = msg.arg1;
			progressDialog.setProgress(total);
			if (total >= 100) {
				dismissDialog(PROGRESS_DIALOG);
				bookView.drawPage(null, book.getCurrentLocation(), false);
			}
		}
	};

	private class ProgressThread extends Thread {
		Handler mHandler;

		ProgressThread(Handler h) {
			mHandler = h;
		}

		public void run() {
			try {
				pagefactory.openbook(book);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message msg = mHandler.obtainMessage();
			msg.arg1 = 100;
			mHandler.sendMessage(msg);
		}

	}
}
