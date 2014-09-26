
package com.yt.reader.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StatFs;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.utils.FileUtils;

public class ProInforActivity extends YTReaderActivity {

    private TextView batteryResult;

    private ProgressBar batteryProgress;

    private BatteryReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.proinfo);
        receiver = new BatteryReceiver();
        // 获取电量
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);// 注册BroadcastReceiver
        batteryResult = (TextView) findViewById(R.id.batteryResult);
        batteryProgress = (ProgressBar) findViewById(R.id.progress);

        // 获取sd卡存储进度
        ProgressBar sdStoreprogress = (ProgressBar) findViewById(R.id.sdStoreprogress);
        StatFs statfs = new StatFs(FileUtils.getSDPath().toString());
        long size = statfs.getBlockSize();// 每个block的大小
        long total = statfs.getBlockCount();// 总block的大小
        long available = statfs.getAvailableBlocks();// 可用block的大小
        int totalSize = (int) (total * size / 1024 / 1024);
        int availableSize = (int) (available * size / 1024 / 1024);
        sdStoreprogress.setMax(totalSize);
        sdStoreprogress.setProgress(totalSize - availableSize);
        TextView sdStoreResult = (TextView) findViewById(R.id.sdStoreResult);
        sdStoreResult.setText((100 - availableSize * 100 / totalSize) + "%");
        // sd卡的使用情况
        TextView sdUse = (TextView) findViewById(R.id.sdUse);
        sdUse.setText(availableSize + "MB(" + getResources().getString(R.string.available) + ")/"
                + totalSize + "MB");

        // 返回功能
        ImageView backV = (ImageView) findViewById(R.id.back);
        backV.setOnClickListener(new BackListener());

    }

    public class BackListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            ProInforActivity.this.finish();
        }

    }

    private class BatteryReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            int current = intent.getExtras().getInt("level");// 获得当前电量
            int total = intent.getExtras().getInt("scale");// 获得总电量
            int percent = current * 100 / total;
            batteryResult.setText(percent + "%");
            batteryProgress.setMax(total);
            batteryProgress.setProgress(current);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(receiver);
    }

}
