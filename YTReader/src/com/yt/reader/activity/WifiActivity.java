
package com.yt.reader.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.database.CurdDataBase;
import com.yt.reader.model.Wifi;
import com.yt.reader.utils.WifiUtils;

public class WifiActivity extends YTReaderActivity {
    private static int SIGN_NUM = 4;

    // 用于显示每列5个Item项。
    private static int VIEW_COUNT = 8;

    // 用于显示页号的索引
    private static int index = 0;

    private NetWorkAdapter ma;

    private TextView currentT;// 当前页面

    private TextView totalT;// 总页面

    private int totalBook;

    private ImageButton btnLeft;

    private ImageButton btnRight;

    private TextView wifistate;

    private WifiUtils wifiUtiles;

    private List<ScanResult> mWifiList;

    private ListView networkList;

    private List<Wifi> listWifi;// 存放数据中所有的wifi

    private WifiInfo wifiInfo;// 当前连接情况

    // 排序方式
    private PopupWindow diaLogPop;

    private View dialogView;

    private CurdDataBase cdb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.yt.reader.R.layout.network);
        wifiUtiles = new WifiUtils(this);       
        // 连接情况
        wifiInfo = wifiUtiles.getWifiInfor();
        cdb = new CurdDataBase(this);
        listWifi = cdb.queryAllWifi();
        // 将信号等级和是否是当前app全部初始化
        cdb.updateAll();
        // 扫描附近的app获取扫描的结果
        mWifiList = wifiUtiles.startScan();
        // 将不在数据库的进行添加以及激活当前的app
        saveWifi(mWifiList, listWifi, cdb);
        // 重新获取新的所有的app
        listWifi = cdb.queryAllWifi();
        // wifi是否启用
        CheckBox checkwifi = (CheckBox) findViewById(R.id.checkwifi);
        checkwifi.setOnCheckedChangeListener(new WifiCheckOnCheckChangeListener());
        wifistate = (TextView) findViewById(R.id.wifistate);
        networkList = (ListView) findViewById(R.id.wifilist);
        // 获取初始化的wifi是否被启用
        boolean bool = wifiUtiles.isWifiEnable();
        if (bool) {
            wifistate.setText(WifiActivity.this.getResources().getString(R.string.connected));
            checkwifi.setChecked(true);
        } else {
            wifistate.setText(WifiActivity.this.getResources().getString(R.string.disconnected));
            checkwifi.setChecked(false);
        }

        // 返回按钮
        ImageView netback = (ImageView) findViewById(R.id.netback);

        currentT = (TextView) findViewById(R.id.netcurrentPageCase);
        totalT = (TextView) findViewById(R.id.nettotalPageCase);

        btnLeft = (ImageButton) findViewById(R.id.netleft);
        btnRight = (ImageButton) findViewById(R.id.netright);
        setTotalAndPage();
        checkButton();
        btnLeft.setOnClickListener(new PageButton());
        btnRight.setOnClickListener(new PageButton());
        netback.setOnClickListener(new NetBackOnClickListener());
    }

    public class WifiCheckOnCheckChangeListener implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            if (isChecked) {// 打开wifi,需要时间
                boolean bool = wifiUtiles.OpenWifi();
                wifistate.setText(WifiActivity.this.getResources().getString(R.string.connecting));
                Handler handle = new Handler();
                handle.post(new WifiRunnable(bool));

            } else {
                boolean bool = wifiUtiles.CloseWifi();
                if (bool) {
                    wifistate.setText(WifiActivity.this.getResources().getString(
                            R.string.disconnected));
                    cdb.updateAll();
                    List<Wifi> listWifiInv = cdb.queryAllWifi();
                    ma = new NetWorkAdapter(WifiActivity.this, listWifiInv);
                    networkList.setAdapter(ma);
                }
            }
            networkList.setOnItemClickListener(new NetWorkItemOnClickListener());
            networkList.setOnTouchListener(new NetWorkListOnTounchListener());

        }

    }

    public class WifiRunnable implements Runnable {
        public boolean bool;

        public WifiRunnable(boolean bool) {
            this.bool = bool;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            WifiManager mWifiManager = (WifiManager) WifiActivity.this
                    .getSystemService(WifiActivity.this.WIFI_SERVICE);
            while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                try {
                    // 为了避免程序一直while循环，让它睡个100毫秒在检测……
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }
            }
            if (bool) {
                wifistate.setText(WifiActivity.this.getResources().getString(R.string.connected));
                // 将不在数据库的进行添加以及激活当前的app
                List<ScanResult> wifiScanResult = wifiUtiles.startScan();
                while (wifiScanResult == null) {// 如果为空就循环获取
                    wifiScanResult = wifiUtiles.startScan();
                }
                if(wifiScanResult.size()!=0){
                    cdb.updateAll();
                    saveWifi(wifiScanResult, listWifi, cdb);
                }

                // 重新获取新的所有的app
                listWifi = cdb.queryAllWifi();
                ma = new NetWorkAdapter(WifiActivity.this, listWifi);
                networkList.setAdapter(ma);
            }
        }

    }

    public class NetBackOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            WifiActivity.this.finish();
            Intent intent = new Intent(WifiActivity.this, SettingsActivity.class);
            WifiActivity.this.startActivity(intent);
        }

    }

    /***
     * 连接和扫描wifi的提示页面
     * 
     * @param ssid
     * @param type 1：连接，2：扫描wifi
     */
    private void dialogPopupWindow(String ssid) {
        if (diaLogPop == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            dialogView = layoutInflater.inflate(R.layout.dialog, null, false);
            diaLogPop = new PopupWindow(dialogView, 250, 250);// 创建PopupWindow实例
        }
        TextView title = (TextView) dialogView.findViewById(R.id.tilte);
        TextView content = (TextView) dialogView.findViewById(R.id.content);
        title.setText(WifiActivity.this.getResources().getString(R.string.wifi));
        content.setText(WifiActivity.this.getResources().getString(R.string.connecting) + " "
                + ssid);
        diaLogPop.showAtLocation(this.findViewById(R.id.ll), Gravity.CENTER, 0, 0);
        diaLogPop.update();
    }

    /***
     * 判断扫描的app在数据库是否存在，不存在要添加到数据库中
     * 
     * @param mWifiList
     * @param wifilist
     */
    public void saveWifi(List<ScanResult> mWifiList, List<Wifi> wifilist, CurdDataBase cdb) {
        Map<String, String> wifiMap = new HashMap<String, String>();
        // 以ssid为键组装到Map中
        for (Wifi wifi : wifilist) {
            wifiMap.put(wifi.getSsid(), wifi.getSectype());
        }
        if (mWifiList != null) {
            for (ScanResult sr : mWifiList) {
                String ssid = sr.SSID;
                String sectType = wifiUtiles.securedType(sr.capabilities);
                int signStr = wifiUtiles.calculateSignalLevel(sr.level, SIGN_NUM);
                Wifi wifi = new Wifi();
                wifi.setSsid(ssid);
                wifi.setSectype(sectType);// 加密方式
                wifi.setIscurrentApp(1);// 是当前app
                wifi.setSignStr(signStr);// 信号强度
                if (!wifiMap.containsKey(ssid)) {// 不包含查找的app
                    cdb.save(wifi);
                } else {
                    cdb.updateSingStrAndIsCurrent(wifi);// 修改
                }
            }
        }

    }

    public class NetWorkListOnTounchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mVfDetector.onTouchEvent(event);

        }

    }

    // 进行滑动分页
    private GestureDetector mVfDetector = new GestureDetector(new OnGestureListener() {
        // 手指在屏幕上移动距离小于此值不会被认为是手势
        private static final int SWIPE_MIN_DISTANCE = 80;

        // 手指在屏幕上移动速度小于此值不会被认为手势
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        // 手势识别函数，到此函数被系统回调时说明系统认为发生了手势事件，
        // 我们可以做进一步判定。
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 如果第1个坐标点大于第二个坐标点，说明是向左滑动
            // 滑动距离以及滑动速度是额外判断，可根据实际情况修改。
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                // 下
                Log.i("GestureDemo", "ViewFlipper left");
                if (index + 1 < getPage(totalBook)) {
                    rightView();
                }
                return true;
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) { // 上
                Log.i("GestureDemo", "ViewFlipper right");
                if (index > 0) {
                    leftView();
                }
                return true;
            }

            return false;

        }

        @Override
        public boolean onDown(MotionEvent e) {

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            return false;
        }
    });

    /***
     * 设置总是和页面
     * 
     * @param list
     */
    public void setTotalAndPage() {
        if (mWifiList == null) {
            totalBook = 0;
        } else {
            totalBook = mWifiList.size();
        }
        String totalPage = String.valueOf(getPage(totalBook));
        if (totalBook == 0) {
            totalT.setText("0");
            currentT.setText("0");
        } else {
            totalT.setText(totalPage);
            String currentPage = String.valueOf(index + 1);
            currentT.setText(currentPage);
        }
    }

    public class NetWorkItemOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Wifi wifi = listWifi.get(index * VIEW_COUNT + arg2);
            WifiInfo wifiInfo = wifiUtiles.getWifiInfor();
            if (wifi.getSsid().equals(wifiInfo.getSSID()) && wifi.getIscurrentApp() == 1) {// wifi当前的app已经连接
                Intent mIntent = new Intent(WifiActivity.this, AboutConnActivity.class);
                Bundle mBundle = new Bundle();
                // 加密方式
                mBundle.putString("security", wifi.getSectype());
                mIntent.putExtras(mBundle);
                WifiActivity.this.startActivity(mIntent);

            } else if (!wifi.getSsid().equals(wifiInfo.getSSID()) && wifi.getIscurrentApp() == 1) {// 跳到连接页面进行连接
                if (wifi.getPassword() != null) {
                    // 显示正在连接的
                    //dialogPopupWindow(wifi.getSsid());
                    ProgressDialog mpDialog = new ProgressDialog(WifiActivity.this);
                    mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
                    mpDialog.setMessage(getString(R.string.connecting)+wifi.getSsid());
                    mpDialog.show();
                }
                Intent mIntent = new Intent(WifiActivity.this, WifiConnectActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("wifi", wifi);
                mIntent.putExtras(mBundle);
                WifiActivity.this.startActivity(mIntent);

            } else if (wifi.getIscurrentApp() == 0) {// 在其他地方用过的app
                Intent mIntent = new Intent(WifiActivity.this, InvalidAppActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("wifi", wifi);
                mIntent.putExtras(mBundle);
                WifiActivity.this.startActivity(mIntent);
            }

        }

    }

    /***
     * 下面分页按钮的监听
     * 
     * @author sbp
     */
    private class PageButton implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.netleft:
                    leftView();
                    break;

                case R.id.netright:
                    rightView();
                    break;
            }

        }

    }

    // 点击左边的Button，表示向前翻页，索引值要减1.
    public void leftView() {
        index--;
        // 刷新ListView里面的数值。
        ma.notifyDataSetChanged();

        // 检查Button是否可用。
        checkButton();

        // 显示当前页
        String currentPage = String.valueOf(index + 1);
        currentT.setText(currentPage);

    }

    // 点击右边的Button，表示向后翻页，索引值要加1.
    public void rightView() {
        index++;

        // 刷新ListView里面的数值。
        ma.notifyDataSetChanged();

        // 检查Button是否可用。
        checkButton();
        // 显示当前页
        String currentPage = String.valueOf(index + 1);
        currentT.setText(currentPage);
    }

    /**
     * 左按钮失效
     */
    private void btnLeftEnabled() {
        btnLeft.setImageResource(R.drawable.arrow_1_d);
        btnLeft.setEnabled(false);
    }

    /**
     * 右按钮失效
     */
    private void btnRightEnabled() {
        btnRight.setImageResource(R.drawable.arrow_2_d);
        btnRight.setEnabled(false);
    }

    /***
     * 左按钮启用
     */
    private void btnLeftEnablTrue() {
        btnLeft.setImageResource(R.drawable.arrow_1);
        btnLeft.setEnabled(true);
    }

    /***
     * 右按钮启用
     */
    private void btnRightEnablTrue() {
        btnRight.setImageResource(R.drawable.arrow_2);
        btnRight.setEnabled(true);
    }

    /***
     * 检查两个按钮可能用
     */
    public void checkButton() {
        if (mWifiList == null) {
            btnLeftEnabled();
            btnRightEnabled();
        } else if (mWifiList.size() <= VIEW_COUNT) {
            btnLeftEnabled();
            btnRightEnabled();
        } else {
            // 索引值小于等于0，表示不能向前翻页了，以经到了第一页了。
            // 将向前翻页的按钮设为不可用。
            if (index <= 0) {
                btnLeftEnabled();
                btnRightEnablTrue();
            }
            // 值的长度减去前几页的长度，剩下的就是这一页的长度，如果这一页的长度比View_Count小，表示这是最后的一页了，后面在没有了。
            // 将向后翻页的按钮设为不可用。
            else if (mWifiList.size() - index * VIEW_COUNT <= VIEW_COUNT) {
                btnLeftEnablTrue();
                btnRightEnabled();
            }

            // 否则将2个按钮都设为可用的。
            else {
                btnLeftEnablTrue();
                btnRightEnablTrue();

            }
        }

    }

    /**
     * 获取页数
     * 
     * @param total
     * @return
     */
    private int getPage(int total) {
        int page = 0;
        if (total % VIEW_COUNT == 0) {
            page = total / VIEW_COUNT;
        } else {
            page = total / VIEW_COUNT + 1;
        }
        return page;
    }

    /***
     * @author sbp 组装网络的列表
     */
    public class NetWorkAdapter extends BaseAdapter {
        List<Wifi> data;

        private LayoutInflater mInflater;

        public NetWorkAdapter(Context context, List<Wifi> data) {
            this.data = data;
            this.mInflater = LayoutInflater.from(context);

        }

        // 设置每一页的长度，默认的是View_Count的值。
        @Override
        public int getCount() {

            // ori表示到目前为止的前几页的总共的个数。
            int ori = VIEW_COUNT * index;

            // 值的总个数-前几页的个数就是这一页要显示的个数，如果比默认的值小，说明这是最后一页，只需显示这么多就可以了
            if (data == null) {
                return 0;
            }
            if (data.size() - ori < VIEW_COUNT) {
                return data.size() - ori;
            }
            // 如果比默认的值还要大，说明一页显示不完，还要用换一页显示，这一页用默认的值显示满就可以了。
            else {
                return VIEW_COUNT;
            }
        }

        @Override
        public Object getItem(int position) {

            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) { // 装载布局文件
                convertView = mInflater.inflate(R.layout.networkitem, null);
            }
            TextView ssidV = (TextView) convertView.findViewById(R.id.ssid);
            // 加密方式
            TextView securedV = (TextView) convertView.findViewById(R.id.secured);
            // 是否连接
            ImageView isConnectI = (ImageView) convertView.findViewById(R.id.isconnect);
            // 锁
            ImageView lockI = (ImageView) convertView.findViewById(R.id.lock);
            ImageView signStr = (ImageView) convertView.findViewById(R.id.signStr);
            // TextView要显示的是当前的位置+前几页已经显示的位置个数的对应的位置上的值。

            Wifi wifi = data.get(position + index * VIEW_COUNT);
            wifiInfo = wifiUtiles.getWifiInfor();
            String ssid = wifiInfo.getSSID();
            if (wifi.getSsid().equals(ssid)&&wifiInfo.getIpAddress()!=0) {// 当前wifi正在连接
                isConnectI.setImageResource(R.drawable.con_now);
                wifistate.setText(WifiActivity.this.getResources().getString(R.string.connected)
                        + " " + ssid);
            } else {
                isConnectI.setImageDrawable(null);
            }
            ssidV.setText(wifi.getSsid());
            String str = wifi.getSectype();
            int calevel = wifi.getSignStr();
            if (str == null) {// 没有加密方式
                lockI.setImageDrawable(null);
            } else {// 有加密方式
                securedV.setText(WifiActivity.this.getResources().getString(R.string.secured) + " "
                        + str);
                lockI.setImageResource(R.drawable.lock);
            }

            if (calevel < 0) {
                signStr.setImageResource(R.drawable.wifi_black5);
            } else if (calevel == 0) {
                signStr.setImageResource(R.drawable.wifi_black1);
            } else if (calevel == 1) {
                signStr.setImageResource(R.drawable.wifi_black2);
            } else if (calevel == 2) {
                signStr.setImageResource(R.drawable.wifi_black3);
            } else if (calevel == 3) {
                signStr.setImageResource(R.drawable.wifi_black4);
            } else if (calevel == 4) {
                signStr.setImageResource(R.drawable.wifi_black4);
            }
            return convertView;
        }

    }

}
