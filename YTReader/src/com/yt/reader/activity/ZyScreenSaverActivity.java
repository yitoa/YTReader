
package com.yt.reader.activity;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageSwitcher;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;

public class ZyScreenSaverActivity extends YTReaderActivity {
    private WakeLock mWakeLock;

    private int horverstat;// 横竖屏状态，默认竖屏

    private ImageSwitcher saveScreenImage;

    private boolean bool = true;

    private boolean b_switch = false;

    private static int MSG_UPDATE = 1;

    private int cur_index = 0;

    private Thread saveScreenThread;

    private int time = 0;

    private KeyguardManager mKeyguardManager = null;

    private KeyguardLock mKeyguardLock = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.screensaver);
        saveScreenImage = (ImageSwitcher) findViewById(R.id.saveScreen);
        // 强制点亮屏幕
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, "SimpleTimer");
        mWakeLock.acquire();
        if (pm.isScreenOn()) {
            mWakeLock.release();
            mWakeLock = null;
        }

        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock("");
        mKeyguardLock.disableKeyguard();
        // 利用线程来更新 当前欲显示的图片id， 调用handler来选中当前图片
        saveScreenThread = new Thread(new Thread() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (bool) {
                    if (time > 20000) {// 20秒后就不在切换图片了
                        bool = false;
                        mKeyguardLock.reenableKeyguard();
                        ZyScreenSaverActivity.this.onStop();
                    }
                    Message msg = mhandler.obtainMessage(MSG_UPDATE, cur_index, 0);
                    mhandler.sendMessage(msg);

                    // 更新时间间隔为 2s
                    try {
                        Thread.sleep(2000);
                        b_switch = true;
                        time = time + 2000;
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (cur_index > 3) {
                        cur_index = 1;
                    } else {
                        cur_index++;// 放置在Thread.sleep(2000)
                    }
                }
            }
        });
        saveScreenThread.start();
        // 判断横竖屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            horverstat = 1;// 横屏为1
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            horverstat = 2;
        }
        Log.i("MyScreenSaver", "MyScreenSaver");
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }

    }

    private Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE) {
                try {
                    if (cur_index == 0 || cur_index == 1) {
                        saveScreenImage.setBackgroundResource(R.drawable.savescreen1);
                    } else if (cur_index == 2) {
                        saveScreenImage.setBackgroundResource(R.drawable.savescreen2);
                    } else {
                        saveScreenImage.setBackgroundResource(R.drawable.savescreen3);
                    }
                } catch (Error e) {
                    Log.i("MyScreenSaver", "Screen Pic switch error!");// 内存不足
                }
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDestroy();
                finish();// 任意键关闭屏保
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        if (b_switch) {
            bool = false; // 屏幕再次变暗，停止图片切换
            Log.i("MyScreenSaver", "End Switch");
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        bool = false;
        saveScreenImage.setBackgroundDrawable(null);
        super.onDestroy();

    }

}
