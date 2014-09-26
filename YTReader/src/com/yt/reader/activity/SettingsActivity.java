
package com.yt.reader.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yt.reader.MainActivity;
import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.database.DBSchema;
import com.yt.reader.database.InitDataBase;
import com.yt.reader.utils.Constant;

public class SettingsActivity extends YTReaderActivity {

    private AlertDialog.Builder recoveryFailed;

    private AlertDialog.Builder ad;

    private SharedPreferences style;// 字体样式的配置文件
    
    
    private InitDataBase cdb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题
        setContentView(R.layout.settings);
        cdb=new InitDataBase(this);
        // 产品信息
        LinearLayout proInfor = (LinearLayout) findViewById(R.id.proInfor);
        proInfor.setOnClickListener(new SettingOnClickListener());
        // 设置语言
        LinearLayout langSet = (LinearLayout) findViewById(R.id.langSet);
        langSet.setOnClickListener(new SettingOnClickListener());

        // 点击返回按钮
        ImageView setback = (ImageView) findViewById(R.id.setback);
        setback.setOnClickListener(new SettingOnClickListener());

        // 点击屏幕
        LinearLayout screenV = (LinearLayout) findViewById(R.id.screen);
        screenV.setOnClickListener(new SettingOnClickListener());
        
        //翻页效果
        LinearLayout pageTurn = (LinearLayout) findViewById(R.id.pageTurn);
        pageTurn.setOnClickListener(new SettingOnClickListener());

        // 点击网络
        LinearLayout network = (LinearLayout) findViewById(R.id.network);
        network.setOnClickListener(new SettingOnClickListener());

        // 点击日期和时间设置按钮
        LinearLayout dateAndTime = (LinearLayout) findViewById(R.id.dateAndTime);
        dateAndTime.setOnClickListener(new SettingOnClickListener());

        // 设置字体
        // LinearLayout fontsSet = (LinearLayout) findViewById(R.id.fontsSet);
        // fontsSet.setOnClickListener(new SettingOnClickListener());

        LinearLayout recovery = (LinearLayout) findViewById(R.id.recovery);
        recovery.setOnClickListener(new SettingOnClickListener());

        style = getSharedPreferences(Constant.STYLE_REFERENCE, 0);
        recoveryFailedDialog();
        openOptionsDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /***
     * 确定恢复出厂设置
     */
    private void openOptionsDialog() {
        ad = new AlertDialog.Builder(this);
        ad.setMessage(R.string.recoverySet);
        ad.setPositiveButton(R.string.determine, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                try {
                    //copyDataBase();
                    cdb.del();
                    cdb.updateBook();
                    recover();
                    initScreen();
                    finish();
                    Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                    SettingsActivity.this.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    recoveryFailed.show();
                }

            }
        });

        ad.setNegativeButton(R.string.cancel, null);

    }
    
    /***
     * 默认为竖屏
     */
    private void initScreen(){
        SharedPreferences isherVerPre = getSharedPreferences("herver", -1);
        Editor edit=isherVerPre.edit();
        edit.putString("isherver", "1");
        edit.commit();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /***
     * 恢复出厂设置成功
     */
    private void recoveryFailedDialog() {// 打开对话框
        recoveryFailed = new AlertDialog.Builder(this);
        recoveryFailed.setMessage(R.string.recoveryFailed);// 设置对话框内容
        recoveryFailed.setPositiveButton(R.string.determine, null);
        recoveryFailed.setNegativeButton(R.string.cancel, null);

    }

    public class SettingOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.proInfor:
                    Intent intent = new Intent(SettingsActivity.this, ProInforActivity.class);
                    SettingsActivity.this.startActivity(intent);

                    break;
                case R.id.langSet:
                    Intent langIntent = new Intent(SettingsActivity.this, LangSetActivity.class);
                    SettingsActivity.this.startActivity(langIntent);
                    break;
                case R.id.pageTurn:
                    Intent pageTurnIntent = new Intent(SettingsActivity.this, PageTurnActivity.class);
                    SettingsActivity.this.startActivity(pageTurnIntent);
                    break;
                case R.id.setback:
                    SettingsActivity.this.finish();
                    Intent backIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    SettingsActivity.this.startActivity(backIntent);
                    break;
                case R.id.screen:
                    SettingsActivity.this.finish();
                    Intent screenIntent = new Intent(SettingsActivity.this, ScreenActivity.class);
                    SettingsActivity.this.startActivity(screenIntent);
                    // SettingsActivity.this.onDestroy();
                    break;
                case R.id.network:
                    Intent netIntent = new Intent(SettingsActivity.this, WifiActivity.class);
                    SettingsActivity.this.startActivity(netIntent);
                    break;
                case R.id.dateAndTime:
                    Intent dateIntent = new Intent(SettingsActivity.this, DateAndTimeActivity.class);
                    SettingsActivity.this.startActivity(dateIntent);
                    break;
                // case R.id.fontsSet:
                // Intent fontsIntent = new Intent(SettingsActivity.this,
                // FontsActivity.class);
                // SettingsActivity.this.startActivity(fontsIntent);
                // break;
                case R.id.recovery:
                    ad.show();
                    break;

            }

        }

    }

    /***
     * 恢复语言 字体设置中的字体大小 等
     */
    private void recover() {
    	Editor editor;
        SharedPreferences langPre = getSharedPreferences("langpre", -1);
        editor = langPre.edit();
        editor.putString(Constant.COLUMN_LANG, Constant.STYLE_DEFAULT_LANG);
        editor.commit();
        
        SharedPreferences pageTurnPre = getSharedPreferences(Constant.STYLE_REFERENCE, -1);
        editor= pageTurnPre.edit();
        editor.putString(Constant.COLUMN_PAGETURN, Constant.STYLE_DEFAULT_PAGETURN);
        editor.commit();
        
        editor = style.edit();
        editor.putInt(DBSchema.COLUMN_STYLE_MARGIN_WIDTH, Constant.STYLE_DEFAULT_MARGIN_WIDTH);
        editor.putInt(DBSchema.COLUMN_STYLE_MARGIN_HEIGHT, Constant.STYLE_DEFAULT_MARGIN_HEIGHT);
        editor.putInt(DBSchema.COLUMN_STYLE_LINE_SPACING, Constant.STYLE_DEFAULT_LINE_SPACING);
        editor.putString(DBSchema.COLUMN_STYLE_TYPEFACE, Constant.STYLE_DEFAULT_TYPEFACE);
        editor.putInt(DBSchema.COLUMN_STYLE_SIZE, Constant.STYLE_DEFAULT_SIZE);
        editor.commit();
        // added by zjq 2012.11.14
        SharedPreferences outtimePre = getSharedPreferences("outtime", -1);// 屏保延迟的时间
        Editor ed = outtimePre.edit();
        ed.putString("time", "1");
        ed.commit();
        Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_OFF_TIMEOUT,  1 * 60 * 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
