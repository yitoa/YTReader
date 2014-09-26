package com.yt.reader;

import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;

import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yt.reader.base.BookPageFactory;
import com.yt.reader.base.BookView;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.database.DBSchema;
import com.yt.reader.database.OpenDataBase;
import com.yt.reader.model.Book;
import com.yt.reader.optionmenu.ContentActivity;
import com.yt.reader.optionmenu.FindActivity;
import com.yt.reader.utils.Constant;
import com.yt.reader.utils.DateUtils;
import com.yt.reader.utils.FileUtils;

/**
 * 处理打开书籍的共用Activity，弹出菜单、保存阅读记录等功能都在此类中。
 * 
 * @author lsj
 */
public abstract class BaseBookActivity extends YTReaderActivity {
	private Handler handler;

	public static final int CODE_CONTENT = 1;

	public static final int PDF_CODE_FIND = 4;

	public static final int CODE_FIND = 2;

	private String fpath;

	private String fname;

	public Book book;

	private ContentResolver resolver;

	private OpenDataBase odb;

	private RelativeLayout statusBarLayout;

	protected BookView bookView;

	public View bookinfoView, menuView, searchView, gotoView1, gotoView2,
			textView;// option

	// menu相关的对话框

	public RelativeLayout contentLayout;// 整个页面的layout

	public LinearLayout menuLayout;// option menu对话框

	public TextView bookNameView;

	protected ImageButton bookmarkButton;

	public TextView pageNumView;

	protected TextView contentButton;

	protected TextView findButton;

	protected TextView gotoButton;

	protected TextView textButton;

	public int screenWidth, screenHeight;

	protected BookPageFactory pagefactory;

	public LinearLayout searchLayout;// 搜索结果对话框

	protected long[] searchKeys;// 搜索结果位置，用于传递给FindAcivity

	protected int searchPage;// 搜索列表正在查看的页码，用于传递给FindAcivity

	protected String searchText;// 搜索文本

	protected int searchIndex;// 搜索结果中正在显示的当前位置

	private ImageButton searchListButton, previousButton, nextButton,
			closeSearchButton;

	private EditText inputSearch;

	public LinearLayout gotoLayout1, gotoLayout2;// goto对话框

	public ImageButton closeGotoButton1, closeGotoButton2;

	private Button gobackButton, gopageButton;

	protected TextView gotoPage;// 显示当前页和总页数

	protected SeekBar gotoSeekBar;

	private long originalPage;// 拖动条拖动前的页数或偏移

	protected EditText gotoInput;// 输入待跳转的页码或偏移，需要在子类中设置其hint值

	protected Button goButton;

	private Button goto0, goto1, goto2, goto3, goto4, goto5, goto6, goto7,
			goto8, goto9, gotoDot;

	private ImageButton gotoDelButton;

	public LinearLayout textLayout;

	private ImageButton closeTextButton;

	private TextView timeView;

	private ImageButton textSignA1, textImageA1, textSignA2, textImageA2,
			textSignA3, textImageA3, textSignA4, textImageA4, textSignA5,
			textImageA5, textSignA6, textImageA6, textSignA7, textImageA7;// 字号有关控件

	private ListView textFontList;// 字体列表

	private ImageButton textSignSpacing1, textImageSpacing1, textSignSpacing2,
			textImageSpacing2, textSignSpacing3, textImageSpacing3;// 行距有关控件

	private ImageButton textSignMargin1, textImageMargin1, textSignMargin2,
			textImageMargin2, textSignMargin3, textImageMargin3;// 边距有关控件

	private ImageButton textDefault;// 是否使用系统默认配置

	private SharedPreferences style;

	private SharedPreferences.Editor editor;

	private TreeMap<String, Boolean> typefaceMap;

	private FontListAdapter fontListAdapter;

	public YTReaderApplication application;

