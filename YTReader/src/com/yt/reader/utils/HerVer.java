
package com.yt.reader.utils;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

public class HerVer {

    private Activity object;

    public HerVer(Activity activity) {

        this.object = activity;
    }

    /***
     * 获取是横屏还是竖屏
     * 
     * @return
     */
    public void setHerOrVer() {
        String state = null;
        // 获取Preferences中存放的数据
        SharedPreferences isherVerPre = object.getSharedPreferences("herver", -1);
        state = isherVerPre.getString("isherver", "1");
     // 判断横竖屏
        if ("2".equals(state)) {// 横屏
            object.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if ("1".equals(state)) {// 竖屏
            object.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        SharedPreferences langPre = object.getSharedPreferences("langpre", -1);
        String lang = langPre.getString("lang", "CN");
        Configuration config = object.getResources().getConfiguration();
        DisplayMetrics dm = object.getResources().getDisplayMetrics();
        if ("US".equals(lang)) {
            config.locale = Locale.US;
        } else if ("CN".equals(lang)) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        }
        object.getResources().updateConfiguration(config, dm);
    }

}
