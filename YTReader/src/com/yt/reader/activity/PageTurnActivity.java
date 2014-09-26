package com.yt.reader.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.utils.Constant;

public class PageTurnActivity extends YTReaderActivity {

	private ImageView pageTurnCurlSelect;

	private ImageView pageTurnNoSelect;

	private SharedPreferences pageTurnPre;

	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pageturn);
		// 获取返回图标
		ImageView back = (ImageView) findViewById(R.id.pageback);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PageTurnActivity.this.finish();
			}
		});

		// 卷曲翻页
		LinearLayout curlLayout = (LinearLayout) findViewById(R.id.pageTurnCurl);
		// 不翻页
		LinearLayout noLayout = (LinearLayout) findViewById(R.id.pageTurnNo);
		// 卷曲翻页选择图标
		pageTurnCurlSelect = (ImageView) findViewById(R.id.pageTurnCurlSelect);
		// 不翻页选择图标
		pageTurnNoSelect = (ImageView) findViewById(R.id.pageTurnNoSelect);

		pageTurnPre = getSharedPreferences(Constant.STYLE_REFERENCE, -1);
		String style = pageTurnPre.getString(Constant.COLUMN_PAGETURN,
				Constant.STYLE_DEFAULT_PAGETURN);
		if (Constant.STYLE_DEFAULT_PAGETURN.equals(style)) {
			pageTurnNoSelect.setVisibility(View.VISIBLE);
		} else {
			pageTurnCurlSelect.setVisibility(View.VISIBLE);
		}

		curlLayout.setOnClickListener(new PageTurnOnClickListener());
		noLayout.setOnClickListener(new PageTurnOnClickListener());

	}

	public class PageTurnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			editor = pageTurnPre.edit();
			switch (v.getId()) {
			case R.id.pageTurnCurl:
				pageTurnCurlSelect.setVisibility(View.VISIBLE);
				pageTurnNoSelect.setVisibility(View.GONE);
				editor.putString(Constant.COLUMN_PAGETURN,Constant.STYLE_PAGETURN_CURL);
				break;
			case R.id.pageTurnNo:
				pageTurnCurlSelect.setVisibility(View.GONE);
				pageTurnNoSelect.setVisibility(View.VISIBLE);
				editor.putString(Constant.COLUMN_PAGETURN, Constant.STYLE_DEFAULT_PAGETURN);
				break;
			}
			editor.commit();
			PageTurnActivity.this.finish();
		}

	}
}
