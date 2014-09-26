
package com.yt.reader.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;

public class ScreenActivity extends YTReaderActivity {

    private RadioButton horRadio;

    private RadioButton verRadio;

    private SharedPreferences outtimePre;// 屏保延迟的时间

    private TextView screentimeout;

    private boolean isShow = false;// 是否显示

    private LinearLayout timelist;// 时间列表

    private ImageView showtime;// 显示时间

    private ImageView oneMinSelect;// 1分钟

    private ImageView twoMinSelect;// 2分钟

    private ImageView fiveMinSelect;// 5分钟

    private ImageView fifthMinSelect;// 15分钟

    private ImageView hourSelect;// 1个小时
    
    private SharedPreferences isherVerPre; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen);
        // 横竖屏的切换
        RadioGroup horver = (RadioGroup) findViewById(R.id.horVer);
        // 横屏
        horRadio = (RadioButton) findViewById(R.id.horRadio);
        // 竖屏
        verRadio = (RadioButton) findViewById(R.id.verRadio);

        // 选择的时间
        screentimeout = (TextView) findViewById(R.id.screentimeout);

        // 显示时间
        showtime = (ImageView) findViewById(R.id.showtime);
        // 时间列表
        timelist = (LinearLayout) findViewById(R.id.timelist);

        LinearLayout oneMin = (LinearLayout) findViewById(R.id.oneMin);

        LinearLayout twoMin = (LinearLayout) findViewById(R.id.twoMin);

        LinearLayout fiveMin = (LinearLayout) findViewById(R.id.fiveMin);

        LinearLayout fifthMin = (LinearLayout) findViewById(R.id.fifthMin);

        LinearLayout hour = (LinearLayout) findViewById(R.id.onehour);

        ImageView screenback = (ImageView) findViewById(R.id.screenback);

        oneMinSelect = (ImageView) findViewById(R.id.oneMinSelect);

        twoMinSelect = (ImageView) findViewById(R.id.twoMinSelect);

        fiveMinSelect = (ImageView) findViewById(R.id.fiveMinSelect);

        fifthMinSelect = (ImageView) findViewById(R.id.fifthMinSelect);

        hourSelect = (ImageView) findViewById(R.id.onehourSelect);
        
        // 判断横竖屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            horRadio.setChecked(true);
            verRadio.setChecked(false);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            verRadio.setChecked(true);
            horRadio.setChecked(false);
        }
        // 获取Preferences中存放的数据
        outtimePre = getSharedPreferences("outtime", -1);
        
        isherVerPre= getSharedPreferences("herver", -1);
        String time = outtimePre.getString("time", null);
        if (time == null) {
            screentimeout.setText(this.getResources().getString(R.string.screenOut));
            oneMinSelect.setVisibility(View.VISIBLE);
            Editor ed = outtimePre.edit();
            int outtime = 60000;// 以毫秒为单位
            ed.putString("time", "1");
            outtime = 1 * 60 * 1000;
            ed.commit();
            // 修改settings中的SCREEN_OFF_TIMEOUT屏幕超时的值，下面所有的应用都，值为-1表示不休眠
            Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_OFF_TIMEOUT, outtime);
            screentimeout.setText(ScreenActivity.this.getResources().getString(
                    R.string.screenOut)
                    + " "
                    + 1
                    + ScreenActivity.this.getResources().getString(R.string.minutes));
        } else {
            screentimeout.setText(this.getResources().getString(R.string.screenOut) + " " + time
                    + this.getResources().getString(R.string.minutes));
            if ("1".equals(time)) {
                oneMinSelect.setVisibility(View.VISIBLE);
            } else if ("2".equals(time)) {
                twoMinSelect.setVisibility(View.VISIBLE);
            } else if ("5".equals(time)) {
                fiveMinSelect.setVisibility(View.VISIBLE);
            } else if ("15".equals(time)) {
                fifthMinSelect.setVisibility(View.VISIBLE);
            } else if ("60".equals(time)) {
                hourSelect.setVisibility(View.VISIBLE);
            }
        }

        horver.setOnCheckedChangeListener(new HorVerCheckChangeListener());
        showtime.setOnClickListener(new IsShowTimeOnClickListener());
        screenback.setOnClickListener(new ScreenBackOnClickListener());

        oneMin.setOnClickListener(new TimeListOnClickListener());
        twoMin.setOnClickListener(new TimeListOnClickListener());
        fiveMin.setOnClickListener(new TimeListOnClickListener());
        fifthMin.setOnClickListener(new TimeListOnClickListener());
        hour.setOnClickListener(new TimeListOnClickListener());

    }

    public class ScreenBackOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            ScreenActivity.this.finish();
            Intent intent = new Intent(ScreenActivity.this, SettingsActivity.class);
            ScreenActivity.this.startActivity(intent);

        }

    }

    public class TimeListOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Editor ed = outtimePre.edit();
            int outtime = 60000;// 以毫秒为单位
            switch (v.getId()) {
                case R.id.oneMin:
                    ed.putString("time", "1");
                    outtime = 1 * 60 * 1000;
                    oneMinSelect.setVisibility(View.VISIBLE);
                    twoMinSelect.setVisibility(View.GONE);
                    fiveMinSelect.setVisibility(View.GONE);
                    fifthMinSelect.setVisibility(View.GONE);
                    hourSelect.setVisibility(View.GONE);
                    screentimeout.setText(ScreenActivity.this.getResources().getString(
                            R.string.screenOut)
                            + " "
                            + 1
                            + ScreenActivity.this.getResources().getString(R.string.minutes));
                    break;
                case R.id.twoMin:
                    ed.putString("time", "2");
                    outtime = 2 * 60 * 1000;
                    oneMinSelect.setVisibility(View.GONE);
                    twoMinSelect.setVisibility(View.VISIBLE);
                    fiveMinSelect.setVisibility(View.GONE);
                    fifthMinSelect.setVisibility(View.GONE);
                    hourSelect.setVisibility(View.GONE);
                    screentimeout.setText(ScreenActivity.this.getResources().getString(
                            R.string.screenOut)
                            + " "
                            + 2
                            + ScreenActivity.this.getResources().getString(R.string.minutes));
                    break;
                case R.id.fiveMin:
                    ed.putString("time", "5");
                    outtime = 5 * 60 * 1000;
                    oneMinSelect.setVisibility(View.GONE);
                    twoMinSelect.setVisibility(View.GONE);
                    fiveMinSelect.setVisibility(View.VISIBLE);
                    fifthMinSelect.setVisibility(View.GONE);
                    hourSelect.setVisibility(View.GONE);
                    screentimeout.setText(ScreenActivity.this.getResources().getString(
                            R.string.screenOut)
                            + " "
                            + 5
                            + ScreenActivity.this.getResources().getString(R.string.minutes));
                    break;
                case R.id.fifthMin:
                    ed.putString("time", "15");
                    outtime = 15 * 60 * 1000;
                    oneMinSelect.setVisibility(View.GONE);
                    twoMinSelect.setVisibility(View.GONE);
                    fiveMinSelect.setVisibility(View.GONE);
                    fifthMinSelect.setVisibility(View.VISIBLE);
                    hourSelect.setVisibility(View.GONE);

                    screentimeout.setText(ScreenActivity.this.getResources().getString(
                            R.string.screenOut)
                            + " "
                            + 15
                            + ScreenActivity.this.getResources().getString(R.string.minutes));
                    break;
                case R.id.onehour:
                    ed.putString("time", "60");
                    outtime = 60 * 60 * 1000;
                    oneMinSelect.setVisibility(View.GONE);
                    twoMinSelect.setVisibility(View.GONE);
                    fiveMinSelect.setVisibility(View.GONE);
                    fifthMinSelect.setVisibility(View.GONE);
                    hourSelect.setVisibility(View.VISIBLE);
                    screentimeout
                            .setText(ScreenActivity.this.getResources().getString(
                                    R.string.screenOut)
                                    + " "
                                    + 1
                                    + ScreenActivity.this.getResources().getString(R.string.hour));
                    break;

            }
            ed.commit();
            // 修改settings中的SCREEN_OFF_TIMEOUT屏幕超时的值，下面所有的应用都，值为-1表示不休眠
            Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_OFF_TIMEOUT, outtime);
        }

    }

    public class IsShowTimeOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (isShow) {// 显示
                showtime.setImageResource(R.drawable.s_arror_d);
                timelist.setVisibility(View.VISIBLE);
                isShow = false;
            } else {
                showtime.setImageResource(R.drawable.s_arror_r);
                timelist.setVisibility(View.GONE);
                isShow = true;
            }

        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            horRadio.setChecked(true);
            verRadio.setChecked(false);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            verRadio.setChecked(true);
            horRadio.setChecked(false);
        }
    }

    private class HorVerCheckChangeListener implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            
            Editor ed = isherVerPre.edit();
            switch (checkedId) {
                case R.id.horRadio:// 横屏
                    ScreenActivity.this
                            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    ed.putString("isherver","2");
                    ed.commit();
                    break;
                case R.id.verRadio:// 竖屏
                    ScreenActivity.this
                            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    ed.putString("isherver", "1");
                    ed.commit();
                    break;

            }

        }

    }

}
