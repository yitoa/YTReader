package com.yt.reader.format.pdf;

import java.io.IOException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Path.Direction;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.yt.reader.R;
import com.yt.reader.base.BookPageFactory;
import com.yt.reader.database.DBSchema;
import com.yt.reader.model.Book;
import com.yt.reader.optionmenu.TextSearch;
import com.yt.reader.utils.Constant;

public class MuPDFCore extends BookPageFactory {
	/* load our native library */
	static {
		System.loadLibrary("mupdf");
	}

	/* Readable members */
	private int pageNum = -1;;

	public float pageWidth;

	public float pageHeight;

	/* The native functions */
	public static native int openFile(String filename);

	private static native int countPagesInternal();

	private static native void gotoPageInternal(int localActionPageNum);

	private static native float getPageWidth();

	private static native float getPageHeight();

	public static native void drawPage(Bitmap bitmap, int pageW, int pageH,
			int patchX, int patchY, int patchW, int patchH);

	public static native RectF[] searchPage(String text);

	public static native SearchItem[] search(String text);

	public static native int getPageLink(int page, float x, float y);

	public static native LinkInfo[] getPageLinksInternal(int page);

	public static native OutlineItem[] getOutlineInternal();

	public static native boolean hasOutlineInternal();

	public static native boolean needsPasswordInternal();

	public static native boolean authenticatePasswordInternal(String password);

	public static native void destroying();

	public static native String getBookMarkStr(int page);

	private int width, height;// 屏幕尺寸
	private int vWidth, vHeight;// 绘制区尺寸
	private SharedPreferences style;// 字体样式的配置文件
	private PDFActivity context;
	private int readingPage;
	private int totalPage;

	private AsyncTask<Void, Void, Void> drawTask;
	private LinearLayout progressLayout;
	private ProgressBar progressBar;
	private final Handler mHandler = new Handler();
	private static final int PROGRESS_DIALOG_DELAY = 200;

	public MuPDFCore(int width, int height, PDFActivity context) {
		super(width, height);
		this.width = width;
		this.height = height;
		this.context = context;
		setTextStyle();
	}

	public int countPages() {
		totalPage = (int) context.book.getTotalPage();
		if (totalPage <= 0) {
			totalPage = countPagesSynchronized();
			ContentResolver resolver = context.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(DBSchema.COLUMN_BOOK_TOTAL_PAGE, totalPage);
			context.book.setTotalPage(totalPage);
			resolver.update(DBSchema.CONTENT_URI_BOOK, values, BaseColumns._ID
					+ " = " + context.book.getId(), null);
		}
		return totalPage;
	}

	private synchronized int countPagesSynchronized() {
		return countPagesInternal();
	}

