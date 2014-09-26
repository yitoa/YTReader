package com.yt.reader.base;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.yt.reader.BaseBookActivity;
import com.yt.reader.R;

public class BookOnTouchListener implements OnTouchListener {
	private BaseBookActivity activity;
	private BookView bookView;
	private int screenWidth;
	private BookPageFactory pagefactory;
	private Bitmap curPageBitmap, nextPageBitmap;
	private Canvas curPageCanvas, nextPageCanvas;
	private String searchText;

	public BookOnTouchListener(BaseBookActivity activity, BookView bookView,
			BookPageFactory pagefactory) {
		this.activity = activity;
		this.bookView = bookView;
		this.pagefactory = pagefactory;
		this.searchText = bookView.searchText;
		this.screenWidth = activity.screenWidth;
		this.curPageBitmap = activity.application.getCurPageBitmap();
		this.nextPageBitmap = activity.application.getNextPageBitmap();
		this.curPageCanvas = activity.application.getCurPageCanvas();
		this.nextPageCanvas = activity.application.getNextPageCanvas();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean ret = false;
		if (v == bookView) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.v("action", "down " + event.getX());
				if (event.getX() > screenWidth / 3
						&& event.getX() < screenWidth * 2 / 3) {
					if (null == activity.menuLayout) {
						activity.makeButtonsView();
						activity.contentLayout.addView(activity.menuView);
					}
					boolean shown = activity.menuLayout.isShown();
					activity.setOptionMenuInvisible();
					if (shown) {
						activity.menuLayout.setVisibility(View.INVISIBLE);
					} else {
						activity.menuLayout.setVisibility(View.VISIBLE);
					}
					// Toast.makeText(activity, "middle", Toast.LENGTH_SHORT)
					// .show();
					return false;
				} else {
					activity.setOptionMenuInvisible();
					bookView.abortAnimation();
					bookView.oldY = event.getY();
					bookView.calcCornerXY(event.getX(), event.getY());
					// bookView.calcCornerXY(
					// event.getX(),
					// Math.min(event.getY(), activity.screenHeight
					// - event.getY()));//TODO 临时解决真机中出现的bug
					//pagefactory.onDraw(curPageCanvas, searchText);
					if (bookView.DragToRight()) {
						if (pagefactory.isfirstPage()) {
							Toast.makeText(activity, R.string.first_page, Toast.LENGTH_SHORT).show();
							return false;
						}
						try {
							pagefactory.prePage();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						if (pagefactory.islastPage()) {
							Toast.makeText(activity, R.string.last_page, Toast.LENGTH_SHORT).show();
							return false;
						}
						try {
							pagefactory.nextPage();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					bookView.isTurned = true;
					bookView.setBitmaps(curPageBitmap, nextPageBitmap);
					pagefactory.onDraw(nextPageCanvas, searchText);
				}
			}

			ret = bookView.doTouchEvent(event);
			return ret;
		}
		return false;
	}

}
