
package com.yt.reader.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.database.DBSchema;
import com.yt.reader.utils.Constant;

public class FontsActivity extends YTReaderActivity {

    private SharedPreferences fontsPre;// 字体

    private ImageView arial, simhei, calibri, timesNewRoman, verdana;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fonts);
        arial = (ImageView) findViewById(R.id.arialSelect);
        simhei = (ImageView) findViewById(R.id.simheiSelect);
        calibri = (ImageView) findViewById(R.id.calibriSelect);
        timesNewRoman = (ImageView) findViewById(R.id.timesNewRomanSelect);
        verdana = (ImageView) findViewById(R.id.verdanaSelect);

        // 获取Preferences中存放的数据
        fontsPre = getSharedPreferences(Constant.STYLE_REFERENCE, 0);
        // 默认为arial字体
        String fontStyle = fontsPre.getString(DBSchema.COLUMN_STYLE_TYPEFACE,
                Constant.STYLE_DEFAULT_TYPEFACE);
        if (fontStyle.equals("arial.ttf")) {
            arial.setVisibility(View.VISIBLE);
        } else if (fontStyle.equals("simhei.ttf")) {
            simhei.setVisibility(View.VISIBLE);
        } else if (fontStyle.equals("calibri.ttf")) {
            calibri.setVisibility(View.VISIBLE);
        } else if (fontStyle.equals("timesNewRoman.ttf")) {
            timesNewRoman.setVisibility(View.VISIBLE);
        } else if (fontStyle.equals("verdana.ttf")) {
            verdana.setVisibility(View.VISIBLE);
        }
        ImageView fontsback = (ImageView) findViewById(R.id.fontsback);

        fontsback.setOnClickListener(new FontsBackOnClickListener());

        LinearLayout arialStyle = (LinearLayout) findViewById(R.id.arial);
        LinearLayout simheiStyle = (LinearLayout) findViewById(R.id.simhei);
        LinearLayout calibriStyle = (LinearLayout) findViewById(R.id.calibri);
        LinearLayout timesNewRomanStyle = (LinearLayout) findViewById(R.id.timesNewRoman);
        LinearLayout verdanaStyle = (LinearLayout) findViewById(R.id.verdana);
        arialStyle.setOnClickListener(new FontsStyleOnClickListener());
        simheiStyle.setOnClickListener(new FontsStyleOnClickListener());
        calibriStyle.setOnClickListener(new FontsStyleOnClickListener());
        timesNewRomanStyle.setOnClickListener(new FontsStyleOnClickListener());
        verdanaStyle.setOnClickListener(new FontsStyleOnClickListener());

    }

    public class FontsBackOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            FontsActivity.this.finish();
            Intent intent = new Intent(FontsActivity.this, SettingsActivity.class);
            FontsActivity.this.startActivity(intent);
        }

    }

    public class FontsStyleOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            arial.setVisibility(View.INVISIBLE);
            simhei.setVisibility(View.INVISIBLE);
            calibri.setVisibility(View.INVISIBLE);
            timesNewRoman.setVisibility(View.INVISIBLE);
            verdana.setVisibility(View.INVISIBLE);
            Editor ed = fontsPre.edit();
            switch (v.getId()) {
                case R.id.arial:
                    ed.putString(DBSchema.COLUMN_STYLE_TYPEFACE, "arial.ttf");
                    arial.setVisibility(View.VISIBLE);
                    break;
                case R.id.simhei:
                    ed.putString(DBSchema.COLUMN_STYLE_TYPEFACE, "simhei.ttf");
                    simhei.setVisibility(View.VISIBLE);
                    break;
                case R.id.calibri:
                    ed.putString(DBSchema.COLUMN_STYLE_TYPEFACE, "calibri.ttf");
                    calibri.setVisibility(View.VISIBLE);
                    break;
                case R.id.timesNewRoman:
                    ed.putString(DBSchema.COLUMN_STYLE_TYPEFACE, "timesNewRoman.ttf");
                    timesNewRoman.setVisibility(View.VISIBLE);
                    break;
                case R.id.verdana:
                    ed.putString(DBSchema.COLUMN_STYLE_TYPEFACE, "verdana.ttf");
                    verdana.setVisibility(View.VISIBLE);
                    break;
            }
            ed.commit();

        }

    }

}
