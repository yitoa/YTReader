
package com.yt.reader.activity;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.utils.DateUtils;

public class DateAndTimeActivity extends YTReaderActivity {
    // 获取日期格式器对象
    DateFormat fmtDate = DateFormat.getDateInstance();

    // 获取时间格式器对象
    DateFormat fmtTime = DateFormat.getTimeInstance();

    Calendar dateAndTime =null;

    private TextView dateVal;

    private TextView timeVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题
        setContentView(R.layout.dateandtime);
        dateVal = (TextView) findViewById(R.id.dateVal);
        timeVal = (TextView) findViewById(R.id.timeVal);
        ImageView dateback = (ImageView) findViewById(R.id.dateback);
        long time = DateUtils.getGreenwichDate(null)
                .getTime();
        TimeZone tz = TimeZone.getTimeZone("Etc/Greenwich");
        dateAndTime=Calendar.getInstance(tz);
        dateAndTime.setTimeInMillis(time);

        // 得到页面设定日期的按钮控件对象
        TextView dateBtn = (TextView) findViewById(R.id.dateSet);
        TextView timeBtn = (TextView) findViewById(R.id.timeSet);
        // 设置按钮的点击事件监听器
        dateBtn.setOnClickListener(new DateAndTimeOnClickListener());
        timeBtn.setOnClickListener(new DateAndTimeOnClickListener());
        dateback.setOnClickListener(new DateAndTimeOnClickListener());

        // 更新页面内容
        updateViewContent();

    }

    public class DateAndTimeOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dateSet:
                    // 生成一个DatePickerDialog对象，并显示。显示的DatePickerDialog控件可以选择年月日，并设置
                    new DatePickerDialog(DateAndTimeActivity.this, d,
                            dateAndTime.get(Calendar.YEAR), dateAndTime.get(Calendar.MONTH),
                            dateAndTime.get(Calendar.DAY_OF_MONTH)).show();
                    break;
                case R.id.timeSet:
                    new TimePickerDialog(DateAndTimeActivity.this, t,
                            dateAndTime.get(Calendar.HOUR_OF_DAY),
                            dateAndTime.get(Calendar.MINUTE), true).show();
                    break;
                case R.id.dateback:
                    DateAndTimeActivity.this.finish();
                    //Intent intent = new Intent(DateAndTimeActivity.this, SettingsActivity.class);
                    //DateAndTimeActivity.this.startActivity(intent);
                    break;

            }

        }

    }

    // 当点击DatePickerDialog控件的设置按钮时，调用该方法
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // 修改日历控件的年，月，日
            // 这里的year,monthOfYear,dayOfMonth的值与DatePickerDialog控件设置的最新值一致
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            // 将页面TextView的显示更新为最新时间
            updateViewContent();
            // 更新系统时间
            SystemClock.setCurrentTimeMillis(dateAndTime.getTimeInMillis());
        }
    };

    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {

        // 同DatePickerDialog控件
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            updateViewContent();

            // 更新系统时间
            SystemClock.setCurrentTimeMillis(dateAndTime.getTimeInMillis());
        }
    };

    // 更新页面TextView的方法
    private void updateViewContent() {
        // 获取日期
        Date date = DateUtils.getGreenwichDate(dateAndTime.getTime());
        String dateV = DateUtils.dateToString(date, "yyyy-MM-dd");
        // 获取时间
        String timeV = DateUtils.dateToString(date, "HH:mm");
       
        dateVal.setText(dateV);

        timeVal.setText(timeV);
    }

}
