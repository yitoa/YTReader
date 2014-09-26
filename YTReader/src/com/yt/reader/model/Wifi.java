
package com.yt.reader.model;

import java.io.Serializable;

public class Wifi implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ssid;

    private String sectype;

    private String password;

    private int iscurrentApp;// 是否是当前app

    private int signStr;// 信号等级

    public int getIscurrentApp() {
        return iscurrentApp;
    }

    public void setIscurrentApp(int iscurrentApp) {
        this.iscurrentApp = iscurrentApp;
    }

    public int getSignStr() {
        return signStr;
    }

    public void setSignStr(int signStr) {
        this.signStr = signStr;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSectype() {
        return sectype;
    }

    public void setSectype(String sectype) {
        this.sectype = sectype;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
