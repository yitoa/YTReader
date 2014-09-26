package com.yt.reader.optionmenu;

import java.io.File;
import java.util.ArrayList;

import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yt.reader.BaseBookActivity;
import com.yt.reader.R;
import com.yt.reader.database.DBSchema;
import com.yt.reader.format.cool.CoolPageFactory;
import com.yt.reader.format.pdf.MuPDFCore;
import com.yt.reader.format.pdf.SearchItem;
import com.yt.reader.model.Book;
import com.yt.reader.utils.FileUtils;
import com.yt.reader.utils.ParserUtils;

/**
 * 菜单的Find选项，用于全文查找。
 * 
 * @author lsj
 */
public class FindActivity extends BaseOptionActivity {
	private static int LINES = 10;// 竖屏时每页显示的行数

	private static int find_index = 1;// 翻页页码

	private String searchText;// 待搜索文本

	private long bookId;

	private Book book;

	private ImageButton backButton;

	private Button searchButton;

	private ImageView searchdel;// 用于一键删除输入框中所有字符

	private EditText inputSearch;// 搜索输入框

	private ImageButton findPreviousButton, findNextButton;

	private TextView findPageView;

	private FindAdapter findAdapter;

	private ListView searchView;

	private static final int PROGRESS_DIALOG = 0;

