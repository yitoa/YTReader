
package com.yt.reader.activity;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.database.CurdDataBase;
import com.yt.reader.model.Wifi;
import com.yt.reader.utils.WifiUtils;
import com.yt.reader.utils.WifiUtils.WifiCipherType;

public class WifiConnectActivity extends YTReaderActivity {

    private EditText wifipassword;

    private TextView showpwd;

    private WifiUtils wifiUtils;

    private Wifi wifi;

    private CurdDataBase cdb;// 数据库操作对象

    private WifiInfo wifiInfo;

    private TextView connfails;

    private Button connectT;

    private String password;

    private String securetype;

    private String ssid;

    private ProgressDialog progressDialog;

    private static final int PROGRESS_DIALOG = 0;

    private ProgressThread progressThread;

    private int ljtype;// 连接方式 0:表示没有密码的需要输入密码 1：有密码的直接连接

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        wifi = (Wifi) getIntent().getSerializableExtra("wifi");
        wifiUtils = new WifiUtils(this);
        wifiInfo = wifiUtils.getWifiInfor();
        cdb = new CurdDataBase(this);
        if (wifi.getPassword() == null) {
            ljtype = 0;
            setContentView(R.layout.connectnetwork);
            // 获取传来的wifi
            TextView connssid = (TextView) findViewById(R.id.conssid);
            connssid.setText(this.getResources().getString(R.string.conssid) + " " + wifi.getSsid());
            // 信号强度
            TextView signstr = (TextView) findViewById(R.id.signstr);
            String signsize = null;
            if (wifi.getSignStr() < 0) {
                signsize = this.getResources().getString(R.string.no);
            } else if (wifi.getSignStr() >= 0 && wifi.getSignStr() <= 1) {
                signsize = this.getResources().getString(R.string.weak);
            } else {
                signsize = this.getResources().getString(R.string.good);
            }
            signstr.setText(this.getResources().getString(R.string.signstr) + ": " + signsize);
            // 加密方式
            TextView securityT = (TextView) findViewById(R.id.security);
            securityT.setText(this.getResources().getString(R.string.security) + ": "
                    + wifi.getSectype());
            // 输入的密码
            wifipassword = (EditText) findViewById(R.id.wifipassword);
            wifipassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            // 是否显示密码
            CheckBox isshowwifipwd = (CheckBox) findViewById(R.id.isshowwifipwd);
            isshowwifipwd.setOnCheckedChangeListener(listener);
            showpwd = (TextView) findViewById(R.id.showpwd);
            // 连接网络
            connectT = (Button) findViewById(R.id.connect);
            ssid = wifi.getSsid();
            securetype = wifi.getSectype();
            connectT.setOnClickListener(new ConnectOnClickListener());
            connfails = (TextView) findViewById(R.id.connfails);
            ImageView wifiConnback = (ImageView) findViewById(R.id.wifiConnback);
            wifiConnback.setOnClickListener(new WifiConnBack());
        } else {
            // 直接连wifi
            ljtype = 1;
            password = wifi.getPassword();
            securetype = wifi.getSectype();
            ssid = wifi.getSsid();
            showDialog(PROGRESS_DIALOG);
        }

    }

    public class WifiConnBack implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            WifiConnectActivity.this.finish();
            Intent intent = new Intent(WifiConnectActivity.this, WifiActivity.class);
            WifiConnectActivity.this.startActivity(intent);
        }

    }

    private class ConnectOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            password = wifipassword.getText().toString();
            showDialog(PROGRESS_DIALOG);
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                progressDialog = new ProgressDialog(WifiConnectActivity.this);
                progressDialog.setMessage(getString(R.string.connecting));
                return progressDialog;
            default:
                return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case PROGRESS_DIALOG:
                progressDialog.setProgress(0);
                progressThread = new ProgressThread(handler);
                progressThread.start();
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            int state = msg.arg2;
            progressDialog.setProgress(total);
            if (total >= 100) {
                dismissDialog(PROGRESS_DIALOG);
            }
            if (state == 0 && ljtype == 0) {
                if (wifipassword != null) {
                    wifipassword.setText(null);
                }
                if (connfails != null) {
                    connfails.setVisibility(View.VISIBLE);
                }
            } else if (state == 0 && ljtype == 1) {
                cdb.updatePassword(null, wifi.getSsid());// 把密码清空
                Intent intent = new Intent(WifiConnectActivity.this, WifiActivity.class);
                WifiConnectActivity.this.startActivity(intent);
            } else if (state == 1) {
                Intent intent = new Intent(WifiConnectActivity.this, WifiActivity.class);
                WifiConnectActivity.this.startActivity(intent);
            }
        }
    };

    private class ProgressThread extends Thread {
        Handler mHandler;

        ProgressThread(Handler h) {
            mHandler = h;
        }

        public void run() {
            int state = connwifi(securetype, password, ssid);
            Message msg = mHandler.obtainMessage();
            msg.arg1 = 100;
            msg.arg2 = state;
            mHandler.sendMessage(msg);
        }

    }

    /**
     * 连接wifi
     * 
     * @param securetype
     * @param password
     * @param ssid
     */

    public int connwifi(String securetype, String password, String ssid) {
        int count = 0;
        // TODO Auto-generated method stub
        WifiCipherType wtpe = null;
        if (securetype == null) {
            wtpe = WifiCipherType.WIFICIPHER_NOPASS;
        } else {
            if ("WPE".equals(securetype)) {
                wtpe = WifiCipherType.WIFICIPHER_WEP;
            } else if (securetype.contains("WPA") || securetype.contains("WPA2")) {
                wtpe = WifiCipherType.WIFICIPHER_WPA;
            } else {
                wtpe = WifiCipherType.WIFICIPHER_NOPASS;
            }
        }
        // 如果当前有连接去掉该连接
        if (wifiInfo.getNetworkId() >= 0) {
            wifiUtils.disConnectionWifi(wifiInfo.getNetworkId());
        }
        boolean bool = wifiUtils.Connect(ssid, password, wtpe);
        wifiInfo = wifiUtils.getWifiInfor();
        while (wifiInfo.getIpAddress() == 0 && count < 5) {
            try {
                Thread.currentThread();
                Thread.sleep(2000);
                wifiInfo = wifiUtils.getWifiInfor();
                count++;
            } catch (InterruptedException ie) {
            }
        }
        // 连接成功
        if (bool && wifiInfo.getNetworkId() >= 0 && wifiInfo.getIpAddress() != 0) {
            if (wifi.getPassword() == null) {
                cdb.updatePassword(password, wifi.getSsid());
            } else {
                if (!password.equals(wifi.getPassword())) {
                    cdb.updatePassword(password, wifi.getSsid());
                }
            }
            // Intent intent = new Intent(WifiConnectActivity.this,
            // WifiActivity.class);
            // WifiConnectActivity.this.startActivity(intent);
            return 1;
        } else {// 连接不成功
            return 0;
        }
    }

    OnCheckedChangeListener listener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            if (isChecked) {
                wifipassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showpwd.setText(WifiConnectActivity.this.getResources().getString(R.string.hidepwd));
                // 如果选中，显示密码
            } else {
                wifipassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showpwd.setText(WifiConnectActivity.this.getResources().getString(R.string.showpwd));
                // 否则隐藏密码
            }

        }

    };

}
