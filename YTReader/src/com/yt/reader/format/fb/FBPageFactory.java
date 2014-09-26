package com.yt.reader.format.fb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.library.FBBook;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLBase64EncodedImage;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLView.PageIndex;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.yt.reader.R;
import com.yt.reader.base.BookPageFactory;
import com.yt.reader.database.DBSchema;
import com.yt.reader.model.Book;
import com.yt.reader.model.Chapter;
import com.yt.reader.utils.Constant;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;

public final class FBPageFactory extends BookPageFactory {

	private FBView fbTextView; // 内容
	private BookModel fbModel; // fb的bookModel，用以解码、缓存、提取meta、封面图片等
	private long m_readingLocation = 0; // 历史位置
	private String str_bookmark_text; // 书签描述
	private FBMainActivity activity; // activity
	private Context appContext;

	int width;
	int height;
	Thread decodeThread;

	private int marginWidth, marginHeight;

	private Bitmap bitmap;// 判断使用curPageBitmap还是nextPageBitmap
	private int action;// 记录是gotoPage还是翻页
	private long gotoLocation;// 记录gotoPage的位置

	public FBPageFactory(int w, int h, FBMainActivity activity) {
		super(w, h);
		this.width = w;
		this.height = h;
		this.activity = activity;
		this.appContext = activity.getApplicationContext();
		AndroidFontUtil.setContext(this.appContext);
	}

