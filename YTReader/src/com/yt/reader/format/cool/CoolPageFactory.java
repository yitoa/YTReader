package com.yt.reader.format.cool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.coolreader.crengine.BackgroundThread;
import org.coolreader.crengine.BookInfo;
import org.coolreader.crengine.Bookmark;
import org.coolreader.crengine.DocView;
import org.coolreader.crengine.DocumentFormat;
import org.coolreader.crengine.Engine;
import org.coolreader.crengine.Engine.HyphDict;
import org.coolreader.crengine.FileInfo;
import org.coolreader.crengine.ImageInfo;
import org.coolreader.crengine.PositionProperties;
import org.coolreader.crengine.Properties;
import org.coolreader.crengine.ReaderCallback;
import org.coolreader.crengine.Settings;
import org.coolreader.crengine.TOCItem;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.yt.reader.R;
import com.yt.reader.base.BookPageFactory;
import com.yt.reader.database.DBSchema;
import com.yt.reader.model.Book;
import com.yt.reader.model.Chapter;
import com.yt.reader.optionmenu.TextSearch;
import com.yt.reader.utils.Constant;

public class CoolPageFactory extends BookPageFactory {

	int width;

	int height;

	CoolMainActivity coolActivity;

	Context appContext;

	long readingLocation = 1;

	Engine mEngine; // 引擎

	static DocView doc; // 解码器接口

	BookInfo mBookInfo;

	Properties props = new Properties();

	boolean initOrNot;// 是否已解码

	Book book;

	private Bitmap bitmap;//判断使用curPageBitmap还是nextPageBitmap
	private int action;// 记录是gotoPage还是翻页

	public CoolPageFactory(int w, int h, CoolMainActivity activity) {
		super(w, h);
		width = w;
		height = h - 31;
		coolActivity = activity;
		appContext = activity.getApplicationContext();
	}

	@Override
	public void openbook(Book book) throws IOException {
		search_text = null;
		initOrNot = false;
		setTextStyle();
		this.book = book;
		initCool();
		loadDocument(book.getPath() + "/" + book.getName(), null);
	}

	public void initCool() {
		System.gc();
		System.gc();
		if (null == mEngine) {
			mEngine = new Engine(appContext);
			// load settings
			props = loadSettings();
			String code = props.getProperty(Settings.PROP_HYPHENATION_DICT,
					Engine.HyphDict.RUSSIAN.toString());
			Engine.HyphDict dict = HyphDict.byCode(code);
			mEngine.setHyphenationDictionary(dict);
		}

		if (null == doc) {
			doc = new DocView(mEngine);
			doc.setReaderCallback(readerCallback);
			doc.create();
			String css = mEngine.loadResourceUtf8(R.raw.fb2);
			if (css != null && css.length() > 0)
				doc.setStylesheet(css);
			applySettings(props, false, false);
			doc.resize(width, height);
		}
	}

	public void unInitCool() {
		coolActivity = null;
		if (null != mEngine) {
			mEngine.uninit();
			mEngine = null;
		}
		if (null != doc) {
			doc.closeImage();
			doc.destroy();
			doc = null;
		}
		BackgroundThread.instance().quit();
		System.gc();
	}

	Handler handler = new Handler();

	class TimeUpdateTitle implements Runnable {
		@Override
		public void run() {
			// 设置标题、进度
			setReadingRate();
			setReadingTitle();
			onDraw(coolActivity.application.getCurPageCanvas(), null);
		}
	}

	public boolean loadDocument(String fileName, final Runnable errorHandler) {
		BackgroundThread.ensureGUI();
		if (fileName == null) {
			errorHandler.run();
			return false;
		}
		String normalized = mEngine.getPathCorrector().normalize(fileName);
		if (normalized == null) {
			mEngine.hideProgress();
			errorHandler.run();
			return false;
		} else if (!normalized.equals(fileName)) {
			fileName = normalized;
		}

		FileInfo fi = new FileInfo(fileName);
		return loadDocument(fi, errorHandler);
	}

	public boolean loadDocument(final FileInfo fileInfo,
			final Runnable errorHandler) {
		post(new LoadDocumentTask(new BookInfo(fileInfo), errorHandler));
		return true;
	}

	private class LoadDocumentTask extends Task {
		String filename;

		// String path;
		Runnable errorHandler;

		String pos;

		// int profileNumber;
		boolean disableInternalStyles;

		boolean disableTextAutoformat;

