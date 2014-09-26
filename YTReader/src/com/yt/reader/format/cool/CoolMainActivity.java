package com.yt.reader.format.cool;

import java.io.IOException;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import com.yt.reader.BaseBookActivity;
import com.yt.reader.R;
import com.yt.reader.base.BookOnTouchListener;
import com.yt.reader.base.BookView;

public class CoolMainActivity extends BaseBookActivity {
	public BookView widget; // 自定义view
	public ProgressDialog pDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pagefactory = new CoolPageFactory(screenWidth, screenHeight, this);
		createUI();
		widget.setBitmaps(application.getCurPageBitmap(),
				application.getCurPageBitmap());
		// 画板灰色
		Paint myGraphPaint = new Paint();
		myGraphPaint.setColor(Color.GRAY);
		application.getCurPageCanvas().drawRect(0, 0, screenWidth,
				screenHeight, myGraphPaint);
		// 显示Dialog
		pDialog = new ProgressDialog(this);
		pDialog.setMessage(getString(R.string.open_progress));
		pDialog.show();

		try {
			pagefactory.openbook(book);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createUI() {
		bookView = new BookView(this, pagefactory,
				application.getCurPageCanvas(), application.getNextPageCanvas());
		bookView.setOnTouchListener(new BookOnTouchListener(CoolMainActivity.this,bookView,pagefactory) );		
		widget = bookView;
		contentLayout.addView(bookView);
		contentLayout.setBackgroundColor(Color.WHITE);
		contentLayout.addView(bookinfoView);
		setContentView(contentLayout);
	}

	public void cancleDialog() {
		if (null != pDialog) {
			pDialog.dismiss();
			pDialog = null;
		}
	}

	@Override
	protected void onDestroy() {
		cancleDialog();
		try {
			widget = null;
			CoolPageFactory page = (CoolPageFactory) pagefactory;
			if (null != page) {
				page.unInitCool();
			}
		} catch (Exception e) {
			System.out.println("cool清理内存失败！");
		}
		super.onDestroy();
	}
}