	/* (non-Javadoc)
	 * @see com.yt.reader.base.BookPageFactory#openbook(com.yt.reader.model.Book)
	 */
	@Override
	public void openbook(Book book) throws IOException {
		// 获取绝对路径+文件名+扩展名
		final String path = book.getPath() + "/" + book.getName();
		if (null == ZLApplication.Instance()) {
			new ZLApplication(); // 创建ZLApplication，以获取静态app
		}
		fbTextView = new FBView();
		setTextStyle();
		ZLApplication.Instance().setView(fbTextView);
		/*try {
			System.out.println("fbrun = "
					+ Thread.currentThread().getId());
			FBBook fbbook = new FBBook(ZLFile.createFileByPath(path));
			fbOpenBook(fbbook);// 打开电子书
		} catch (BookReadingException e) {
			e.printStackTrace();
		}*/
		decodeThread = new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("fbrun = "
							+ Thread.currentThread().getId());
					FBBook fbbook = new FBBook(ZLFile.createFileByPath(path));
					fbOpenBook(fbbook);// 打开电子书
				} catch (BookReadingException e) {
					e.printStackTrace();
				}
			}

		};
		decodeThread.start();

	}

	public void recycle() {
		fbTextView = null;
		fbModel = null;
		activity = null;
	}

	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
	}

	// 获取阅读位置信息
	@Override
	public long getReadingLocation() {
		return m_readingLocation;
	}

	/**
	 * 增加书签
	 */
	@Override
	public void addBookmark() {
		ZLTextWordCursor textCursor = fbTextView.getStartCursor(); // 获取当前阅读位置
		m_readingLocation = mergeLocation(textCursor); // 阅读位置，三个int合并成一个long
		Cursor cursor = activity.getContentResolver().query(
				DBSchema.CONTENT_URI_BOOKMARK,
				new String[] { BaseColumns._ID },
				DBSchema.COLUMN_CHAPTER_BOOKID + "=? AND "
						+ DBSchema.COLUMN_BOOKMARK_LOCATION + "=?",
				new String[] { activity.book.getId() + "",
						m_readingLocation + "" }, null);

		// 查询是否已经存在
		if (cursor.moveToFirst()) {
			cursor.close();
			Toast.makeText(appContext, R.string.bookmark_exist,
					Toast.LENGTH_SHORT).show();
			return;
		}
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(DBSchema.COLUMN_BOOKMARK_LOCATION, m_readingLocation);
		values.put(DBSchema.COLUMN_BOOKMARK_DESCRIPTION, str_bookmark_text);
		values.put(DBSchema.COLUMN_BOOKMARK_BOOKID, activity.book.getId());
		try {
			activity.getContentResolver().insert(DBSchema.CONTENT_URI_BOOKMARK,
					values);
		} catch (Exception e) {
			System.out.println("addBookmark error");
		}
		Toast.makeText(appContext, R.string.bookmark_added, Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 将阅读的位置信息三个int合并成一个long
	 * 
	 * @return long（64位）
	 */
	private long mergeLocation(ZLTextWordCursor textCursor) {
		str_bookmark_text = createBookmarkText(textCursor);
		long myParagraphIndex = 0; // 段落索引
		long myElementIndex = 0; // 元素索引
		long myCharIndex = 0; // 字符索引

		myParagraphIndex = textCursor.getParagraphIndex();
		myElementIndex = textCursor.getElementIndex();
		myCharIndex = textCursor.getCharIndex();

		long location = 0;

		myParagraphIndex = myParagraphIndex << 44;
		myElementIndex = myElementIndex << 24;
		location = myParagraphIndex + myElementIndex + myCharIndex;

		return location;
	}

	int myParagraphIndex_1 = 0; // 段落索引

	int myElementIndex_1 = 0; // 元素索引

	int myCharIndex_1 = 0; // 字符索引

	/**
	 * 将location拆分为三个long
	 * 
	 * @param location
	 * @return
	 */
	private boolean separateLocation(long location) {
		long temp = 0;
		temp = location >> 44;
		myParagraphIndex_1 = (int) temp; // 高20位
		temp = (location >> 24) & 0x00000FFFFF;
		myElementIndex_1 = (int) temp; // 中20位
		temp = location & 0x00000FFFFFF;
		myCharIndex_1 = (int) temp; // 低24位
		return true;
	}

	void fbOpenBook(FBBook fbBook) {
		if (fbBook != null) {
			fbTextView.clearCaches();

			fbModel = null;
			System.gc();
			System.gc();
			try {
				fbModel = BookModel.createModel(fbBook);// 此处开始解码
			} catch (BookReadingException e) {
				e.printStackTrace();
			}

			// 跳转到历史位置
			if (fbModel != null) {
				List<Chapter> list = getContents(fbModel.TOCTree); // 遍历目录树到list中
				insertContent(list); // 添加目录到数据库

				if (!isCoverExist()) {
					ZLImage coverImg = fbBook.getCover();
					if (null != coverImg) {
						Bitmap bitmapCover = null;
						if (coverImg instanceof ZLBase64EncodedImage) {// fb2封面图片
							ZLBase64EncodedImage img = (ZLBase64EncodedImage) coverImg;
							byte[] b;
							try {
								b = img.byData();
								bitmapCover = BitmapFactory.decodeByteArray(b,
										0, b.length, null);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else if (coverImg instanceof ZLImageProxy) {// 其他格式封面图片
							ZLImageProxy img = (ZLImageProxy) coverImg;
							ZLSingleImage singleImg = img.getRealImage();
							if (null != singleImg) {
								final ZLAndroidImageManager mgr = (ZLAndroidImageManager) ZLAndroidImageManager
										.Instance();
								final ZLAndroidImageData data = mgr
										.getImageData(singleImg);
								bitmapCover = data.getFullSizeBitmap();
							}
						}
						if (null != bitmapCover) {
							// addCoverImg2DB(bitmapCover);
							String path = saveCover2SD(bitmapCover,
									activity.book.getPath() + "/"
											+ activity.book.getName());
							addCoverPath2DB(path);
							bitmapCover.recycle();
							bitmapCover = null;
							System.gc();
						}
					}
				}
				Book book = activity.book;
				try {
					if (fbBook.authors() == null
							|| fbBook.authors().size() == 0) {
						book.setAuthor(null);
					} else {
						book.setAuthor(fbBook.authors().get(0).DisplayName);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (!book.getFileType().equals("RTF")) {
					book.setRealName(fbBook.getTitle());

					if (fbBook.getTitle().contains("&#")) {
						String newtitle = getUnicodeChar(fbBook.getTitle());
						book.setRealName(newtitle);
					}
				}

				m_readingLocation = activity.book.getCurrentLocation(); // 获取历史阅读位置

				ZLTextHyphenator.Instance().load(fbBook.getLanguage()); // 单词换行切分
				fbTextView.setModel(fbModel.getTextModel()); // 缓存文件解析，首先把指针指向开头位置
				updateBook(book); // 需要用到BookTextModel
				getLibrary().setView(fbTextView);

				final ZLView view = getLibrary().getCurrentView();
				// 传递PaintContext参数

				// 传递一个新建Canvas句柄，在bitmap上绘画
				// 宽度是屏幕宽度
				// 高度是主显示高度（页码部分单独绘画）
				// 滚动条宽度（忽略）
				final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
						new Canvas(activity.application.getCurPageBitmap()),
						width, height, 0);
				view.setPaintContext(context);

				gotoBookmark(m_readingLocation); // 跳转到历史位置
				view.paint(context, ZLView.PageIndex.current); // 画内容

				// activity.widget.setBitmaps(bitmap);
				view.onScrollingFinished(ZLView.PageIndex.current);
				activity.widget.postInvalidate();
				handler.post(new TimeUpdateTitle()); // 更新标题信息
			}
		}
	}

	Handler handler = new Handler();

	class TimeUpdateTitle implements Runnable {
		@Override
		public void run() {
			// 设置标题、进度（UI线程）
			setReadingRate();
			setReadingTitle();
			activity.cancleDialog();
			onDraw(activity.application.getCurPageCanvas(), null);
		}
	}

	private String getUnicodeChar(String title) {
		String[] str = title.split("&#");
		int lens = str.length;
		for (int i = 0; i < lens; i++) {
			if (str[i].length() < 5)
				continue;
			int num = Integer.parseInt(str[i].substring(0, 5));
			char c = (char) num;
			char[] cc = new char[1];
			cc[0] = c;
			String forReplace = "&#" + str[i].substring(0, 5) + ";";
			title = title.replace(forReplace, new String(cc));
		}
		return title;
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
		ContentValues values = new ContentValues();
		Cursor cursor = activity.getContentResolver().query(
				DBSchema.CONTENT_URI_CHAPTER, new String[] { BaseColumns._ID },
				DBSchema.COLUMN_CHAPTER_BOOKID + "=?",
				new String[] { activity.book.getId() + "" }, null);
		if (cursor.moveToFirst()) {
			cursor.close();
			return;// 已存在
		}
		cursor.close();

		int size = list.size();
		for (int a = 0; a < size; a++) {
			values.put(DBSchema.COLUMN_CHAPTER_BOOKID, list.get(a).getBookId());
			values.put(DBSchema.COLUMN_CHAPTER_LOCATION, list.get(a)
					.getLocation());
			values.put(DBSchema.COLUMN_CHAPTER_TITLE, list.get(a).getTitle());
			try {
				activity.getContentResolver().insert(
						DBSchema.CONTENT_URI_CHAPTER, values);
			} catch (Exception e) {
				System.out.println("insertContent error");
			}
		}
		list.clear();// 释放内存
	}

	/**
	 * 遍历目录树
	 * 
	 * @param TOCTree
	 * @return List<Chapter>
	 */
	private List<Chapter> getContents(TOCTree root) {
		if (null == root)
			return null;
		List<Chapter> listChapter = new ArrayList<Chapter>();

		for (TOCTree tree : root) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference == null) {
				continue;
			}
			Chapter chapter = new Chapter();
			long this_location = reference.ParagraphIndex;
			this_location = this_location << 44;
			chapter.setLocation(this_location);
			chapter.setTitle(tree.getText());
			chapter.setBookId(activity.book.getId());
			listChapter.add(chapter);
		}
		return listChapter;
	}

	/**
	 * 更新book信息
	 */
	private void updateBook(Book book) {
		ContentValues values = new ContentValues();
		Book pre_book = activity.book;
		pre_book.setAuthor(book.getAuthor());
		pre_book.setRealName(book.getRealName());
		int no_para = fbTextView.getModel().getParagraphsNumber();
		long total_page = no_para;
		total_page = total_page << 44;
		pre_book.setTotalPage(total_page);// 总偏移量

		values.put(DBSchema.COLUMN_BOOK_AUTHOR, book.getAuthor());
		values.put(DBSchema.COLUMN_BOOK_REALNAME, book.getRealName());
		values.put(DBSchema.COLUMN_BOOK_TOTAL_PAGE, book.getTotalPage());

		try {
			activity.getContentResolver().update(DBSchema.CONTENT_URI_BOOK,
					values, BaseColumns._ID + " = " + activity.book.getId(),
					null);
		} catch (Exception e) {
			System.out.println("updateBook error");
		}
	}

	private boolean isCoverExist() {
		boolean b = false;
		Cursor cursor = activity
				.getContentResolver()
				.query(DBSchema.CONTENT_URI_BOOK, // 表名
						new String[] { BaseColumns._ID,
								DBSchema.COLUMN_BOOK_COVER_PATH }, // 选择的列
						BaseColumns._ID + "=? AND "
								+ DBSchema.COLUMN_BOOK_COVER_PATH + " NOT NULL", // where
						new String[] { activity.book.getId() + "" }, // where对应的值
						null); // order by

		// 查询是否已经存在
		if (cursor.moveToFirst()) {
			try {
				File file = new File(cursor.getString(cursor
						.getColumnIndex(DBSchema.COLUMN_BOOK_COVER_PATH)));
				if (file.exists()) {
					b = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		return b;
	}

	final int coverWidth = 200;

	/*
	 * 保存封面图片到SD卡
	 */
	private String saveCover2SD(Bitmap bitmap, String path) {
		// 获取路径
		String coverPath = Environment.getExternalStorageDirectory() + "/cover";
		File dir = new File(coverPath);
		// 判断文件夹是否存在
		if (!dir.exists()) {
			dir.mkdir();
		}
		String coverFullPath = "";

		try {
			String md5Path = MD5(path);
			coverFullPath = coverPath + "/" + md5Path + ".png";
			FileOutputStream out = new FileOutputStream(coverFullPath);

			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			Matrix matrix = new Matrix();
			float scale = ((float) coverWidth / width);
			matrix.postScale(scale, scale);
			Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
					matrix, true);
			newbmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
			out = null;
			newbmp.recycle();
			newbmp = null;
		} catch (Exception e) {
			return null;
		}
		return coverFullPath;
	}

	/**
	 * 字符串转MD5哈希值
	 * 
	 * @param s
	 * @return
	 */
	private static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 增加封面路径
	 * 
	 * @param String
	 */
	private void addCoverPath2DB(String path) {
		if (null == path)
			return;

		ContentValues values = new ContentValues();
		values.put(DBSchema.COLUMN_BOOK_COVER_PATH, path);
		try {
			activity.getContentResolver().update(DBSchema.CONTENT_URI_BOOK,
					values, BaseColumns._ID + " = " + activity.book.getId(),
					null);
		} catch (Exception e) {
			System.out.println("addCoverPath2DB error");
		}
	}

	public void gotoBookmark(long location) {
		separateLocation(location);

		fbTextView.gotoPosition(myParagraphIndex_1, myElementIndex_1,
				myCharIndex_1);
		getLibrary().setView(fbTextView);
	}

	/**
	 * 提取bookmark文本
	 * 
	 * @param cursor
	 * @return
	 */
	private String createBookmarkText(ZLTextWordCursor cursor) {
		cursor = new ZLTextWordCursor(cursor);

		final StringBuilder builder = new StringBuilder();
		final StringBuilder sentenceBuilder = new StringBuilder();
		final StringBuilder phraseBuilder = new StringBuilder();

		int wordCounter = 0;
		int sentenceCounter = 0;
		int storedWordCounter = 0;
		boolean lineIsNonEmpty = false;
		boolean appendLineBreak = false;
		mainLoop: while ((wordCounter < 20) && (sentenceCounter < 3)) {
			while (cursor.isEndOfParagraph()) {
				if (!cursor.nextParagraph()) {
					break mainLoop;
				}
				if ((builder.length() > 0)
						&& cursor.getParagraphCursor().isEndOfSection()) {
					break mainLoop;
				}
				if (phraseBuilder.length() > 0) {
					sentenceBuilder.append(phraseBuilder);
					phraseBuilder.delete(0, phraseBuilder.length());
				}
				if (sentenceBuilder.length() > 0) {
					if (appendLineBreak) {
						builder.append("\n");
					}
					builder.append(sentenceBuilder);
					sentenceBuilder.delete(0, sentenceBuilder.length());
					++sentenceCounter;
					storedWordCounter = wordCounter;
				}
				lineIsNonEmpty = false;
				if (builder.length() > 0) {
					appendLineBreak = true;
				}
			}
			final ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				final ZLTextWord word = (ZLTextWord) element;
				if (lineIsNonEmpty) {
					phraseBuilder.append(" ");
				}
				phraseBuilder.append(word.Data, word.Offset, word.Length);
				++wordCounter;
				lineIsNonEmpty = true;
				switch (word.Data[word.Offset + word.Length - 1]) {
				case ',':
				case ':':
				case ';':
				case ')':
					sentenceBuilder.append(phraseBuilder);
					phraseBuilder.delete(0, phraseBuilder.length());
					break;
				case '.':
				case '!':
				case '?':
					++sentenceCounter;
					if (appendLineBreak) {
						builder.append("\n");
						appendLineBreak = false;
					}
					sentenceBuilder.append(phraseBuilder);
					phraseBuilder.delete(0, phraseBuilder.length());
					builder.append(sentenceBuilder);
					sentenceBuilder.delete(0, sentenceBuilder.length());
					storedWordCounter = wordCounter;
					break;
				}
			}
			cursor.nextWord();
		}
		if (storedWordCounter < 4) {
			if (sentenceBuilder.length() == 0) {
				sentenceBuilder.append(phraseBuilder);
			}
			if (appendLineBreak) {
				builder.append("\n");
			}
			builder.append(sentenceBuilder);
		}
		return builder.toString();
	}

	/**
	 * 转到上一页内容
	 */
	@Override
	public void prePage() throws IOException {
		action = -1;
		// final ZLView view = getLibrary().getCurrentView();
		// drawOnBitmap(ZLView.PageIndex.previous,
		// activity.application.getNextPageBitmap());
		// // activity.widget.setBitmaps(bitmap);
		// view.onScrollingFinished(ZLView.PageIndex.previous);
		//
		// FBView fbview = (FBView) view;
		// setReadingLocation(fbview.getStartCursor());
		// setReadingRate();
	}

	private void drawOnBitmap(PageIndex viewPage, Bitmap bitmap) {
		final ZLView view = getLibrary().getCurrentView();
		if (view == null) {
			return;
		}

		// 传递一个新建Canvas句柄，在bitmap上绘画
		// 宽度是屏幕宽度
		// 高度是主显示高度（页码部分单独绘画）
		// 滚动条宽度（忽略）
		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
				new Canvas(bitmap), width, height, 0);
		view.paint(context, viewPage);// 画内容
	}

	/**
	 * 转到下一页内容
	 */
	@Override
	public void nextPage() throws IOException {
		action = 1;
		// final ZLView view = getLibrary().getCurrentView();
		// drawOnBitmap(ZLView.PageIndex.next,
		// activity.application.getNextPageBitmap());
		// // activity.widget.setBitmaps(bitmap);
		// view.onScrollingFinished(ZLView.PageIndex.next);
		//
		// FBView fbview = (FBView) view;
		// setReadingLocation(fbview.getStartCursor());
		// setReadingRate();
	}

	public void setReadingTitle() {
		activity.bookNameView.setText(activity.book.getRealName());
	}

	public void setReadingRate() {
		float fPercent = (float) (getReadingLocation() * 1.0 / activity.book
				.getTotalPage());
		DecimalFormat df = new DecimalFormat("#0.0");
		String strPercent = df.format(fPercent * 100) + "%";
		activity.pageNumView.setText(strPercent);
	}

	@Override
	public boolean isfirstPage() {
		if (0 == m_readingLocation)
			return true;
		return false;
	}

	@Override
	public boolean islastPage() {
		final ZLTextWordCursor end = fbTextView.getEndCursor(); // 获取当前页结束指针
		separateLocation(activity.book.getTotalPage());
		if (end.getParagraphIndex() + 1 == myParagraphIndex_1
				&& end.isEndOfParagraph()) {
			return true;
		}
		return false;
	}

	@Override
	public void setReadingLocation(long location) {
		m_readingLocation = location;
	}

	public void setReadingLocation(ZLTextWordCursor textCursor) {
		long location = mergeLocation(textCursor);
		setReadingLocation(location);
	}

	@Override
	public void onDraw(Canvas canvas, String searchText) {
		int maction = action;
		if (canvas.equals(activity.application.getCurPageCanvas())) {
			Log.v("canvas", "cur");
			bitmap = activity.application.getCurPageBitmap();
			maction = 0;
		} else {
			bitmap = activity.application.getNextPageBitmap();
			Log.v("canvas", "next");
		}
		Log.v("onDraw", "action " + action + " " + maction);
		final ZLView view = getLibrary().getCurrentView();

		if (maction == 0) {// gotoPage
			gotoBookmark(getReadingLocation());
			FBView fbview = (FBView) view;
			Log.v("fbview",fbview+"");
			setReadingLocation(fbview.getStartCursor());
			setReadingRate();
			drawOnBitmap(ZLView.PageIndex.current, bitmap);
			view.onScrollingFinished(ZLView.PageIndex.current);
		} else {
			if (action == -1) {// prePage
				drawOnBitmap(ZLView.PageIndex.previous,bitmap);
				view.onScrollingFinished(ZLView.PageIndex.previous);
			} else {// nextPage
				drawOnBitmap(ZLView.PageIndex.next,bitmap);
				view.onScrollingFinished(ZLView.PageIndex.next);
			}
			FBView fbview = (FBView) view;
			setReadingLocation(fbview.getStartCursor());
			setReadingRate();
		}
	}

	@Override
	public void gotoPage(long location, boolean handleMessyCode)
			throws IOException {
		action = 0;
	}

	@Override
	public void setTextStyle() {
		final SharedPreferences style = activity.getSharedPreferences(Constant.STYLE_REFERENCE, 0);
		marginWidth = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_WIDTH, // 获取左右两边空白尺寸
				Constant.STYLE_DEFAULT_MARGIN_WIDTH); // 获取不到时返回默认值
		marginHeight = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_HEIGHT,// 获取上下两边空白尺寸
				Constant.STYLE_DEFAULT_MARGIN_HEIGHT); // 获取不到时返回默认值

		// 设置FBview上下左右空白参数
		fbTextView.bottomMargin = marginHeight + 20;
		fbTextView.topMargin = marginHeight + 10;
		fbTextView.leftMargin = marginWidth;
		fbTextView.rightMargin = marginWidth;
	}

}