		LoadDocumentTask(BookInfo bookInfo, Runnable errorHandler) {
			BackgroundThread.ensureGUI();
			mBookInfo = bookInfo;
			FileInfo fileInfo = bookInfo.getFileInfo();
			if (fileInfo.getTitle() == null) {
				mEngine.scanBookProperties(fileInfo);
			}
			String language = fileInfo.getLanguage();
			mEngine.setHyphenationLanguage(language);
			this.filename = fileInfo.getPathName();
			this.errorHandler = errorHandler;
			disableInternalStyles = mBookInfo.getFileInfo().getFlag(
					FileInfo.DONT_USE_DOCUMENT_STYLES_FLAG);
			disableTextAutoformat = mBookInfo.getFileInfo().getFlag(
					FileInfo.DONT_REFLOW_TXT_FILES_FLAG);
			if (mBookInfo != null && mBookInfo.getLastPosition() != null)
				pos = mBookInfo.getLastPosition().getStartPos();
		}

		@Override
		public void work() throws IOException {
			BackgroundThread.ensureBackground();
			coverPageBytes = null;
			doc.doCommand(ReaderCommand.DCMD_SET_INTERNAL_STYLES.nativeId,
					disableInternalStyles ? 0 : 1);
			doc.doCommand(ReaderCommand.DCMD_SET_TEXT_FORMAT.nativeId,
					disableTextAutoformat ? 0 : 1);
			boolean success = doc.loadDocument(filename);
			if (success) {
				doc.requestRender();

				// findCoverPage();
				gotoPage(book.getCurrentLocation(), false);
				doc.updateBookInfo(mBookInfo);
				insertContent(getContents());
				book.setTotalPage(1000); // 总页码设置为1000，CurrentLocation =
											// 当前页/实际总页码 * 1000
				updateBook(book);
				setReadingLocation(book.getCurrentLocation());

				handler.post(new TimeUpdateTitle());// 更新标题信息
				coolActivity.cancleDialog();

				if (pos != null) {
					doc.goToPosition(pos, false);
					preparePageImage(0);
				}
				coolActivity.widget.postInvalidate();
				initOrNot = true;
			} else {
				// 解码失败
				coolActivity.cancleDialog();
				Toast.makeText(appContext, R.string.not_supported_file,
						Toast.LENGTH_SHORT).show();
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						coolActivity.finish();
					}
				}).start();
			}
		}

		@Override
		public void done() {
			BackgroundThread.ensureGUI();
			if (coverPageBytes != null && mBookInfo != null
					&& mBookInfo.getFileInfo() != null) {
				if (mBookInfo.getFileInfo().format.needCoverPageCaching()) {
				}
			}

		}

		public void fail(Exception e) {
			BackgroundThread.ensureGUI();
			mBookInfo = null;
			mEngine.hideProgress();
			if (errorHandler != null) {
				errorHandler.run();
			}
		}
	}

	/**
	 * 设置界面标题
	 */
	public void setReadingTitle() {
		String title = book.getRealName();
		int len = title.length();
		if (len > 50) {
			String temp = title.substring(0, 30);
			temp += "..";
			temp += title.substring(len - 4);
			title = temp;
		}
		coolActivity.bookNameView.setText(title);
	}

	/**
	 * 更新book信息
	 */
	private void updateBook(Book book) {
		ContentValues values = new ContentValues();

		values.put(DBSchema.COLUMN_BOOK_TOTAL_PAGE, book.getTotalPage());

		try {
			appContext.getContentResolver().update(DBSchema.CONTENT_URI_BOOK,
					values, BaseColumns._ID + " = " + book.getId(), null);
		} catch (Exception e) {
			System.out.println("updateBook error");
		}
	}

	boolean enable_progress_callback = true;

	ReaderCallback readerCallback = new ReaderCallback() {

		public boolean OnExportProgress(int percent) {
			return true;
		}

		public void OnExternalLink(String url, String nodeXPath) {
		}

		public void OnFormatEnd() {
			// drawPage();
			// scheduleSwapTask();
		}

		public boolean OnFormatProgress(final int percent) {
			if (enable_progress_callback) {
				mEngine.showProgress(percent * 4 / 10 + 5000, "formatting");
			}
			return true;
		}

		public void OnFormatStart() {
		}

		public void OnLoadFileEnd() {
		}

		public void OnLoadFileError(String message) {
		}

		public void OnLoadFileFirstPagesReady() {
		}

		public String OnLoadFileFormatDetected(final DocumentFormat fileFormat) {
			if (fileFormat != null) {
				String s = getCSSForFormat(fileFormat);
				return s;
			}
			return null;
		}

		public boolean OnLoadFileProgress(final int percent) {
			BackgroundThread.ensureBackground();
			if (enable_progress_callback) {
				mEngine.showProgress(percent * 4 / 10 + 1000, "loading");
			}
			return true;
		}

		public void OnLoadFileStart(String filename) {
			// cancelSwapTask();
			BackgroundThread.ensureBackground();
			// log.d("readerCallback.OnLoadFileStart " + filename);
		}

		// / Override to handle external links
		public void OnImageCacheClear() {
			// clearImageCache();
		}

		public boolean OnRequestReload() {
			// reloadDocument();
			return true;
		}

	};

	private void applySettings(Properties props, boolean save,
			boolean saveDelayed) {
		props = new Properties(props); // make a copy
		props.remove(Settings.PROP_TXT_OPTION_PREFORMATTED);
		props.remove(Settings.PROP_EMBEDDED_STYLES);
		props.remove(Settings.PROP_EMBEDDED_FONTS);
		boolean isFullScreen = props.getBool(Settings.PROP_APP_FULLSCREEN,
				false);
		props.setBool(Settings.PROP_SHOW_BATTERY, isFullScreen);
		props.setBool(Settings.PROP_SHOW_TIME, isFullScreen);
		doc.applySettings(props);
	}

	private String getCSSForFormat(DocumentFormat fileFormat) {
		if (fileFormat == null)
			fileFormat = DocumentFormat.FB2;
		File[] dataDirs = mEngine.getDataDirectories(null, false, false);
		String defaultCss = mEngine.loadResourceUtf8(fileFormat
				.getCSSResourceId());
		for (File dir : dataDirs) {
			File file = new File(dir, fileFormat.getCssName());
			if (file.exists()) {
				String css = mEngine.loadFileUtf8(file);
				if (css != null) {
					int p1 = css.indexOf("@import");
					if (p1 < 0)
						p1 = css.indexOf("@include");
					int p2 = css.indexOf("\";");
					if (p1 >= 0 && p2 >= 0 && p1 < p2) {
						css = css.substring(0, p1) + "\n" + defaultCss + "\n"
								+ css.substring(p2 + 2);
					}
					return css;
				}
			}
		}
		return defaultCss;
	}

	public enum ViewMode {
		PAGES, SCROLL
	}

	// Reader命令
	public enum ReaderCommand {
		DCMD_NONE(0), DCMD_REPEAT(1), // repeat last action

		// definitions from crengine/include/lvdocview.h
		DCMD_BEGIN(100), DCMD_LINEUP(101), DCMD_PAGEUP(102), DCMD_PAGEDOWN(103), DCMD_LINEDOWN(
				104), DCMD_LINK_FORWARD(105), DCMD_LINK_BACK(106), DCMD_LINK_NEXT(
				107), DCMD_LINK_PREV(108), DCMD_LINK_GO(109), DCMD_END(110), DCMD_GO_POS(
				111), DCMD_GO_PAGE(112), DCMD_ZOOM_IN(113), DCMD_ZOOM_OUT(114), DCMD_TOGGLE_TEXT_FORMAT(
				115), DCMD_BOOKMARK_SAVE_N(116), DCMD_BOOKMARK_GO_N(117), DCMD_MOVE_BY_CHAPTER(
				118), DCMD_GO_SCROLL_POS(119), DCMD_TOGGLE_PAGE_SCROLL_VIEW(120), DCMD_LINK_FIRST(
				121), DCMD_ROTATE_BY(122), DCMD_ROTATE_SET(123), DCMD_SAVE_HISTORY(
				124), DCMD_SAVE_TO_CACHE(125), DCMD_TOGGLE_BOLD(126), DCMD_SCROLL_BY(
				127), DCMD_REQUEST_RENDER(128), DCMD_GO_PAGE_DONT_SAVE_HISTORY(
				129), DCMD_SET_INTERNAL_STYLES(130),

		DCMD_SELECT_FIRST_SENTENCE(131), // select first sentence on page
		DCMD_SELECT_NEXT_SENTENCE(132), // move selection to next sentence
		DCMD_SELECT_PREV_SENTENCE(133), // move selection to next sentence
		DCMD_SELECT_MOVE_LEFT_BOUND_BY_WORDS(134), // move selection start by
													// words
		DCMD_SELECT_MOVE_RIGHT_BOUND_BY_WORDS(135), // move selection end by
													// words

		DCMD_SET_TEXT_FORMAT(136),

		DCMD_SET_DOC_FONTS(137),

		// definitions from android/jni/readerview.h
		DCMD_OPEN_RECENT_BOOK(2000), DCMD_CLOSE_BOOK(2001), DCMD_RESTORE_POSITION(
				2002),

		// application actions
		DCMD_RECENT_BOOKS_LIST(2003), DCMD_SEARCH(2004), DCMD_EXIT(2005), DCMD_BOOKMARKS(
				2005), DCMD_GO_PERCENT_DIALOG(2006), DCMD_GO_PAGE_DIALOG(2007), DCMD_TOC_DIALOG(
				2008), DCMD_FILE_BROWSER(2009), DCMD_OPTIONS_DIALOG(2010), DCMD_TOGGLE_DAY_NIGHT_MODE(
				2011), DCMD_READER_MENU(2012), DCMD_TOGGLE_TOUCH_SCREEN_LOCK(
				2013), DCMD_TOGGLE_SELECTION_MODE(2014), DCMD_TOGGLE_ORIENTATION(
				2015), DCMD_TOGGLE_FULLSCREEN(2016), DCMD_SHOW_HOME_SCREEN(2017), // home
																					// screen
																					// activity
		DCMD_TOGGLE_DOCUMENT_STYLES(2018), DCMD_ABOUT(2019), DCMD_BOOK_INFO(
				2020), DCMD_TTS_PLAY(2021), DCMD_TOGGLE_TITLEBAR(2022), DCMD_SHOW_POSITION_INFO_POPUP(
				2023), DCMD_SHOW_DICTIONARY(2024), DCMD_OPEN_PREVIOUS_BOOK(2025), DCMD_TOGGLE_AUTOSCROLL(
				2026), DCMD_AUTOSCROLL_SPEED_INCREASE(2027), DCMD_AUTOSCROLL_SPEED_DECREASE(
				2028), DCMD_START_SELECTION(2029), DCMD_SWITCH_PROFILE(2030), DCMD_TOGGLE_TEXT_AUTOFORMAT(
				2031),

		DCMD_FONT_NEXT(2032), DCMD_FONT_PREVIOUS(2033),

		DCMD_USER_MANUAL(2034), DCMD_CURRENT_BOOK_DIRECTORY(2035), ;

		private final int nativeId;

		private ReaderCommand(int nativeId) {
			this.nativeId = nativeId;
		}

		public int getNativeId() {
			return nativeId;
		}
	}

	private void post(Engine.EngineTask task) {
		mEngine.post(task);
	}

	private abstract class Task implements Engine.EngineTask {

		public void done() {
			// override to do something useful
		}

		public void fail(Exception e) {
			// do nothing, just log exception
			// override to do custom action
		}
	}

	public Properties loadSettings() {
		Properties props = new Properties();

		try {
			InputStream is = appContext.getResources().openRawResource(
					R.raw.cr3);
			props.load(is);
			is.close();
			is = null;
		} catch (Exception e) {
		}

		int fontSize = this.fontSize;
		// 边距允许像素：0、1、2、3、4、5、8、10、12、15、20、25、30、40、50、60、80、100、200、300（底层限制）
		String lmargin = String.valueOf(marginWidth);
		String rmargin = String.valueOf(marginWidth);
		String tmargin = String.valueOf(marginHeight + 20);
		String bmargin = String.valueOf(marginHeight);

		props.applyDefault(Settings.PROP_FONT_SIZE, String.valueOf(fontSize));
		props.applyDefault(Settings.PROP_PAGE_MARGIN_LEFT, lmargin); // 左边距
		props.applyDefault(Settings.PROP_PAGE_MARGIN_RIGHT, rmargin); // 右边距
		props.applyDefault(Settings.PROP_PAGE_MARGIN_TOP, tmargin); // 上边距
		props.applyDefault(Settings.PROP_PAGE_MARGIN_BOTTOM, bmargin);// 下边距
		props.applyDefault(Settings.PROP_INTERLINE_SPACE,
				String.valueOf(lineSpacingPercent));// 行距
		props.applyDefault(Settings.PROP_FONT_FACE, myFont);// 字体
		fixFontSettings(props);
		return props;
	}

	public boolean fixFontSettings(Properties props) {
		boolean res = false;
		res = applyDefaultFont(props, Settings.PROP_FONT_FACE, "Simhei") || res;
		res = applyDefaultFont(props, Settings.PROP_STATUS_FONT_FACE, "Simhei")
				|| res;
		res = applyDefaultFont(props, Settings.PROP_FALLBACK_FONT_FACE,
				"Simhei") || res;
		return res;
	}

	private boolean isValidFontFace(String face) {
		String[] fontFaces = mEngine.getFontFaceList();
		if (fontFaces == null)
			return true;
		for (String item : fontFaces) {
			if (item.equals(face))
				return true;
		}
		return false;
	}

	// 应用默认字体
	private boolean applyDefaultFont(Properties props, String propName,
			String defFontFace) {
		String currentValue = props.getProperty(propName);
		boolean changed = false;
		if (currentValue == null) {
			currentValue = defFontFace;
			changed = true;
		}
		if (!isValidFontFace(currentValue)) {
			if (isValidFontFace("Droid Sans"))
				currentValue = "Droid Sans";
			else if (isValidFontFace("Roboto"))
				currentValue = "Roboto";
			else if (isValidFontFace("Droid Serif"))
				currentValue = "Droid Serif";
			else if (isValidFontFace("Arial"))
				currentValue = "Arial";
			else if (isValidFontFace("Times New Roman"))
				currentValue = "Times New Roman";
			else if (isValidFontFace("Droid Sans Fallback"))
				currentValue = "Droid Sans Fallback";
			else {
				String[] fontFaces = mEngine.getFontFaceList();
				if (fontFaces != null)
					currentValue = fontFaces[0];
			}
			changed = true;
		}
		if (changed)
			props.setProperty(propName, currentValue);
		return changed;
	}

	class BitmapInfo {
		Bitmap bitmap;

		PositionProperties position;

		ImageInfo imageInfo;

		void recycle() {
			bitmap.recycle();
			bitmap = null;
			position = null;
			imageInfo = null;
		}

		boolean isReleased() {
			return bitmap == null;
		}

		@Override
		public String toString() {
			return "BitmapInfo [position=" + position + "]";
		}

	}

	private byte[] coverPageBytes = null;

	private void findCoverPage() {
		byte[] coverpageBytes = doc.getCoverPageData();
		if (coverpageBytes != null) {
			coverPageBytes = coverpageBytes;
		}
	}

	/**
	 * 页面跳转
	 * 
	 * @param offset
	 *            ：0为当前页，1为下一页，-1为上一页
	 * @return BitmapInfo：bitmap信息
	 */
	private BitmapInfo preparePageImage(int offset) {
		PositionProperties currpos = doc.getPositionProps(null);
		if (currpos.pageNumber == (currpos.pageCount - 1) && (offset != -1))
			offset = 0;
		boolean isPageView = currpos.pageMode != 0;
		if (null == bitmapInfoNext)
			bitmapInfoNext = new BitmapInfo();
		if (null != bitmapInfoNext)
			bitmapInfoNext.bitmap = bitmap;

		if (offset == 0) {// 当前页
			bitmapInfoNext.position = currpos;
			doc.getPageImage(bitmapInfoNext.bitmap);

			/*
			 * try { FileOutputStream out = new FileOutputStream(
			 * "/mnt/sdcard/Books/a0.png");
			 * bitmapInfoNext.bitmap.compress(Bitmap.CompressFormat.PNG, 90,
			 * out); } catch (Exception e) { e.printStackTrace(); }
			 */

			return bitmapInfoNext;
		}
		if (isPageView) {
			// PAGES: one of next or prev pages requested, offset is specified
			// as param
			int cmd1 = offset > 0 ? ReaderCommand.DCMD_PAGEDOWN.nativeId
					: ReaderCommand.DCMD_PAGEUP.nativeId;
			if (offset < 0)
				offset = -offset;
			if (doc.doCommand(cmd1, offset)) {
				// can move to next page
				PositionProperties nextpos = doc.getPositionProps(null);

				/*
				 * if (!bitmapInfoNext.bitmap.isRecycled()) {
				 * bitmapInfoNext.bitmap.recycle();// 回收以后在重新建sbp
				 * bitmapInfoNext.bitmap = null; System.gc(); }
				 */
				bitmapInfoNext.position = nextpos;
				/*
				 * try { bitmapInfoNext.bitmap = Bitmap.createBitmap(width,
				 * height, Bitmap.Config.ARGB_8888); WeakReference<Bitmap>
				 * bitmapcache = new
				 * WeakReference<Bitmap>(bitmapInfoNext.bitmap);//
				 * 添加一个软引用要是内存溢出可以释放内存 bitmapInfoNext.bitmap = null;
				 * bitmapInfoNext.bitmap = bitmapcache.get(); } catch (Error e)
				 * { Log.i("ytreader", "cool Create bitmap failed!");
				 * activity.finish(); CrashHandler.uncaughtError(activity, e); }
				 */
				doc.getPageImage(bitmapInfoNext.bitmap);
				/*
				 * try { FileOutputStream out = new FileOutputStream(
				 * "/mnt/sdcard/Books/a.png");
				 * bi.bitmap.compress(Bitmap.CompressFormat.PNG, 90, out); }
				 * catch (Exception e) { e.printStackTrace(); }
				 */
			} else {
				return null;
			}
		}
		return bitmapInfoNext;
	}

	BitmapInfo bitmapInfoNext = new BitmapInfo();

	/**
	 * 上一页
	 */
	@Override
	public void prePage() throws IOException {
		Log.v("prePage", "1");
		search_text = null;
		doc.clearSelection();
		action = -1;
		// bitmapInfoNext = preparePageImage(-1);
		// if (null != bitmapInfoNext) {
		// coolActivity.widget.setBitmaps(
		// coolActivity.application.getCurPageBitmap(),
		// coolActivity.application.getNextPageBitmap());// TODO
		// PositionProperties currpos = doc.getPositionProps(null);
		// double rate = (double) (currpos.pageNumber)
		// / (double) currpos.pageCount * 1000;
		// setReadingLocation((long) rate);
		// }
	}

	/**
	 * 下一页
	 */
	@Override
	public void nextPage() throws IOException {
		Log.v("nextPage", "1");
		search_text = null;
		doc.clearSelection();
		action = 1;
		// bitmapInfoNext = preparePageImage(1);
		// if (null != bitmapInfoNext) {
		// coolActivity.widget.setBitmaps(
		// coolActivity.application.getCurPageBitmap(),
		// coolActivity.application.getNextPageBitmap());// TODO
		// PositionProperties currpos = doc.getPositionProps(null);
		// double rate = (double) (currpos.pageNumber)
		// / (double) currpos.pageCount * 1000;
		// setReadingLocation((long) rate);
		// }
	}

	/**
	 * 跳转到指定页
	 */
	@Override
	public void gotoPage(long location, boolean handleMessyCode)
			throws IOException {
		if (-1 != book.getTotalPage()) {
			if (location < 0 || location > book.getTotalPage())
				return;
		}
		PositionProperties currpos = doc.getPositionProps(null);
		double page_d = (double) location / 1000.0 * currpos.pageCount;
		int page = (int) Math.round(page_d);
		int totalPageIndex = currpos.pageCount - 1;
		doc.doCommand(112, page);
		if (null != search_text) {
			if (0 == page) {
				doc.findText(search_text,// 文本
						-1, // 0:当前页到末页, -1:从首页到当前页, 1:下一页到末页
						0, // 0为正向，1为逆向
						1);// 0为大小写敏感，1为不敏感
			} else if (page <= totalPageIndex) {
				doc.findText(search_text,// 文本
						0, // 0:当前页到末页, -1:从首页到当前页, 1:下一页到末页
						0, // 0为正向，1为逆向
						1);// 0为大小写敏感，1为不敏感
			} else {
				doc.findText(search_text,// 文本
						0, // 0:当前页到末页, -1:从首页到当前页, 1:下一页到末页
						0, // 0为正向，1为逆向
						1);// 0为大小写敏感，1为不敏感
			}
		}
		action = 0;
		// bitmapInfoNext = preparePageImage(0);
		// if (null != bitmapInfoNext) {
		// coolActivity.widget.setBitmaps(
		// coolActivity.application.getCurPageBitmap(),
		// coolActivity.application.getNextPageBitmap());// TODO
		// currpos = doc.getPositionProps(null);
		// setReadingLocation(location);
		// }
	}

	public void setReadingRate() {
		float fPercent = (float) (getReadingLocation() * 1.0 / book
				.getTotalPage());
		DecimalFormat df = new DecimalFormat("#0.0");
		String strPercent = df.format(fPercent * 100) + "%";
		coolActivity.pageNumView.setText(strPercent);
	}

	@Override
	public void onDraw(Canvas canvas, String searchText) {
		int maction = action;
		if (canvas.equals(coolActivity.application.getCurPageCanvas())) {
			Log.v("canvas", "cur");
			bitmap = coolActivity.application.getCurPageBitmap();
			maction = 0;
		} else {
			bitmap = coolActivity.application.getNextPageBitmap();
			Log.v("canvas", "next");
		}
		Log.v("onDraw", "action " + action + " " + maction);
		bitmapInfoNext = preparePageImage(maction);
		if (null != bitmapInfoNext) {
			// coolActivity.widget.setBitmaps(
			// coolActivity.application.getCurPageBitmap(),
			// coolActivity.application.getNextPageBitmap());// TODO

			PositionProperties currpos = doc.getPositionProps(null);
			double rate = (double) (currpos.pageNumber)
					/ (double) currpos.pageCount * 1000;
			setReadingLocation((long) rate);
			setReadingRate();
		}
		// coolActivity.widget.postInvalidate();
	}

	@Override
	public boolean isfirstPage() {
		PositionProperties pos = doc.getPositionProps(null);
		if(0 == pos.pageNumber) {
			return true;
		}
		return false;
	}

	@Override
	public boolean islastPage() {
		PositionProperties pos = doc.getPositionProps(null);
		if(pos.pageCount - 1 == pos.pageNumber) {
			return true;
		}
		return false;
	}

	@Override
	public long getReadingLocation() {
		return readingLocation;
	}

	@Override
	public void setReadingLocation(long location) {
		if (location < 0)
			return;
		readingLocation = location;
	}

	@Override
	public void addBookmark() {
		Bookmark bookmark = doc.getCurrentPageBookmark();
		String text = bookmark.getPosText();
		Cursor cursor = appContext.getContentResolver().query(
				DBSchema.CONTENT_URI_BOOKMARK,
				new String[] { BaseColumns._ID },
				DBSchema.COLUMN_CHAPTER_BOOKID + "=? AND "
						+ DBSchema.COLUMN_BOOKMARK_LOCATION + "=?",
				new String[] { book.getId() + "", getReadingLocation() + "" },
				null);

		// 查询是否已经存在
		if (cursor.moveToFirst()) {
			cursor.close();
			Toast.makeText(appContext, R.string.bookmark_exist,
					Toast.LENGTH_SHORT).show();
			return;
		}
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(DBSchema.COLUMN_BOOKMARK_LOCATION, getReadingLocation());
		values.put(DBSchema.COLUMN_BOOKMARK_DESCRIPTION, text);
		values.put(DBSchema.COLUMN_BOOKMARK_BOOKID, book.getId());
		try {
			appContext.getContentResolver().insert(
					DBSchema.CONTENT_URI_BOOKMARK, values);
		} catch (Exception e) {
			System.out.println("addBookmark error");
		}
		Toast.makeText(appContext, R.string.bookmark_added, Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 遍历目录信息
	 * 
	 * @return
	 */
	private List<Chapter> getContents() {
		List<Chapter> list = new ArrayList<Chapter>();
		TOCItem toc = doc.getTOC();
		if (null != toc) {
			int len = toc.getChildCount();
			for (int i = 0; i < len; i++) {
				TOCItem thisItem = toc.getChild(i);
				Chapter chapter = new Chapter();
				chapter.setLocation(thisItem.getPage());// 页码从0开始，需要纠正
				chapter.setTitle(thisItem.getName());
				chapter.setBookId(book.getId());
				list.add(chapter);
			}
		}
		return list;
	}

	/**
	 * 插入目录到数据库
	 * 
	 * @param List
	 *            <Chapter>
	 */
	private void insertContent(List<Chapter> list) {
		if (0 == list.size())
			return;
		PositionProperties currpos = doc.getPositionProps(null);
		int totalPage = currpos.pageCount;
		ContentValues values = new ContentValues();
		// 横屏竖屏都不一样，需要删除了重新添加
		try {
			appContext.getContentResolver().delete(
					DBSchema.CONTENT_URI_CHAPTER,
					DBSchema.COLUMN_CHAPTER_BOOKID + "=? ",
					new String[] { book.getId() + "" });
		} catch (Exception e) {
			System.out.println("deleteContent error");
		}

		int size = list.size();
		for (int a = 0; a < size; a++) {
			values.put(DBSchema.COLUMN_CHAPTER_BOOKID, list.get(a).getBookId());
			long rate = (long) ((double) list.get(a).getLocation()
					/ (double) totalPage * 1000.0);
			values.put(DBSchema.COLUMN_CHAPTER_LOCATION, rate);
			values.put(DBSchema.COLUMN_CHAPTER_TITLE, list.get(a).getTitle());
			try {
				appContext.getContentResolver().insert(
						DBSchema.CONTENT_URI_CHAPTER, values);
			} catch (Exception e) {
				System.out.println("insertContent error");
			}
		}
		list.clear();// 释放内存
	}

	private SharedPreferences style; // 字体样式的配置文件

	private int marginWidth, marginHeight;

	private int fontSize = 20;

	private int lineSpacingPercent = 120;

	private int spacingPixels = 4;

	private String myFont = "Droid Sans";

	/**
	 * 设置样式
	 */
	@Override
	public void setTextStyle() {
		style = appContext.getSharedPreferences(Constant.STYLE_REFERENCE, 0);
		marginWidth = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_WIDTH, // 获取左右边距
				Constant.STYLE_DEFAULT_MARGIN_WIDTH); // 获取不到时返回默认值
		marginHeight = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_HEIGHT,// 获取上下边距
				Constant.STYLE_DEFAULT_MARGIN_HEIGHT); // 获取不到时返回默认值
		fontSize = style.getInt(DBSchema.COLUMN_STYLE_SIZE,
				Constant.STYLE_DEFAULT_SIZE);
		spacingPixels = style.getInt(DBSchema.COLUMN_STYLE_LINE_SPACING,// 获取行距
				Constant.STYLE_DEFAULT_LINE_SPACING); // 获取不到时返回默认值

		myFont = style.getString(DBSchema.COLUMN_STYLE_TYPEFACE,
				Constant.STYLE_DEFAULT_TYPEFACE);

		if (myFont.endsWith(Constant.STYLE_TYPEFACE[0])) {
			myFont = Constant.STYLE_TYPEFACE_TITLE[0];
		} else if (myFont.endsWith(Constant.STYLE_TYPEFACE[1])) {
			myFont = Constant.STYLE_TYPEFACE_TITLE[1];
		} else if (myFont.endsWith(Constant.STYLE_TYPEFACE[2])) {
			myFont = Constant.STYLE_TYPEFACE_TITLE[2];
		} else if (myFont.endsWith(Constant.STYLE_TYPEFACE[3])) {
			myFont = Constant.STYLE_TYPEFACE_TITLE[3];
		} else if (myFont.endsWith(Constant.STYLE_TYPEFACE[4])) {
			myFont = Constant.STYLE_TYPEFACE_TITLE[4];
		} else {
			myFont = "Droid Sans";
		}

		switch (marginWidth) {
		case Constant.STYLE_MARGIN_WIDTH1:
			marginWidth = 20;
			break;
		case Constant.STYLE_MARGIN_WIDTH2:
			marginWidth = 30;
			break;
		case Constant.STYLE_MARGIN_WIDTH3:
			marginWidth = 40;
			break;
		default:
			marginWidth = 30;
		}

		switch (marginHeight) {
		case Constant.STYLE_MARGIN_HEIGHT1:
			marginHeight = 20;
			break;
		case Constant.STYLE_MARGIN_HEIGHT2:
			marginHeight = 30;
			break;
		case Constant.STYLE_MARGIN_HEIGHT3:
			marginHeight = 40;
			break;
		default:
			marginHeight = 30;
		}

		switch (spacingPixels) {
		case Constant.STYLE_LINE_SPACING1:
			lineSpacingPercent = 110;
			break;
		case Constant.STYLE_LINE_SPACING2:
			lineSpacingPercent = 120;
			break;
		case Constant.STYLE_LINE_SPACING3:
			lineSpacingPercent = 130;
			break;
		default:
			lineSpacingPercent = 120;
		}

		if (initOrNot) {
			props = loadSettings();
			applySettings(props, false, false);
		}
	}

	/**
	 * 搜索文本
	 * 
	 * @param text
	 * @return
	 */
	static String search_text;

	public static void search(String text) {
		if (null == doc)
			return;
		search_text = text;
		int count = doc.getPositionProps(null).pageCount;
		for (int i = 0; i < count;) {
			doc.doCommand(112, i); // 跳转到第i页
			boolean b = doc.findText(text,// 文本
					0, // 0:当前页到末页, -1:从首页到当前页, 1:下一页到末页
					0, // 0为正向，1为逆向
					1);// 0为大小写敏感，1为不敏感

			if (b) {
				String title = doc.getSearchTitle();
				PositionProperties currProp = doc.getPositionProps(null);
				if (currProp.pageNumber < i) {
					i++;
					continue;
				}
				i = currProp.pageNumber + 1;
				long rate = (currProp.pageNumber) * 1000 / currProp.pageCount;
				TextSearch.searchResultLocation.add(rate);
				TextSearch.searchResultDescription.add(title);
			} else {
				i++;
			}

		}
		doc.clearSelection();
	}

}
