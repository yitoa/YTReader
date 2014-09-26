
package com.yt.reader;

import android.os.Bundle;
import android.widget.TextView;

import com.yt.reader.base.YTReaderActivity;

public class SearchActivity extends YTReaderActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Search tab");
        setContentView(textview);
    }

}