	private TimeThread timeThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contentLayout = new RelativeLayout(this);
		searchText = null;
		Runtime.getRuntime().maxMemory();
		Bundle bundle = this.getIntent().getBundleExtra("book");
		if (null == bundle) {
			Uri uri = this.getIntent().getData();
			fpath = Uri.decode(uri.getEncodedPath());
			if (null != fpath && fpath.contains("/")) {
				fpath = fpath.substring(0, fpath.lastIndexOf("/"));
				fname = fpath.substring(fpath.lastIndexOf("/") + 1);
			}
		} else {
			fpath = bundle.getString("path");
			fname = bundle.getString("name");
		}
		book = new Book();
		book.setName(fname);
		book.setPath(fpath);
		resolver = this.getContentResolver();
		odb = new OpenDataBase();
		Cursor cursor = odb.getOrInsertBook(book, resolver);
		if (!cursor.moveToFirst()) {
			Toast.makeText(this, R.string.fileerror, Toast.LENGTH_SHORT).show();
			this.finish();// 数据库操作异常，返回。
		}
		book.setId(cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)));// 书的id,方便以后检索
		try {
			book.setCurrentLocation(cursor.getLong(cursor
					.getColumnIndex(DBSchema.COLUMN_BOOK_CURRENT_LOCATION)));// 最近一次阅读位置
		} catch (RuntimeException e) {
			book.setCurrentLocation(0);
		}
		String fileType = cursor.getString(cursor
				.getColumnIndex(DBSchema.COLUMN_BOOK_FILETYPE));// 文件类型
		if (null != fileType)
			book.setFileType(fileType);
		else {
			book.setFileType(FileUtils.getFileType(book.getName()));
		}
		book.setTotalPage(cursor.getLong(cursor
				.getColumnIndex(DBSchema.COLUMN_BOOK_TOTAL_PAGE)));// 总页数或总偏移
		book.setRealName(cursor.getString(cursor
				.getColumnIndex(DBSchema.COLUMN_BOOK_REALNAME)));// 书名
		if (null == book.getRealName() || "".equals(book.getRealName().trim()))
			book.setRealName(fname);
		try {
			init();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		handler = new Handler();
		timeThread = new TimeThread();
		handler.post(timeThread);// 用于1分钟刷新一次时间
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.application.getCurPageBitmap().eraseColor(Color.TRANSPARENT);
		this.application.getNextPageBitmap().eraseColor(Color.TRANSPARENT);
		timeThread.stopThread();
		contentLayout.destroyDrawingCache();
	}

	private void init() {
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();

		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		Log.v("screenSize", "width=" + screenWidth + "; height=" + screenHeight);
		
		String state = null;
        // 获取Preferences中存放的数据
        SharedPreferences isherVerPre = getSharedPreferences("herver", -1);
        state = isherVerPre.getString("isherver", "1");
        // 判断横竖屏
        if ("2".equals(state) && screenWidth < screenHeight) {// 横屏
        	screenWidth ^= screenHeight;
        	screenHeight ^= screenWidth;
        	screenWidth ^= screenHeight;
        	Log.e("screenSize", "screenSize error, Exchange height and width");
        } else if ("1".equals(state) && screenHeight < screenWidth) {// 竖屏
        	screenWidth ^= screenHeight;
        	screenHeight ^= screenWidth;
        	screenWidth ^= screenHeight;
        	Log.e("screenSize", "screenSize error, Exchange height and width");
        }//added by zjq 2012.12.3
        
		application = (YTReaderApplication) this.getApplication();
		if (screenWidth >= screenHeight && !application.initialized(true)) {
			application.setBitmap(Bitmap.createBitmap(screenWidth,
					screenHeight, Bitmap.Config.ARGB_8888), Bitmap
					.createBitmap(screenWidth, screenHeight,
							Bitmap.Config.ARGB_8888), true);
		} else if (screenWidth < screenHeight
				&& !application.initialized(false)) {
			application.setBitmap(Bitmap.createBitmap(screenWidth,
					screenHeight, Bitmap.Config.ARGB_8888), Bitmap
					.createBitmap(screenWidth, screenHeight,
							Bitmap.Config.ARGB_8888), false);
		}

		bookinfoView = getLayoutInflater().inflate(
				R.layout.option_menu_book_info, null);
		statusBarLayout = (RelativeLayout) bookinfoView
				.findViewById(R.id.statusBar);

		timeView = (TextView) bookinfoView.findViewById(R.id.time);
		bookNameView = (TextView) bookinfoView.findViewById(R.id.bookName);
		bookmarkButton = (ImageButton) bookinfoView.findViewById(R.id.bookmark);
		bookmarkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pagefactory.addBookmark();
			}
		});
		pageNumView = (TextView) bookinfoView.findViewById(R.id.pageNum);
	}

	/**
	 * 弹出菜单的初始化和点击事件定义。
	 */
	public void makeButtonsView() {
		menuView = getLayoutInflater().inflate(R.layout.option_menu, null);
		menuLayout = (LinearLayout) menuView.findViewById(R.id.optionMenu);
		contentButton = (TextView) menuView.findViewById(R.id.content);

		contentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setOptionMenuInvisible();
				Intent intent = new Intent();
				intent.putExtra("bookId", book.getId());
				intent.putExtra("totalPage", book.getTotalPage());
				intent.putExtra("filetype", book.getFileType().toUpperCase());
				intent.setClass(BaseBookActivity.this, ContentActivity.class);
				BaseBookActivity.this.startActivityForResult(intent,
						CODE_CONTENT);
			}
		});

		findButton = (TextView) menuView.findViewById(R.id.find);
		findButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setOptionMenuInvisible();
				startFindIntent(false, false);
			}
		});

		gotoButton = (TextView) menuView.findViewById(R.id.go_to);

		gotoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setOptionMenuInvisible();
				if (null == gotoLayout1)
					makeGotoView1();
				gotoLayout1.setVisibility(View.VISIBLE);
				double persentd;
				if ("PDF".equals(book.getFileType())) {
					persentd = (double) (pagefactory.getReadingLocation() + 1)
							* 100 / book.getTotalPage();
				} else if ("TXT".equals(book.getFileType())
						&& pagefactory.islastPage()) {
					persentd = 100;
				} else {
					persentd = (double) pagefactory.getReadingLocation() * 100
							/ book.getTotalPage();
				}
				long percent = Math.round(persentd);
				gotoSeekBar.setProgress((int) percent);
				gotoPage.setText(percent + "%");
			}
		});

		textButton = (TextView) menuView.findViewById(R.id.text);
		if ("PDF".equals(book.getFileType())) {
			textButton
					.setBackgroundResource(R.drawable.option_menu_bg_right_disable);
			textButton.setClickable(false);
		} else {
			textButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					setOptionMenuInvisible();
					if (null == textView) {
						makeTextView();
						contentLayout.addView(textView);
					}
					textLayout.setVisibility(View.VISIBLE);
				}
			});
		}
		menuLayout.setVisibility(View.INVISIBLE);
	}

	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
	}

	/**
	 * 搜索结果返回时弹出的对话框
	 */
	public void makeSearchView() {
		searchView = getLayoutInflater().inflate(R.layout.option_menu_find2,
				null);
		searchLayout = (LinearLayout) searchView.findViewById(R.id.searchView);
		searchLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 什么都不做，防止页面消失。
			}
		});
		searchLayout.setVisibility(View.INVISIBLE);
		closeSearchButton = (ImageButton) searchView
				.findViewById(R.id.closeSearch);
		searchListButton = (ImageButton) searchView
				.findViewById(R.id.searchList);
		previousButton = (ImageButton) searchView.findViewById(R.id.previous);
		nextButton = (ImageButton) searchView.findViewById(R.id.next);
		inputSearch = (EditText) searchView.findViewById(R.id.inputSearch);
		closeSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					ZLAndroidLibrary library = getLibrary();
					if (null != library) {
						FBView fbTextView = (FBView) library.getCurrentView();
						if (null != fbTextView) {
							fbTextView.clearFindResults();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				searchLayout.setVisibility(View.INVISIBLE);
			}
		});
		searchListButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFindIntent(true, false);
			}
		});
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				while (searchIndex > 0
						&& pagefactory.getReadingLocation() == searchKeys[searchIndex - 1]) {// 同一页搜到多个结果，只draw一次
					Log.v("search", "skip search index " + searchIndex
							+ " in page " + pagefactory.getReadingLocation());
					--searchIndex;
				}
				if (searchIndex == 0) {
					Toast.makeText(BaseBookActivity.this, R.string.last_searchResult,
							Toast.LENGTH_SHORT).show();
				} else {
					if (--searchIndex >= 0) {
						pagefactory.setReadingLocation(searchKeys[searchIndex]);
						bookView.drawPage(searchText, searchKeys[searchIndex],
								true);
					} else {
						searchIndex = 0;
					}
				}
			}
		});
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				while (searchIndex + 1 < searchKeys.length
						&& pagefactory.getReadingLocation() == searchKeys[searchIndex + 1]) {// 同一页搜到多个结果，只draw一次
					Log.v("search", "skip search index " + searchIndex
							+ " in page " + pagefactory.getReadingLocation());
					++searchIndex;
				}

				if (searchIndex == searchKeys.length - 1) {
					Toast.makeText(BaseBookActivity.this, R.string.last_searchResult,
							Toast.LENGTH_SHORT).show();
				} else {
					if (++searchIndex < searchKeys.length) {
						pagefactory.setReadingLocation(searchKeys[searchIndex]);
						bookView.drawPage(searchText, searchKeys[searchIndex],
								true);
					} else {
						searchIndex = searchKeys.length - 1;
					}
				}
			}
		});
		inputSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFindIntent(true, true);
			}
		});
	}

	/**
	 * 处理与goto相关的控件
	 */
	public void makeGotoView1() {
		gotoView1 = getLayoutInflater().inflate(R.layout.option_menu_goto1,
				null);
		gotoLayout1 = (LinearLayout) gotoView1.findViewById(R.id.gotoView1);
		gotoLayout1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 什么都不做，防止页面消失。
			}
		});
		gotoLayout1.setVisibility(View.INVISIBLE);
		closeGotoButton1 = (ImageButton) gotoView1
				.findViewById(R.id.closeGoto1);
		closeGotoButton1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoLayout1.setVisibility(View.INVISIBLE);
			}
		});
		gobackButton = (Button) gotoView1.findViewById(R.id.goto_back);
		gobackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pagefactory.setReadingLocation(originalPage);
				bookView.drawPage(null, originalPage, false);
				int percent;
				if ("PDF".equals(book.getFileType())) {
					percent = Math.round((originalPage + 1) * 100
							/ book.getTotalPage());
				} else if ("TXT".equals(book.getFileType())
						&& pagefactory.islastPage()) {
					percent = 100;
				} else {
					percent = Math.round(originalPage * 100
							/ book.getTotalPage());
				}
				gotoSeekBar.setProgress(percent);
				gotoPage.setText(percent + "%");
				gobackButton.setEnabled(false);
				gobackButton.setTextColor(0xff666666);
			}
		});
		gopageButton = (Button) gotoView1.findViewById(R.id.goto_go);
		gopageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gotoLayout1.setVisibility(View.INVISIBLE);
				if (null == gotoView2) {
					makeGotoView2();
					contentLayout.addView(gotoView2);
				}
				gotoLayout2.setVisibility(View.VISIBLE);
			}
		});
		gotoPage = (TextView) gotoView1.findViewById(R.id.goto_text);
		gotoSeekBar = (SeekBar) gotoView1.findViewById(R.id.goto_seekbar);
		gotoSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				gotoPage.setText(progress + "%");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				originalPage = pagefactory.getReadingLocation();
				gobackButton.setEnabled(true);
				gobackButton.setTextColor(Color.BLACK);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				long location;
				if (progress == 0) {
					location = 0;
				} else if (progress == 100) {
					location = book.getTotalPage();
					try {
						pagefactory.prePage();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					double locationd = (double) progress * book.getTotalPage()
							/ 100;
					location = (long) locationd;
				}
				pagefactory.setReadingLocation(location);
				bookView.drawPage(null, location, true);
			}

		});
		contentLayout.addView(gotoView1);

	}

	private void makeGotoView2() {
		gotoView2 = getLayoutInflater().inflate(R.layout.option_menu_goto2,
				null);
		gotoLayout2 = (LinearLayout) gotoView2.findViewById(R.id.gotoView2);

		gotoLayout2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 什么都不做，防止页面消失。
			}
		});
		gotoLayout2.setVisibility(View.INVISIBLE);
		closeGotoButton2 = (ImageButton) gotoView2
				.findViewById(R.id.closeGoto2);
		closeGotoButton2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoLayout2.setVisibility(View.INVISIBLE);
				gotoInput.setText("");
			}
		});
		gotoInput = (EditText) gotoView2.findViewById(R.id.gotoInput);
		gotoInput.setInputType(InputType.TYPE_NULL);
		if (book.hasPageNumber()) {
			gotoInput.setHint(String.format(
					getResources().getString(R.string.goto_enter), 1,
					book.getTotalPage()));
		} else {
			gotoInput.setHint(R.string.goto_enter2);
		}
		gotoInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				Editable text = gotoInput.getText();
				if (null != text) {
					gotoInput.setSelection(text.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});
		goButton = (Button) gotoView2.findViewById(R.id.goto_go2);
		goButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					long location;
					if (book.hasPageNumber()) {// 有页码的格式
						location = Long.parseLong(gotoInput.getText()
								.toString().trim());
						if (location < 1) {
							location = 1;
						} else if (location > book.getTotalPage()) {
							location = book.getTotalPage();
						}
					} else {// TODO 显示百分比的格式，除TXT外的其他格式需要重新计算
						Double percentage = Double.parseDouble(gotoInput
								.getText().toString().trim());
						if (percentage < 0) {
							location = 0;
						} else if (percentage > 100) {
							location = book.getTotalPage();
						} else {
							double locationD = percentage * book.getTotalPage()
									/ 100;
							location = (long) locationD;
							if (location >= book.getTotalPage())
								location = book.getTotalPage() - 1;
						}
					}
					if ("PDF".equals(book.getFileType())) {// MUPDF页码从0开始计数
						location--;
					}
					pagefactory.setReadingLocation(location);
					bookView.drawPage(null, location, true);
				} catch (NumberFormatException e) {

				}
			}
		});
		gotoDelButton = (ImageButton) gotoView2.findViewById(R.id.goto_del);
		gotoDelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoInput.setText("");
			}
		});
		goto0 = (Button) gotoView2.findViewById(R.id.goto_0);
		goto0.setOnClickListener(new GotoNumberOnClickListener(0));
		goto1 = (Button) gotoView2.findViewById(R.id.goto_1);
		goto1.setOnClickListener(new GotoNumberOnClickListener(1));
		goto2 = (Button) gotoView2.findViewById(R.id.goto_2);
		goto2.setOnClickListener(new GotoNumberOnClickListener(2));
		goto3 = (Button) gotoView2.findViewById(R.id.goto_3);
		goto3.setOnClickListener(new GotoNumberOnClickListener(3));
		goto4 = (Button) gotoView2.findViewById(R.id.goto_4);
		goto4.setOnClickListener(new GotoNumberOnClickListener(4));
		goto5 = (Button) gotoView2.findViewById(R.id.goto_5);
		goto5.setOnClickListener(new GotoNumberOnClickListener(5));
		goto6 = (Button) gotoView2.findViewById(R.id.goto_6);
		goto6.setOnClickListener(new GotoNumberOnClickListener(6));
		goto7 = (Button) gotoView2.findViewById(R.id.goto_7);
		goto7.setOnClickListener(new GotoNumberOnClickListener(7));
		goto8 = (Button) gotoView2.findViewById(R.id.goto_8);
		goto8.setOnClickListener(new GotoNumberOnClickListener(8));
		goto9 = (Button) gotoView2.findViewById(R.id.goto_9);
		goto9.setOnClickListener(new GotoNumberOnClickListener(9));
		gotoDot = (Button) gotoView2.findViewById(R.id.goto_dot);
		if (book.hasPageNumber()) {
			gotoDot.setBackgroundResource(R.drawable.goto_button);
			gotoDot.setText(R.string.button_cancle);

			gotoDot.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoLayout2.setVisibility(View.INVISIBLE);
					gotoInput.setText("");
				}
			});
		} else {
			gotoDot.setBackgroundResource(R.drawable.goto_button);
			gotoDot.setText(".");
			gotoDot.setOnClickListener(new GotoNumberOnClickListener(-1));
		}

	}

	private void makeTextView() {
		textView = getLayoutInflater().inflate(R.layout.option_menu_text, null);
		style = getSharedPreferences(Constant.STYLE_REFERENCE, 0);
		editor = style.edit();
		textLayout = (LinearLayout) textView.findViewById(R.id.textView);
		textLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 什么都不做，防止页面消失。
			}
		});
		textLayout.setVisibility(View.INVISIBLE);
		closeTextButton = (ImageButton) textView.findViewById(R.id.closeText);
		closeTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				textLayout.setVisibility(View.INVISIBLE);
			}
		});
		textDefault = (ImageButton) textView.findViewById(R.id.text_default);
		textDefault.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setTextDefault();
			}

		});
		textSignA1 = (ImageButton) textView.findViewById(R.id.sign_a1);
		textImageA1 = (ImageButton) textView.findViewById(R.id.image_a1);
		textImageA1.setOnClickListener(new TextSizeOnClickListener(1));
		textSignA2 = (ImageButton) textView.findViewById(R.id.sign_a2);
		textImageA2 = (ImageButton) textView.findViewById(R.id.image_a2);
		textImageA2.setOnClickListener(new TextSizeOnClickListener(2));
		textSignA3 = (ImageButton) textView.findViewById(R.id.sign_a3);
		textImageA3 = (ImageButton) textView.findViewById(R.id.image_a3);
		textImageA3.setOnClickListener(new TextSizeOnClickListener(3));
		textSignA4 = (ImageButton) textView.findViewById(R.id.sign_a4);
		textImageA4 = (ImageButton) textView.findViewById(R.id.image_a4);
		textImageA4.setOnClickListener(new TextSizeOnClickListener(4));
		textSignA5 = (ImageButton) textView.findViewById(R.id.sign_a5);
		textImageA5 = (ImageButton) textView.findViewById(R.id.image_a5);
		textImageA5.setOnClickListener(new TextSizeOnClickListener(5));
		textSignA6 = (ImageButton) textView.findViewById(R.id.sign_a6);
		textImageA6 = (ImageButton) textView.findViewById(R.id.image_a6);
		textImageA6.setOnClickListener(new TextSizeOnClickListener(6));
		textSignA7 = (ImageButton) textView.findViewById(R.id.sign_a7);
		textImageA7 = (ImageButton) textView.findViewById(R.id.image_a7);
		textImageA7.setOnClickListener(new TextSizeOnClickListener(7));
		int defaultSize = style.getInt(DBSchema.COLUMN_STYLE_SIZE,
				Constant.STYLE_DEFAULT_SIZE);
		setTextSize(defaultSize);

		String typeface = style.getString(DBSchema.COLUMN_STYLE_TYPEFACE,
				Constant.STYLE_DEFAULT_TYPEFACE);
		typefaceMap = new TreeMap<String, Boolean>();
		for (String key : Constant.STYLE_TYPEFACE) {
			typefaceMap.put(key, key.equals(typeface));
		}
		textFontList = (ListView) textView.findViewById(R.id.text_font_list);
		fontListAdapter = new FontListAdapter(this, typefaceMap);
		textFontList.setAdapter(fontListAdapter);
		fontListAdapter.notifyDataSetChanged();

		textSignSpacing1 = (ImageButton) textView
				.findViewById(R.id.sign_spacing1);
		textImageSpacing1 = (ImageButton) textView
				.findViewById(R.id.image_spacing1);
		textImageSpacing1.setOnClickListener(new TextSpacingOnClickListener(1));
		textSignSpacing2 = (ImageButton) textView
				.findViewById(R.id.sign_spacing2);
		textImageSpacing2 = (ImageButton) textView
				.findViewById(R.id.image_spacing2);
		textImageSpacing2.setOnClickListener(new TextSpacingOnClickListener(2));
		textSignSpacing3 = (ImageButton) textView
				.findViewById(R.id.sign_spacing3);
		textImageSpacing3 = (ImageButton) textView
				.findViewById(R.id.image_spacing3);
		textImageSpacing3.setOnClickListener(new TextSpacingOnClickListener(3));
		int defaultSpacing = style.getInt(DBSchema.COLUMN_STYLE_LINE_SPACING,
				Constant.STYLE_DEFAULT_LINE_SPACING);
		setTextSpacing(defaultSpacing);

		textSignMargin1 = (ImageButton) textView
				.findViewById(R.id.sign_margin1);
		textImageMargin1 = (ImageButton) textView
				.findViewById(R.id.image_margin1);
		textImageMargin1.setOnClickListener(new TextMarginOnClickListener(1));
		textSignMargin2 = (ImageButton) textView
				.findViewById(R.id.sign_margin2);
		textImageMargin2 = (ImageButton) textView
				.findViewById(R.id.image_margin2);
		textImageMargin2.setOnClickListener(new TextMarginOnClickListener(2));
		textSignMargin3 = (ImageButton) textView
				.findViewById(R.id.sign_margin3);
		textImageMargin3 = (ImageButton) textView
				.findViewById(R.id.image_margin3);
		textImageMargin3.setOnClickListener(new TextMarginOnClickListener(3));
		int defaultMarginWidth = style.getInt(
				DBSchema.COLUMN_STYLE_MARGIN_WIDTH,
				Constant.STYLE_DEFAULT_MARGIN_WIDTH);
		int defaultMarginHeight = style.getInt(
				DBSchema.COLUMN_STYLE_MARGIN_HEIGHT,
				Constant.STYLE_DEFAULT_MARGIN_HEIGHT);
		setTextMargin(defaultMarginWidth, defaultMarginHeight);
	}

	/**
	 * 设置文本样式是否为系统默认配置的指示图标
	 */
	private void setTextDefaultImage() {
		int width = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_WIDTH,
				Constant.STYLE_DEFAULT_MARGIN_WIDTH);
		int size = style.getInt(DBSchema.COLUMN_STYLE_SIZE,
				Constant.STYLE_DEFAULT_SIZE);
		int spacing = style.getInt(DBSchema.COLUMN_STYLE_LINE_SPACING,
				Constant.STYLE_DEFAULT_LINE_SPACING);
		String typeface = style.getString(DBSchema.COLUMN_STYLE_TYPEFACE,
				Constant.STYLE_DEFAULT_TYPEFACE);
		if (width == Constant.STYLE_DEFAULT_MARGIN_WIDTH
				&& size == Constant.STYLE_DEFAULT_SIZE
				&& spacing == Constant.STYLE_DEFAULT_LINE_SPACING
				&& typeface.equals(Constant.STYLE_DEFAULT_TYPEFACE)) {
			textDefault.setBackgroundResource(R.drawable.text_default_selected);
		} else {
			textDefault
					.setBackgroundResource(R.drawable.text_default_unselected);
		}
	}

	/**
	 * 设置文本样式为系统默认配置
	 */
	private void setTextDefault() {
		setTextSize(Constant.STYLE_DEFAULT_SIZE);

		for (int i = 0; i < typefaceMap.keySet().size(); i++) {
			typefaceMap.put(Constant.STYLE_TYPEFACE[i],
					Constant.STYLE_TYPEFACE[i]
							.equals(Constant.STYLE_DEFAULT_TYPEFACE));
		}
		fontListAdapter.notifyDataSetChanged();
		editor.putString(DBSchema.COLUMN_STYLE_TYPEFACE,
				Constant.STYLE_DEFAULT_TYPEFACE);
		editor.commit();
		setTextSpacing(Constant.STYLE_DEFAULT_LINE_SPACING);
		setTextMargin(Constant.STYLE_DEFAULT_MARGIN_WIDTH,
				Constant.STYLE_DEFAULT_MARGIN_HEIGHT);
		pagefactory.setTextStyle();
		bookView.drawPage(null, pagefactory.getReadingLocation(), false);
	}

	/**
	 * 根据width和height设置边距。
	 * 
	 * @param margin
	 */
	private void setTextMargin(int width, int height) {
		textSignMargin1.setBackgroundDrawable(null);
		textImageMargin1
				.setBackgroundResource(R.drawable.text_margin1_unselected);
		textSignMargin2.setBackgroundDrawable(null);
		textImageMargin2
				.setBackgroundResource(R.drawable.text_margin2_unselected);
		textSignMargin3.setBackgroundDrawable(null);
		textImageMargin3
				.setBackgroundResource(R.drawable.text_margin3_unselected);
		switch (width) {
		case Constant.STYLE_MARGIN_WIDTH1:
			textSignMargin1.setBackgroundResource(R.drawable.text_selected2);
			textImageMargin1
					.setBackgroundResource(R.drawable.text_margin1_selected);
			break;
		case Constant.STYLE_MARGIN_WIDTH2:
			textSignMargin2.setBackgroundResource(R.drawable.text_selected2);
			textImageMargin2
					.setBackgroundResource(R.drawable.text_margin2_selected);
			break;
		case Constant.STYLE_MARGIN_WIDTH3:
			textSignMargin3.setBackgroundResource(R.drawable.text_selected2);
			textImageMargin3
					.setBackgroundResource(R.drawable.text_margin3_selected);
			break;
		}
		editor.putInt(DBSchema.COLUMN_STYLE_MARGIN_WIDTH, width);
		editor.putInt(DBSchema.COLUMN_STYLE_MARGIN_HEIGHT, height);
		editor.commit();
		setTextDefaultImage();
	}

	private class TextMarginOnClickListener implements OnClickListener {
		private int number;

		public TextMarginOnClickListener(int number) {
			this.number = number;
		}

		@Override
		public void onClick(View v) {
			int width = 0, height = 0;
			switch (number) {
			case 1:
				width = Constant.STYLE_MARGIN_WIDTH1;
				height = Constant.STYLE_MARGIN_HEIGHT1;
				break;
			case 2:
				width = Constant.STYLE_MARGIN_WIDTH2;
				height = Constant.STYLE_MARGIN_HEIGHT2;
				break;
			case 3:
				width = Constant.STYLE_MARGIN_WIDTH3;
				height = Constant.STYLE_MARGIN_HEIGHT3;
				break;
			}
			setTextMargin(width, height);
			pagefactory.setTextStyle();
			bookView.drawPage(null, pagefactory.getReadingLocation(), false);
		}

	}

	/**
	 * 根据spacing设置行距。
	 * 
	 * @param spacing
	 */
	private void setTextSpacing(int spacing) {
		textSignSpacing1.setBackgroundDrawable(null);
		textImageSpacing1
				.setBackgroundResource(R.drawable.text_spacing1_unselected);
		textSignSpacing2.setBackgroundDrawable(null);
		textImageSpacing2
				.setBackgroundResource(R.drawable.text_spacing2_unselected);
		textSignSpacing3.setBackgroundDrawable(null);
		textImageSpacing3
				.setBackgroundResource(R.drawable.text_spacing3_unselected);
		switch (spacing) {
		case Constant.STYLE_LINE_SPACING1:
			textSignSpacing1.setBackgroundResource(R.drawable.text_selected2);
			textImageSpacing1
					.setBackgroundResource(R.drawable.text_spacing1_selected);
			break;
		case Constant.STYLE_LINE_SPACING2:
			textSignSpacing2.setBackgroundResource(R.drawable.text_selected2);
			textImageSpacing2
					.setBackgroundResource(R.drawable.text_spacing2_selected);
			break;
		case Constant.STYLE_LINE_SPACING3:
			textSignSpacing3.setBackgroundResource(R.drawable.text_selected2);
			textImageSpacing3
					.setBackgroundResource(R.drawable.text_spacing3_selected);
			break;
		}
		editor.putInt(DBSchema.COLUMN_STYLE_LINE_SPACING, spacing);
		editor.commit();
		setTextDefaultImage();
	}

	private class TextSpacingOnClickListener implements OnClickListener {
		private int number;

		public TextSpacingOnClickListener(int number) {
			this.number = number;
		}

		@Override
		public void onClick(View v) {
			int spacing = 0;
			switch (number) {
			case 1:
				spacing = Constant.STYLE_LINE_SPACING1;
				break;
			case 2:
				spacing = Constant.STYLE_LINE_SPACING2;
				break;
			case 3:
				spacing = Constant.STYLE_LINE_SPACING3;
				break;
			}
			setTextSpacing(spacing);
			pagefactory.setTextStyle();
			bookView.drawPage(null, pagefactory.getReadingLocation(), false);
		}

	}

	private class FontListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private TreeMap<String, Boolean> map;

		public FontListAdapter(Context context, TreeMap<String, Boolean> map) {
			this.mInflater = LayoutInflater.from(context);
			this.map = map;
		}

		@Override
		public int getCount() {
			return Constant.STYLE_TYPEFACE.length;
		}

		@Override
		public Object getItem(int position) {
			return Constant.STYLE_TYPEFACE[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.option_text_font_item,
						null);
			}
			final ImageButton sign = (ImageButton) convertView
					.findViewById(R.id.sign);
			TextView text = (TextView) convertView.findViewById(R.id.text);
			if (map.get(Constant.STYLE_TYPEFACE[position])) {
				sign.setBackgroundResource(R.drawable.text_selected1);
			} else
				sign.setBackgroundDrawable(null);
			setTextDefaultImage();
			text.setText(Constant.STYLE_TYPEFACE_TITLE[position]);
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for (String key : map.keySet()) {
						map.put(key,
								key.equals(Constant.STYLE_TYPEFACE[position]));
					}
					sign.setBackgroundResource(R.drawable.text_selected1);
					editor.putString(DBSchema.COLUMN_STYLE_TYPEFACE,
							Constant.STYLE_TYPEFACE[position]);
					editor.commit();
					fontListAdapter.notifyDataSetChanged();
					pagefactory.setTextStyle();
					bookView.drawPage(null, pagefactory.getReadingLocation(),
							false);
				}
			});
			return convertView;
		}

	}

	private class TextSizeOnClickListener implements OnClickListener {
		private int number;

		private int size;

		public TextSizeOnClickListener(int number) {
			this.number = number;
		}

		@Override
		public void onClick(View v) {
			switch (number) {
			case 1:
				size = Constant.STYLE_SIZE1;
				break;
			case 2:
				size = Constant.STYLE_SIZE2;
				break;
			case 3:
				size = Constant.STYLE_SIZE3;
				break;
			case 4:
				size = Constant.STYLE_SIZE4;
				break;
			case 5:
				size = Constant.STYLE_SIZE5;
				break;
			case 6:
				size = Constant.STYLE_SIZE6;
				break;
			case 7:
				size = Constant.STYLE_SIZE7;
				break;
			}
			setTextSize(size);
			pagefactory.setTextStyle();
			bookView.drawPage(null, pagefactory.getReadingLocation(), false);
		}

	}

	/**
	 * 根据size设置字号。
	 * 
	 * @param size
	 */
	private void setTextSize(int size) {
		textSignA1.setBackgroundDrawable(null);
		textSignA2.setBackgroundDrawable(null);
		textSignA3.setBackgroundDrawable(null);
		textSignA4.setBackgroundDrawable(null);
		textSignA5.setBackgroundDrawable(null);
		textSignA6.setBackgroundDrawable(null);
		textSignA7.setBackgroundDrawable(null);
		switch (size) {
		case Constant.STYLE_SIZE1:
			textSignA1.setBackgroundResource(R.drawable.text_selected2);
			break;
		case Constant.STYLE_SIZE2:
			textSignA2.setBackgroundResource(R.drawable.text_selected2);
			break;
		case Constant.STYLE_SIZE3:
			textSignA3.setBackgroundResource(R.drawable.text_selected2);
			break;
		case Constant.STYLE_SIZE4:
			textSignA4.setBackgroundResource(R.drawable.text_selected2);
			break;
		case Constant.STYLE_SIZE5:
			textSignA5.setBackgroundResource(R.drawable.text_selected2);
			break;
		case Constant.STYLE_SIZE6:
			textSignA6.setBackgroundResource(R.drawable.text_selected2);
			break;
		case Constant.STYLE_SIZE7:
			textSignA7.setBackgroundResource(R.drawable.text_selected2);
			break;
		}
		editor.putInt(DBSchema.COLUMN_STYLE_SIZE, size);
		editor.commit();
		setTextDefaultImage();
	}

	private class GotoNumberOnClickListener implements OnClickListener {
		private int number;

		public GotoNumberOnClickListener(int number) {
			this.number = number;
		}

		@Override
		public void onClick(View v) {
			if (number == -1)
				gotoInput.setText(gotoInput.getText().toString() + ".");
			else
				gotoInput.setText(gotoInput.getText().toString() + number);

		}

	}

	/**
	 * 失去焦点时，保存数据库阅读记录和最后一次阅读时间。
	 */
	@Override
	protected void onPause() {
		super.onPause();

		book.setCurrentLocation(pagefactory.getReadingLocation());
		ContentValues values = new ContentValues();
		values.put(DBSchema.COLUMN_BOOK_CURRENT_LOCATION,
				book.getCurrentLocation());
		values.put(DBSchema.COLUMN_BOOK_LAST_READING_TIME, DateUtils
				.getGreenwichDate(null).getTime());
		getContentResolver().update(DBSchema.CONTENT_URI_BOOK, values,
				BaseColumns._ID + " = " + book.getId(), null);

	}

	/**
	 * 处理从弹出菜单返回的事件。
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (null != menuLayout) {// 横竖屏时需要判断
			menuLayout.setVisibility(View.INVISIBLE);
		}
		if (resultCode == CODE_CONTENT) {
			Bundle bundle = data.getExtras();
			book.setCurrentLocation(pagefactory.getReadingLocation());
			pagefactory.setReadingLocation(bundle.getLong("location"));
			bookView.drawPage(bundle.getLong("location"), false);
		}
		if (resultCode == CODE_FIND) {
			if (null == searchLayout) {
				makeSearchView();
				contentLayout.addView(searchView);
			}
			searchLayout.setVisibility(View.VISIBLE);
			Bundle bundle = data.getExtras();
			book.setCurrentLocation(pagefactory.getReadingLocation());
			long location = bundle.getLong("location");
			pagefactory.setReadingLocation(location);
			searchKeys = bundle.getLongArray("searchKey");
			searchPage = bundle.getInt("page");
			searchText = bundle.getString("searchText");
			inputSearch.setText(searchText);
			searchIndex = 0;
			while (location != searchKeys[searchIndex]) {// 当前显示的location位置
				searchIndex++;
			}
			bookView.drawPage(searchText, location, true);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 打开并将搜索结果传回FindActivity
	 * 
	 * @param returnResult
	 *            true为需要将结果传回FindActivity
	 * @param findNew
	 *            true为开始新的搜索
	 */
	private void startFindIntent(boolean returnResult, boolean findNew) {
		Intent intent = new Intent();
		intent.putExtra("bookId", book.getId());
		if (returnResult) {
			intent.putExtra("page", searchPage);
			intent.putExtra("searchText", searchText);
			if (findNew) {
				intent.putExtra("findNew", "true");
			}
		}
		intent.setClass(BaseBookActivity.this, FindActivity.class);
		this.startActivityForResult(intent, CODE_FIND);
	}

	class TimeThread extends Thread {
		private Thread currentThread;

		public TimeThread() {
			currentThread = Thread.currentThread();
		}

		public void stopThread() {
			currentThread = null;
		}

		@Override
		public void run() {
			if (null != currentThread) {
				timeView.setText(DateUtils.dateToString(new Date(), "hh:mm"));
				// Log.i("time", DateUtils.dateToString(new Date(), "hh:mm"));
				handler.postDelayed(this, 30000);
			}
		}
	}

	public void setOptionMenuInvisible() {
		if (null != menuLayout)
			menuLayout.setVisibility(View.INVISIBLE);
		if (null != gotoLayout1)
			gotoLayout1.setVisibility(View.INVISIBLE);
		if (null != gotoLayout2)
			gotoLayout2.setVisibility(View.INVISIBLE);
		if (null != textLayout)
			textLayout.setVisibility(View.INVISIBLE);
		if (null != searchLayout) {
			try {
				ZLAndroidLibrary library = getLibrary();
				if (null != library) {
					FBView fbTextView = (FBView) library.getCurrentView();
					if (null != fbTextView) {
						fbTextView.clearFindResults();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			searchLayout.setVisibility(View.INVISIBLE);
		}
	}

}