	public synchronized PointF getPageSize(int page) {
		try {
			gotoPage(page, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new PointF(pageWidth, pageHeight);
	}

	public synchronized void onDestroy() {
		destroying();
	}

	public synchronized void drawPage(int page, Bitmap bitmap, int pageW,
			int pageH, int patchX, int patchY, int patchW, int patchH) {
		try {
			gotoPage(page, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		drawPage(bitmap, pageW, pageH, patchX, patchY, patchW, patchH);
	}

	public synchronized int hitLinkPage(int page, float x, float y) {
		return getPageLink(page, x, y);
	}

	public synchronized LinkInfo[] getPageLinks(int page) {
		return getPageLinksInternal(page);
	}

	public synchronized RectF[] searchPage(int page, String text) {
		try {
			gotoPage(page, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return searchPage(text);
	}

	public synchronized boolean hasOutline() {
		return hasOutlineInternal();
	}

	public synchronized OutlineItem[] getOutline() {
		return getOutlineInternal();
	}

	public synchronized boolean needsPassword() {
		return needsPasswordInternal();
	}

	public synchronized boolean authenticatePassword(String password) {
		return authenticatePasswordInternal(password);
	}

	@Override
	public void openbook(Book book) throws IOException {
		openFile(book.getPath() + "/" + book.getName());
		countPages();
		readingPage = (int) book.getCurrentLocation();
		if (readingPage < 0) {
			readingPage = 0;
		}
		setChapters();
	}

	/**
	 * 第一次打开时将章节信息写入数据库。
	 */
	private void setChapters() {
		if (hasOutline()) {
			Cursor cursor = context.getContentResolver().query(
					DBSchema.CONTENT_URI_CHAPTER,
					new String[] { DBSchema.COLUMN_CHAPTER_LOCATION,
							DBSchema.COLUMN_CHAPTER_TITLE },
					DBSchema.COLUMN_CHAPTER_BOOKID + "=? ",
					new String[] { context.book.getId() + "" },
					DBSchema.COLUMN_CHAPTER_LOCATION);
			if (0 < cursor.getCount()) {// 书签已经存入数据库
				cursor.close();
				return;
			}
			ContentResolver resolver = context.getContentResolver();
			OutlineItem items[] = this.getOutline();
			ContentValues values;
			for (OutlineItem item : items) {
				values = new ContentValues();
				values.put(DBSchema.COLUMN_CHAPTER_LOCATION, item.page);
				values.put(DBSchema.COLUMN_CHAPTER_TITLE, item.title);
				values.put(DBSchema.COLUMN_CHAPTER_BOOKID, context.book.getId());
				resolver.insert(DBSchema.CONTENT_URI_CHAPTER, values);
			}
		}
	}

	@Override
	public synchronized void onDraw(final Canvas canvas, String searchText) {
		context.pageNumView.setText((readingPage + 1) + "/" + totalPage);// readingPage从0开始计数
		Bitmap bitmap;
		if(canvas.equals(context.application.getCurPageCanvas())){
			Log.v("canvas","cur");
			bitmap=context.application.getCurPageBitmap();
		}else{
			bitmap=context.application.getNextPageBitmap();
			Log.v("canvas","next");
		}
		drawPage(bitmap, width, vHeight, 0, -20,
				width, height);//画当前的页面
		if (null != searchText) {
			Paint paintPath = new Paint();
			paintPath.setColor(Color.DKGRAY);
			paintPath.setAlpha(120);
			for (SearchItem item : TextSearch.searchResultRect) {
				if (item.getPage() > readingPage)
					break;
				if (item.getPage() == readingPage) {
					RectF rectF = new RectF(item.getLeft1(), item.getTop1(),
							item.getRight1(), item.getBottom1());
					Path path = new Path();
					path.addRect(rectF, Direction.CW);
					canvas.drawPath(path, paintPath);
					if (-1 != item.getLeft2()) {//搜索结果占两行
						rectF = new RectF(item.getLeft2(), item.getTop2(),
								item.getRight2(), item.getBottom2());
						path = new Path();
						path.addRect(rectF, Direction.CW);
						canvas.drawPath(path, paintPath);
					}
				}
			}
		}
	}

	@Override
	public void prePage() throws IOException {
		if (this.isfirstPage()) {
			return;
		}
		gotoPage(--readingPage, false);
	}

	@Override
	public void nextPage() throws IOException {
		if (this.islastPage()) {
			return;
		}
		gotoPage(++readingPage, false);
	}

	@Override
	public void gotoPage(long location, boolean handleMessyCode)
			throws IOException {
		readingPage = (int) location;
		if (readingPage > totalPage - 1)
			readingPage = (int) totalPage - 1;
		else if (readingPage < 0)
			readingPage = 0;
		gotoPageInternal(readingPage);
	}

	@Override
	public boolean isfirstPage() {
		return readingPage == 0;
	}

	@Override
	public boolean islastPage() {
		return readingPage == totalPage - 1;
	}

	@Override
	public long getReadingLocation() {
		return readingPage;
	}

	@Override
	public void setReadingLocation(long location) {
		readingPage = (int) location;
	}

	@Override
	public void addBookmark() {
		Cursor cursor = context.getContentResolver().query(
				DBSchema.CONTENT_URI_BOOKMARK,
				new String[] { BaseColumns._ID },
				DBSchema.COLUMN_CHAPTER_BOOKID + "=? AND "
						+ DBSchema.COLUMN_BOOKMARK_LOCATION + "=?",
				new String[] { context.book.getId() + "",
						getReadingLocation() + "" }, null);

		// 查询是否已经存在
		if (cursor.moveToFirst()) {
			Toast.makeText(context, R.string.bookmark_exist, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		ContentValues values = new ContentValues();
		values.put(DBSchema.COLUMN_BOOKMARK_LOCATION, getReadingLocation());
		String title = getBookMarkStr((int) getReadingLocation());
		Log.v("PDFBookmark", title);
		values.put(DBSchema.COLUMN_BOOKMARK_DESCRIPTION, title);// TODO
		values.put(DBSchema.COLUMN_BOOKMARK_BOOKID, context.book.getId());
		context.getContentResolver().insert(DBSchema.CONTENT_URI_BOOKMARK,
				values);
		Toast.makeText(context, R.string.bookmark_added, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void setTextStyle() {
		style = context.getSharedPreferences(Constant.STYLE_REFERENCE, 0);
		int marginWidth = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_WIDTH,
				Constant.STYLE_DEFAULT_MARGIN_WIDTH);
		int marginHeight = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_HEIGHT,
				Constant.STYLE_DEFAULT_MARGIN_HEIGHT);
		vWidth = width - marginWidth * 2;
		vHeight = height - marginHeight * 2;
	}

}