	private ProgressThread progressThread;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.option_menu_find);

		if (getWindowManager().getDefaultDisplay().getWidth() > getWindowManager()
				.getDefaultDisplay().getHeight()) {// 横屏
			LINES = 8;
		}

		Bundle bundle = this.getIntent().getExtras();
		if (null == bundle) {
			this.finish();
		} else {
			inputSearch = (EditText) findViewById(R.id.inputSearch);
			searchdel = (ImageView) findViewById(R.id.searchdel);
			searchText = bundle.getString("searchText");
			if (null != searchText) {
				find_index = bundle.getInt("page");
				inputSearch.setText(searchText);
				searchdel.setVisibility(View.VISIBLE);
			} else {
				TextSearch.searchResultLocation = new ArrayList<Long>();
				TextSearch.searchResultDescription = new ArrayList<String>();
				TextSearch.searchResultRect=new ArrayList<SearchItem>();
			}
			bookId = bundle.getLong("bookId");
			Uri uri = Uri.parse("content://" + DBSchema.AUTHORITY + "/"
					+ DBSchema.TABLE_BOOK + "/#" + bookId);
			Cursor cursor = this.getContentResolver().query(
					uri,
					new String[] { DBSchema.COLUMN_BOOK_NAME,
							DBSchema.COLUMN_BOOK_PATH,
							DBSchema.COLUMN_BOOK_SIZE,
							DBSchema.COLUMN_BOOK_TOTAL_PAGE,
							DBSchema.COLUMN_BOOK_FILETYPE }, "_id=?",
					new String[] { bookId + "" }, null);
			if (cursor.moveToFirst()) {
				book = new Book();
				book.setPath(cursor.getString(cursor
						.getColumnIndex(DBSchema.COLUMN_BOOK_PATH)));
				book.setName(cursor.getString(cursor
						.getColumnIndex(DBSchema.COLUMN_BOOK_NAME)));
				book.setSize(cursor.getString(cursor
						.getColumnIndex(DBSchema.COLUMN_BOOK_SIZE)));
				if (null == book.getSize()) {
					book.setSize(FileUtils.getFilesize(new File(book.getPath()
							+ "/" + book.getName())));
				}
				book.setTotalPage(cursor.getLong(cursor
						.getColumnIndex(DBSchema.COLUMN_BOOK_TOTAL_PAGE)));
				book.setFileType(cursor.getString(cursor
						.getColumnIndex(DBSchema.COLUMN_BOOK_FILETYPE)));
				if (null == book.getFileType()) {
					book.setFileType(FileUtils.getFileType(book.getName()));
				}
			}

		}
		findAdapter = new FindAdapter(this);
		searchView = (ListView) findViewById(R.id.searchlist);
		searchView.setAdapter(findAdapter);
		final GestureDetector gestureDetector = new GestureDetector(
				new MyOnGestureListener());
		searchView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}

		});
		backButton = (ImageButton) findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FindActivity.this.finish();
			}
		});

		searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				find_index = 1;
				if (null != FindActivity.this.getCurrentFocus())
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(FindActivity.this
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);// 隐藏软键盘
				searchText = ParserUtils.trim(inputSearch.getText().toString());
				if (null != searchText && searchText.length() > 0) {
					showDialog(PROGRESS_DIALOG);
				}
			}
		});
		searchdel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				inputSearch.setText(null);
				searchdel.setVisibility(View.INVISIBLE);
			}
		});

		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (null == s || s.length() == 0) {
					searchdel.setVisibility(View.INVISIBLE);
					searchButton.setClickable(false);
				} else {
					searchdel.setVisibility(View.VISIBLE);
					searchButton.setClickable(true);
				}
			}
		});
		findPreviousButton = (ImageButton) findViewById(R.id.find_previous);
		findPreviousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPrevious();
			}
		});
		findPageView = (TextView) findViewById(R.id.find_page);
		findNextButton = (ImageButton) findViewById(R.id.find_next);
		findNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNext();
			}
		});

		if (null != bundle.getString("findNew")) {
			inputSearch.setText(null);
		}
		changePageState(true);
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(FindActivity.this);
			progressDialog.setMessage(getString(R.string.find_progress));
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
				progressThread.setState(ProgressThread.STATE_DONE);
				// Log.v("searchResult", "test 2");
				findAdapter.notifyDataSetChanged();
				changePageState(false);
			}
		}
	};

	private class ProgressThread extends Thread {
		Handler mHandler;

		final static int STATE_DONE = 0;

		final static int STATE_RUNNING = 1;

		int mState;

		ProgressThread(Handler h) {
			mHandler = h;
		}

		public void run() {
			mState = STATE_RUNNING;
			if (mState == STATE_RUNNING) {
				search(searchText);
				Message msg = mHandler.obtainMessage();
				msg.arg1 = 100;
				mHandler.sendMessage(msg);
			}
		}

		/*
		 * sets the current state for the thread, used to stop the thread
		 */
		public void setState(int state) {
			mState = state;
		}
	}

	/**
	 * 搜索的主入口
	 * 
	 * @param text
	 *            待搜索文本
	 */
	private void search(String text) {
		TextSearch.searchResultLocation.clear();
		TextSearch.searchResultDescription.clear();
		TextSearch.searchResultRect.clear();
		if (book.getFileType().equals("TXT")) {
			TextSearch.search(book, text);
		} else if (book.getFileType().equals("PDF")) {// TODO
			MuPDFCore.openFile(book.getPath() + "/" + book.getName());
			SearchItem[] items = MuPDFCore.search(text);
			if (null != items) {
				Log.v("searchResult", "result numbers: " + items.length);
				for (SearchItem item : items) {
					if (null == item)// 从底层传来的为固定数组，内容可能为空。
						break;
					Log.v("searchResult", item.toString());
					TextSearch.searchResultLocation.add((long) item.getPage());
					TextSearch.searchResultDescription.add(item
							.getDescription());
					TextSearch.searchResultRect.add(item);
				}
			} else {
				Log.v("searchResult", "search items is null");
			}
		} else if (book.getFileType().equals("FB2")
				|| book.getFileType().equals("EPUB")
				|| book.getFileType().equals("HTM")
				|| book.getFileType().equals("HTML")
				|| book.getFileType().equals("RTF")
				|| book.getFileType().equals("MOBI")) {
			try {
				FBView fbTextView = (FBView) getLibrary().getCurrentView();
				fbTextView.search(text, true, false, false, false);
				fbTextView.getModel().getLocations(); // 获取位置与文本信息
			} catch (Error e) {
				System.out.println("fb_search_out_of_memery");
			}
		} else if (book.getFileType().equals("PDB")
				|| book.getFileType().equals("DOC")) {
			try {
				CoolPageFactory.search(text);
			} catch (Error e) {
				System.out.println("cool_search_out_of_memery");
			}
		} else {// TODO 其他格式自行添加

		}
	}

	// fbreader获取ZLAndroidLibrary
	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
	}

	/**
	 * 更改翻页按钮及页码显示状态
	 * 
	 * @param isInitial
	 *            第一次加载该页面
	 */
	private void changePageState(boolean isInitial) {

		if (TextSearch.searchResultLocation.isEmpty()) {
			findPreviousButton.setVisibility(View.INVISIBLE);
			findNextButton.setVisibility(View.INVISIBLE);
			if (!isInitial)
				findPageView.setText(R.string.find_noresult);
			else
				findPageView.setText("");
			return;
		}
		findPreviousButton.setVisibility(View.VISIBLE);
		findNextButton.setVisibility(View.VISIBLE);
		if (find_index == 1) {
			findPreviousButton.setBackgroundResource(R.drawable.arrow_1_d);
			findPreviousButton.setClickable(false);
		} else {
			findPreviousButton.setBackgroundResource(R.drawable.arrow_1);
			findPreviousButton.setClickable(true);
		}
		findPageView
				.setText(String.format(
						getResources().getString(R.string.content_page),
						find_index,
						(TextSearch.searchResultLocation.size() % LINES == 0) ? TextSearch.searchResultLocation
								.size() / LINES
								: TextSearch.searchResultLocation.size()
										/ LINES + 1));
		if (TextSearch.searchResultLocation.size() % LINES == 0
				&& find_index == TextSearch.searchResultLocation.size() / LINES
				|| TextSearch.searchResultLocation.size() % LINES > 0
				&& find_index == TextSearch.searchResultLocation.size() / LINES
						+ 1) {
			findNextButton.setBackgroundResource(R.drawable.arrow_2_d);
			findNextButton.setClickable(false);
		} else {
			findNextButton.setBackgroundResource(R.drawable.arrow_2);
			findNextButton.setClickable(true);
		}
	}

	/**
	 * 上一页
	 * 
	 * @param method
	 */
	private void onPrevious() {
		if (find_index <= 1)
			return;
		find_index--;
		changePageState(false);
		findAdapter.notifyDataSetChanged();
	}

	/**
	 * 下一页
	 * 
	 * @param method
	 */
	private void onNext() {
		int pages = TextSearch.searchResultLocation.size() % LINES == 0 ? TextSearch.searchResultLocation
				.size() / LINES
				: TextSearch.searchResultLocation.size() / LINES + 1;
		if (find_index >= pages)
			return;
		find_index++;
		changePageState(false);
		findAdapter.notifyDataSetChanged();
	}

	private static class ViewHolder {
		public TextView locationView;

		public TextView decriptionView;
	}

	private class FindAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public FindAdapter(Context context) {
			Log.v("findAdapter", "FindAdapter()");
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// Log.v("findAdapter","getCount()");
			if (TextSearch.searchResultLocation.size() / LINES >= find_index)
				return LINES;
			return TextSearch.searchResultLocation.size() % LINES;
		}

		@Override
		public Object getItem(int arg0) {
			// Log.v("findAdapter","getItem()");
			return TextSearch.searchResultLocation.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			// Log.v("findAdapter","getItemId()");
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.v("findAdapter", "getView()");
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater
						.inflate(R.layout.option_menu_item, null);
				holder.locationView = (TextView) convertView
						.findViewById(R.id.location);
				holder.decriptionView = (TextView) convertView
						.findViewById(R.id.decription);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			long[] data = new long[TextSearch.searchResultLocation.size()];
			int i = 0;
			for (long key : TextSearch.searchResultLocation) {
				data[i++] = key;
			}
			long location = 0;
			final Intent intent = new Intent();
			location = data[position + (find_index - 1) * LINES];
			if (book.getFileType().equals("TXT")
					|| book.getFileType().equals("PDB")
					|| book.getFileType().equals("DOC")
					|| book.getFileType().equals("FB2")
					|| book.getFileType().equals("EPUB")
					|| book.getFileType().equals("HTM")
					|| book.getFileType().equals("HTML")
					|| book.getFileType().equals("RTF")
					|| book.getFileType().equals("MOBI")) {// 有些格式显示百分比
				holder.locationView.setText(ParserUtils.getPercent(location,
						book.getTotalPage()));
			} else if (book.getFileType().equals("PDF")) {
				holder.locationView.setText((location + 1) + "");
			} else {// 另一些格式直接显示页码
				holder.locationView.setText(location + "");
			}

			String descrption = TextSearch.searchResultDescription.get(position
					+ (find_index - 1) * LINES);
			SpannableStringBuilder style = new SpannableStringBuilder(
					descrption);
			int start, loc = 0;
			String str = descrption.toLowerCase();
			while (-1 < (start = str.indexOf(searchText.toLowerCase()))) {// 高亮匹配的字符串
				style.setSpan(new StyleSpan(Typeface.BOLD), loc + start, loc
						+ start + searchText.length(),
						Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				Log.v("searchResult", "highlight from " + (loc + start)
						+ " to " + (loc + start + searchText.length()));
				str = str.substring(start + searchText.length());
				loc = descrption.length() - str.length();
			}
			holder.decriptionView.setText(style);
			intent.putExtra("location", location);
			intent.putExtra("searchKey", data);
			intent.putExtra("searchText", searchText);
			intent.putExtra("page", find_index);
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FindActivity.this.setResult(BaseBookActivity.CODE_FIND,
							intent);
					FindActivity.this.finish();
				}
			});
			return convertView;
		}
	}

	private class MyOnGestureListener implements
			GestureDetector.OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (velocityY < 0) {
				onNext();
			} else {
				onPrevious();
			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

	}

}
