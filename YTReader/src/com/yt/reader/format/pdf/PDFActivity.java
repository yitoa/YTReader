package com.yt.reader.format.pdf;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.yt.reader.BaseBookActivity;
import com.yt.reader.R;
import com.yt.reader.base.BookOnTouchListener;
import com.yt.reader.base.BookView;

public class PDFActivity extends BaseBookActivity {

	private EditText mPasswordView;
	private AlertDialog.Builder mAlertBuilder;
	private ProgressDialog progressDialog;
	private static final int PROGRESS_DIALOG = 0;
	private ProgressThread progressThread;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAlertBuilder = new AlertDialog.Builder(this);
		pagefactory = new MuPDFCore(screenWidth, screenHeight, this);
		if (((MuPDFCore) pagefactory).needsPassword()) {
			requestPassword(savedInstanceState);
			return;
		}
		createUI(savedInstanceState);
		bookView.setBitmaps(application.getCurPageBitmap(),
				application.getCurPageBitmap());
		showDialog(PROGRESS_DIALOG);
	}

	public void createUI(Bundle savedInstanceState) {
		bookView = new BookView(this, pagefactory,
				application.getCurPageCanvas(), application.getNextPageCanvas());
		bookView.setOnTouchListener(new BookOnTouchListener(PDFActivity.this,bookView,pagefactory) );		
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
			progressDialog = new ProgressDialog(PDFActivity.this);
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
				bookView.drawPage(null, book.getCurrentLocation(), false);
				dismissDialog(PROGRESS_DIALOG);
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

	private void requestPassword(final Bundle savedInstanceState) {
		mPasswordView = new EditText(this);
		mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordView
				.setTransformationMethod(new PasswordTransformationMethod());

		AlertDialog alert = mAlertBuilder.create();
		alert.setTitle(R.string.enter_password);
		alert.setView(mPasswordView);
		alert.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (((MuPDFCore) pagefactory)
								.authenticatePassword(mPasswordView.getText()
										.toString())) {
							createUI(savedInstanceState);
						} else {
							requestPassword(savedInstanceState);
						}
					}
				});
		alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		alert.show();
	}
}
