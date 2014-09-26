
package com.yt.reader.activity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.utils.WifiUtils;

public class AboutConnActivity extends YTReaderActivity {

    private WifiUtils wifiUtils;

    private WifiInfo wifiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutconnect);
        wifiUtils = new WifiUtils(this);
        Bundle bundle = this.getIntent().getExtras();
        String sectype = bundle.getString("security");

        wifiInfo = wifiUtils.getWifiInfor();

        TextView aboutconssid = (TextView) findViewById(R.id.aboutconssid);

        aboutconssid.setText(this.getResources().getString(R.string.info) + " "
                + wifiInfo.getSSID());
        // 状态
        TextView statusV = (TextView) findViewById(R.id.status);
        // 网速
        TextView speedV = (TextView) findViewById(R.id.speed);
        // 信号
        TextView singnalV = (TextView) findViewById(R.id.singnal);

        // 加密方式
        TextView securityw = (TextView) findViewById(R.id.securityw);

        // ip地址
        TextView ipV = (TextView) findViewById(R.id.ip);

        statusV.setText(this.getResources().getString(R.string.connected));
        speedV.setText(String.valueOf(wifiInfo.getLinkSpeed()) + "M");
        int calSignal = wifiUtils.calculateSignalLevel(wifiInfo.getRssi(), 4);
        String str = null;
        if (calSignal <= 1) {
            str = this.getResources().getString(R.string.weak);
        } else {
            str = this.getResources().getString(R.string.good);
        }
        singnalV.setText(str);

        securityw.setText(sectype);
        ipV.setText(Formatter.formatIpAddress(wifiInfo.getIpAddress()));

        TextView forgetV = (TextView) findViewById(R.id.forget);

        forgetV.setOnClickListener(new ForGetOnClickListener(wifiInfo.getNetworkId()));

        ImageView connectback = (ImageView) findViewById(R.id.connectback);
        connectback.setOnClickListener(new ConnectBackOnClickListener());

        
    }

    private class ConnectBackOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            AboutConnActivity.this.finish();
            //Intent intent = new Intent(AboutConnActivity.this, WifiActivity.class);
            //AboutConnActivity.this.startActivity(intent);
        }

    }

    private class ForGetOnClickListener implements OnClickListener {

        private int netWorkId;

        public ForGetOnClickListener(int netWorkId) {
            this.netWorkId = netWorkId;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            wifiUtils.disConnectionWifi(netWorkId);
            Intent intent = new Intent(AboutConnActivity.this, WifiActivity.class);
            AboutConnActivity.this.startActivity(intent);
        }

    }

}
