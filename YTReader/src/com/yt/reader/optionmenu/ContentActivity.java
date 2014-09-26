package com.yt.reader.optionmenu;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.yt.reader.BaseBookActivity;
import com.yt.reader.R;
import com.yt.reader.database.DBSchema;
import com.yt.reader.model.Bookmark;
import com.yt.reader.model.Chapter;
import com.yt.reader.utils.ParserUtils;

/**
 * 菜单的Content选项，包括书签和章节功能。
 * 
 * @author lsj
 */
public class ContentActivity extends BaseOptionActivity {
	private static int LINES = 10;// 竖屏时每页显示的行数

	private static final int BOOKMARK = 1;

	private static final int CHAPTER = 2;

	private static int bookmark_index = 1;

	private static int chapter_index = 1;

	private TabHost tabHost;

	private ListView chapterView, bookmarkView;

	private ContentAdapter bookmarkAdapter, chapterAdapter;

	private List<Bookmark> bookmarks;

	private List<Chapter> chapters;

	private ImageButton backButton;

	private Button clearAllButton;

	private long bookId;

	private long totalPage;// 书的总页数或总字节数

	private String filetype;

	private ImageButton bookmarkPreviousButton, bookmarkNextButton,
			chapterPreviousButton, chapterNextButton;

	private TextView bookmarkPageView, chapterPageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.option_menu_content);

		if (getWindowManager().getDefaultDisplay().getWidth() > getWindowManager()
				.getDefaultDisplay().getHeight()) {// 横屏
			LINES = 8;
		}

		Bundle bundle = this.getIntent().getExtras();
		if (null == bundle) {
			this.finish();
		} else {
			bookId = bundle.getLong("bookId");
			totalPage = bundle.getLong("totalPage");
			filetype = bundle.getString("filetype");
		}

		View chapterTab = (View) LayoutInflater.from(this).inflate(
				R.layout.tabmini, null);
		TextView text = (TextView) chapterTab.findViewById(R.id.tab_label);
		text.setText(R.string.content_chapter);

		View bookmarkTab = (View) LayoutInflater.from(this).inflate(
				R.layout.tabmini, null);
		text = (TextView) bookmarkTab.findViewById(R.id.tab_label);
		text.setText(R.string.content_bookmark);

		tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();

		tabHost.addTab(tabHost.newTabSpec("bookmarkTab")
				.setIndicator(bookmarkTab).setContent(R.id.bookmarkTab));
		tabHost.addTab(tabHost.newTabSpec("chapterTab")
				.setIndicator(chapterTab).setContent(R.id.chapterTab));

		chapterView = (ListView) findViewById(R.id.chapterlist);
		bookmarkView = (ListView) findViewById(R.id.bookmarklist);
		bookmarks = getBookmarks();
		chapters = getChapters();
		bookmarkAdapter = new ContentAdapter(this, bookmarks, BOOKMARK);
		bookmarkView.setAdapter(bookmarkAdapter);
		chapterAdapter = new ContentAdapter(this, chapters, CHAPTER);
		chapterView.setAdapter(chapterAdapter);
		backButton = (ImageButton) findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContentActivity.this.finish();
			}
		});

		clearAllButton = (Button) findViewById(R.id.clearAll);
		clearAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ContentActivity.this);
				builder.setMessage(R.string.content_clearconfirm)
						.setCancelable(false)
						.setPositiveButton(R.string.button_confirm,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										getContentResolver().delete(
												DBSchema.CONTENT_URI_BOOKMARK,
												DBSchema.COLUMN_BOOKMARK_BOOKID
														+ "=? ",
												new String[] { bookId + "" });
										bookmarks.clear();
										bookmarkAdapter.notifyDataSetChanged();
										changePageState(BOOKMARK);
										clearAllButton
												.setVisibility(View.INVISIBLE);
									}
								})
						.setNegativeButton(R.string.button_cancle,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				builder.create().show();
			}
		});

		if (bookmarks.isEmpty()) {
			clearAllButton.setVisibility(View.INVISIBLE);
		} else {
			clearAllButton.setVisibility(View.VISIBLE);
		}
		handlePage();

		final GestureDetector gestureDetector = new GestureDetector(
				new MyOnGestureListener());
		tabHost.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}

		});
		bookmarkView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}

		});
		chapterView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}

		});
	}

	/**
	 * 从数据库中检索该书的所有书签
	 * 
	 * @return
	 */
	private List<Bookmark> getBookmarks() {
		List<Bookmark> list = new ArrayList<Bookmark>();
		Cursor cursor = getContentResolver()
				.query(DBSchema.CONTENT_URI_BOOKMARK,
						new String[] { DBSchema.COLUMN_BOOKMARK_LOCATION,
								DBSchema.COLUMN_BOOKMARK_DESCRIPTION },
						DBSchema.COLUMN_BOOKMARK_BOOKID + "=? ",
						new String[] { bookId + "" },
						DBSchema.COLUMN_BOOKMARK_LOCATION);
		while (cursor.moveToNext()) {
			Bookmark bookmark = new Bookmark();
			bookmark.setBookId(bookId);
			bookmark.setLocation(cursor.getLong(cursor
					.getColumnIndex(DBSchema.COLUMN_BOOKMARK_LOCATION)));
			bookmark.setDescription(cursor.getString(cursor
					.getColumnIndex(DBSchema.COLUMN_BOOKMARK_DESCRIPTION)));
			list.add(bookmark);
		}
		cursor.close();
		return list;
	}

	/**
	 * 从数据库中检索该书的所有书签
	 * 
	 * @return
	 */
	private List<Chapter> getChapters() {
		List<Chapter> list = new ArrayList<Chapter>();
		Cursor cursor = getContentResolver().query(
				DBSchema.CONTENT_URI_CHAPTER,
				new String[] { DBSchema.COLUMN_CHAPTER_LOCATION,
						DBSchema.COLUMN_CHAPTER_TITLE },
				DBSchema.COLUMN_CHAPTER_BOOKID + "=? ",
				new String[] { bookId + "" }, DBSchema.COLUMN_CHAPTER_LOCATION);
		while (cursor.moveToNext()) {
			Chapter chapter = new Chapter();
			chapter.setBookId(bookId);
			chapter.setLocation(cursor.getLong(cursor
					.getColumnIndex(DBSchema.COLUMN_CHAPTER_LOCATION)));
			chapter.setTitle(cursor.getString(cursor
					.getColumnIndex(DBSchema.COLUMN_CHAPTER_TITLE)));
			list.add(chapter);
		}
		cursor.close();
		return list;
	}

	/**
	 * 处理与分页显示相关的代码
	 */
	private void handlePage() {
		bookmarkPreviousButton = (ImageButton) findViewById(R.id.bookmark_previous);
		bookmarkPreviousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPrevious(BOOKMARK);
			}
		});
		bookmarkPageView = (TextView) findViewById(R.id.bookmark_page);
		bookmarkNextButton = (ImageButton) findViewById(R.id.bookmark_next);
		bookmarkNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNext(BOOKMARK);
			}
		});
		changePageState(BOOKMARK);

		chapterPreviousButton = (ImageButton) findViewById(R.id.chapter_previous);
		chapterPreviousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPrevious(CHAPTER);
			}
		});
		chapterPageView = (TextView) findViewById(R.id.chapter_page);
		chapterNextButton = (ImageButton) findViewById(R.id.chapter_next);
		chapterNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNext(CHAPTER);
			}
		});
		changePageState(CHAPTER);
	}

	/**
	 * 更改翻页按钮及页码显示状态
	 * 
	 * @param method
	 */
	private void changePageState(int method) {
		if (method == BOOKMARK) {
			if (bookmarks.isEmpty()) {
				bookmarkPreviousButton.setVisibility(View.INVISIBLE);
				bookmarkNextButton.setVisibility(View.INVISIBLE);
				bookmarkPageView.setText(R.string.content_nobookmark);
				return;
			}
			bookmarkPreviousButton.setVisibility(View.VISIBLE);
			bookmarkNextButton.setVisibility(View.VISIBLE);
			if (bookmark_index == 1) {
				bookmarkPreviousButton
						.setBackgroundResource(R.drawable.arrow_1_d);
				bookmarkPreviousButton.setClickable(false);
			} else {
				bookmarkPreviousButton
						.setBackgroundResource(R.drawable.arrow_1);
				bookmarkPreviousButton.setClickable(true);
			}
			bookmarkPageView.setText(String.format(
					getResources().getString(R.string.content_page),
					bookmark_index,
					(bookmarks.size() % LINES == 0) ? bookmarks.size() / LINES
							: bookmarks.size() / LINES + 1));
			if (bookmarks.size() % LINES == 0
					&& bookmark_index == bookmarks.size() / LINES
					|| bookmarks.size() % LINES > 0
					&& bookmark_index == bookmarks.size() / LINES + 1) {
				bookmarkNextButton.setBackgroundResource(R.drawable.arrow_2_d);
				bookmarkNextButton.setClickable(false);
			} else {
				bookmarkNextButton.setBackgroundResource(R.drawable.arrow_2);
				bookmarkNextButton.setClickable(true);
			}
		} else if (method == CHAPTER) {
			if (chapters.isEmpty()) {
				chapterPreviousButton.setVisibility(View.INVISIBLE);
				chapterNextButton.setVisibility(View.INVISIBLE);
				chapterPageView.setText(R.string.content_nochapter);
				return;
			}
			chapterPreviousButton.setVisibility(View.VISIBLE);
			chapterNextButton.setVisibility(View.VISIBLE);
			if (chapter_index == 1) {
				chapterPreviousButton
						.setBackgroundResource(R.drawable.arrow_1_d);
				chapterPreviousButton.setClickable(false);
			} else {
				chapterPreviousButton.setBackgroundResource(R.drawable.arrow_1);
				chapterPreviousButton.setClickable(true);
			}
			chapterPageView.setText(String.format(
					getResources().getString(R.string.content_page),
					chapter_index,
					(chapters.size() % LINES == 0) ? chapters.size() / LINES
							: chapters.size() / LINES + 1));
			if (chapters.size() % LINES == 0
					&& chapter_index == chapters.size() / LINES
					|| chapters.size() % LINES > 0
					&& chapter_index == chapters.size() / LINES + 1) {
				chapterNextButton.setBackgroundResource(R.drawable.arrow_2_d);
				chapterNextButton.setClickable(false);
			} else {
				chapterNextButton.setBackgroundResource(R.drawable.arrow_2);
				chapterNextButton.setClickable(true);
			}
		}
	}

	/**
	 * 上一页
	 * 
	 * @param method
	 */
	private void onPrevious(int method) {
		if (method == BOOKMARK && bookmark_index > 1) {
			bookmark_index--;
			changePageState(method);
			bookmarkAdapter.notifyDataSetChanged();
		} else if (method == CHAPTER && chapter_index > 1) {
			chapter_index--;
			changePageState(method);
			chapterAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 下一页
	 * 
	 * @param method
	 */
	private void onNext(int method) {
		if (method == BOOKMARK) {
			int pages = bookmarks.size() % LINES == 0 ? bookmarks.size()
					/ LINES : bookmarks.size() / LINES + 1;
			if (bookmark_index >= pages)
				return;
			bookmark_index++;
			changePageState(method);
			bookmarkAdapter.notifyDataSetChanged();
		} else if (method == CHAPTER) {
			int pages = chapters.size() % LINES == 0 ? chapters.size() / LINES
					: chapters.size() / LINES + 1;
			if (chapter_index >= pages)
				return;
			chapter_index++;
			changePageState(method);
			chapterAdapter.notifyDataSetChanged();
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

			if ("bookmarkTab".equals(tabHost.getCurrentTabTag())) {
				if (velocityY < 0) {
					onNext(BOOKMARK);
				} else {
					onPrevious(BOOKMARK);
				}
			}
			if ("chapterTab".equals(tabHost.getCurrentTabTag())) {
				if (velocityY < 0) {
					onNext(CHAPTER);
				} else {
					onPrevious(CHAPTER);
				}
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

	private class ContentAdapter extends BaseAdapter {
		List<?> data;

		int method;

		private LayoutInflater mInflater;

		public ContentAdapter(Context context, List<?> data, int method) {
			this.data = data;
			this.method = method;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			int index = (method == BOOKMARK) ? bookmark_index : chapter_index;
			if (data.size() / LINES >= index)
				return LINES;
			return data.size() % LINES;
		}

		@Override
		public Object getItem(int arg0) {
			return data.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.option_menu_item, null);
			}
			long location = 0;
			final Intent intent = new Intent();
			TextView locationView = (TextView) convertView
					.findViewById(R.id.location);
			TextView decriptionView = (TextView) convertView
					.findViewById(R.id.decription);
			if (method == BOOKMARK) {
				Bookmark bookmark = (Bookmark) data.get(position
						+ (bookmark_index - 1) * LINES);
				location = bookmark.getLocation();
				if (filetype.equals("TXT") || filetype.equals("FB2")
						|| filetype.equals("HTM") || filetype.equals("HTML")
						|| filetype.equals("RTF") || filetype.equals("PDB")
						|| filetype.equals("DOC") || filetype.equals("EPUB")
						|| filetype.equals("MOBI")) {// 有些格式显示百分比
					locationView.setText(ParserUtils.getPercent(location,
							totalPage));
				} else if (filetype.equals("PDF")) {
					locationView.setText((location+1) + "");
				}  else {// 另一些格式直接显示页码
					locationView.setText(location + "");
				}
				if (bookmark.getDescription() != null) {
					decriptionView.setText(ParserUtils.trim(bookmark
							.getDescription()));
				}
			} else if (method == CHAPTER) {
				Chapter chapter = (Chapter) data.get(position
						+ (chapter_index - 1) * LINES);
				location = chapter.getLocation();
				if (filetype.equals("TXT") || filetype.equals("FB2")
						|| filetype.equals("HTM") || filetype.equals("HTML")
						|| filetype.equals("RTF") || filetype.equals("PDB")
						|| filetype.equals("DOC") || filetype.equals("EPUB")
						|| filetype.equals("MOBI")) {// 有些格式显示百分比
					locationView.setText(ParserUtils.getPercent(location,
							totalPage));
				} else if (filetype.equals("PDF")) {
					locationView.setText((location+1) + "");
				} else {// 另一些格式直接显示页码
					locationView.setText(location + "");
				}
				decriptionView.setText(ParserUtils.trim(chapter.getTitle()));
			}
			intent.putExtra("location", location);
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ContentActivity.this.setResult(
							BaseBookActivity.CODE_CONTENT, intent);
					ContentActivity.this.finish();
				}
			});
			return convertView;
		}

	}
}
