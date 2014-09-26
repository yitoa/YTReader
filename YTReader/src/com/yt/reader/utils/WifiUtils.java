
package com.yt.reader.utils;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiUtils {

    /** Called when the activity is first created. */

    private WifiManager mWifiManager;

    // 扫描出的网络连接列表
    private List<ScanResult> mWifiList;

    // 网络连接列表
    private List<WifiConfiguration> mWifiConfigurations;

    /***
     * @author sbp 两种编码方式WEP、WPA
     */
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    public WifiUtils(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
    }

    // 断开指定ID的网络
    public void disConnectionWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public List<ScanResult> startScan() {
        mWifiManager.startScan();
        // 得到扫描结果
        return mWifiManager.getScanResults();
    }

    /***
     * 获取当前连接的信息
     * 
     * @return
     */
    public WifiInfo getWifiInfor() {
        return mWifiManager.getConnectionInfo();
    }

    /***
     * 计算信号强度
     * 
     * @param rssi
     * @param numLevels
     * @return
     */
    public int calculateSignalLevel(int rssi, int numLevels) {
        return mWifiManager.calculateSignalLevel(rssi, numLevels);
    }

    /***
     * 获取加密方式
     * 
     * @param str
     * @return
     */
    public String securedType(String str) {
        if (str.contains("WPA") && str.contains("WPA2")) {
            return "WPA/WP2";
        } else if (str.contains("WPA")) {
            return "WPA";
        } else if (str.contains("WPA2")) {
            return "WPA2";
        } else if (str.contains("WEP")) {
            return "WPE";
        }
        return null;
    }

    public List<ScanResult> getmWifiList() {
        return mWifiList;
    }

    /***
     * 获取wifi是否被启用
     * 
     * @return
     */
    public boolean isWifiEnable() {
        return mWifiManager.isWifiEnabled();
    }

    /***
     * 关闭wifi
     * 
     * @return
     */
    public boolean CloseWifi() {
        boolean bRet = true;
        if (mWifiManager.isWifiEnabled()) {// 如果是启用的
            bRet = mWifiManager.setWifiEnabled(false);
        }
        return bRet;
    }

    // 打开wifi功能
    public boolean OpenWifi() {
        boolean bRet = true;
        if (!mWifiManager.isWifiEnabled()) {
            bRet = mWifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    // 提供一个外部接口，传入要连接的无线网
    public boolean Connect(String SSID, String Password, WifiCipherType Type) {
        if (!this.OpenWifi()) {
            return false;
        }
        // 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
        // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            try {
                // 为了避免程序一直while循环，让它睡个100毫秒在检测……
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        }

        WifiConfiguration wifiConfig = this.CreateWifiInfo(SSID, Password, Type);
        //
        if (wifiConfig == null) {
            return false;
        }

        WifiConfiguration tempConfig = this.IsExsits(SSID);

        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        int netID = mWifiManager.addNetwork(wifiConfig);
        boolean bRet = mWifiManager.enableNetwork(netID, true);
        return bRet;
    }

    // 查看以前是否也配置过这个网络
    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiConfiguration CreateWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration wc = new WifiConfiguration();
        wc.allowedAuthAlgorithms.clear();
        wc.allowedGroupCiphers.clear();
        wc.allowedKeyManagement.clear();
        wc.allowedPairwiseCiphers.clear();
        wc.allowedProtocols.clear();
        wc.SSID = "\"" + SSID + "\"";
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            wc.wepKeys[0] = "";
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wc.wepTxKeyIndex = 0;
        } else if (Type == WifiCipherType.WIFICIPHER_WEP) {
            wc.wepKeys[0] = "\"" + Password + "\""; // 该热点的密码
            wc.hiddenSSID = true;
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wc.wepTxKeyIndex = 0;
        } else if (Type == WifiCipherType.WIFICIPHER_WPA) {
            wc.preSharedKey = "\"" + Password + "\"";
            wc.hiddenSSID = true;
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        } else {
            return null;
        }
        return wc;
    }
}
