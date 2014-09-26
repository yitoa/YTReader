
package com.yt.reader.service;

import com.yt.reader.activity.ZyScreenSaverActivity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class ZyScreenService extends Service {
    
    @Override
    public IBinder onBind(Intent arg0) {     
        return null;
    }

    @Override
    public void onCreate() {        
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        BroadcastReceiver mMasterResetReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                try {
                    Intent i = new Intent();
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setClass(context, ZyScreenSaverActivity.class);
                    context.startActivity(i);
                    // finish();
                    Log.i("BroadcastReceiver", "BroadcastReceiver");

                } catch (Exception e) {
                    Log.i("Output:", e.toString());
                }
            }
        };
        registerReceiver(mMasterResetReciever, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

}
