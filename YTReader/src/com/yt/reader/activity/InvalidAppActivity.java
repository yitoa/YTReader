
package com.yt.reader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.database.CurdDataBase;
import com.yt.reader.model.Wifi;

public class InvalidAppActivity extends YTReaderActivity {

    private CurdDataBase cdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invalidconnect);
        Wifi wifi = (Wifi) getIntent().getSerializableExtra("wifi");
        cdb = new CurdDataBase(this);

        TextView forgetV = (TextView) findViewById(R.id.invalidforget);
        forgetV.setOnClickListener(new ForGetOnClickListener(wifi.getSsid()));

        ImageView invalidConnback = (ImageView) findViewById(R.id.invalidConnback);
        invalidConnback.setOnClickListener(new InvalidConnBackOnClickListener());

    }

    private class InvalidConnBackOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            InvalidAppActivity.this.finish();
            Intent intent = new Intent(InvalidAppActivity.this, WifiActivity.class);
            InvalidAppActivity.this.startActivity(intent);
        }

    }

    private class ForGetOnClickListener implements OnClickListener {

        private String ssid;

        public ForGetOnClickListener(String ssid) {
            this.ssid = ssid;
        }

        @Override
        public void onClick(View v) {
            cdb.del(ssid);
            Intent intent = new Intent(InvalidAppActivity.this, WifiActivity.class);
            InvalidAppActivity.this.startActivity(intent);
        }

    }

}
