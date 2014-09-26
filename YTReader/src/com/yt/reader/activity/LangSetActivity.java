
package com.yt.reader.activity;

import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;

public class LangSetActivity extends YTReaderActivity {

    private Configuration config;

    private DisplayMetrics dm;

    private ImageView zhSelect;

    private ImageView enSelect;

    private SharedPreferences langPre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.langset);
        // 获取返回图标
        ImageView langback = (ImageView) findViewById(R.id.langback);
        langback.setOnClickListener(new LangSetOnClickListener());

        // 中文
        LinearLayout zhl = (LinearLayout) findViewById(R.id.zh);
        // 英文
        LinearLayout enl = (LinearLayout) findViewById(R.id.en);
        // 中文选择图标
        zhSelect = (ImageView) findViewById(R.id.zhSelect);
        // 英文选择图标
        enSelect = (ImageView) findViewById(R.id.enSelect);

        langPre = getSharedPreferences("langpre", -1);
        // 获取Preferences中存放的数据
        String able = getResources().getConfiguration().locale.getCountry();
        if ("US".equals(able)) {
            enSelect.setVisibility(View.VISIBLE);
        } else if ("CN".equals(able)) {
            zhSelect.setVisibility(View.VISIBLE);
        }
        config = getResources().getConfiguration();
        dm = getResources().getDisplayMetrics();

        zhl.setOnClickListener(new SelectLangOnClickListener());
        enl.setOnClickListener(new SelectLangOnClickListener());

        // langRadio.setOnCheckedChangeListener(new
        // LangCheckedChangeListener());

    }

    public class SelectLangOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent=new Intent(LangSetActivity.this,SettingsActivity.class);
            Editor ed = langPre.edit();
            switch (v.getId()) {
                case R.id.zh:
                    config.locale = Locale.SIMPLIFIED_CHINESE;
                    zhSelect.setVisibility(View.VISIBLE);
                    enSelect.setVisibility(View.GONE);
                    ed.putString("lang", "CN");
                    ed.commit();
                    getResources().updateConfiguration(config, dm);
                    LangSetActivity.this.finish();
                    LangSetActivity.this.startActivity(intent);
                    break;
                case R.id.en:
                    config.locale = Locale.US;
                    zhSelect.setVisibility(View.GONE);
                    enSelect.setVisibility(View.VISIBLE);
                    ed.putString("lang", "US");
                    ed.commit();
                    getResources().updateConfiguration(config, dm);
                    LangSetActivity.this.finish();
                    LangSetActivity.this.startActivity(intent);
                    break;
            }

        }

    }

    public class LangSetOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            LangSetActivity.this.finish();
            /*
             * Intent intent = new Intent(LangSetActivity.this,
             * SettingsActivity.class);
             * LangSetActivity.this.startActivity(intent);
             */
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

}
