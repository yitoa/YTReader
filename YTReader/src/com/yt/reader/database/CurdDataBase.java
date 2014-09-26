
package com.yt.reader.database;

import java.util.ArrayList;
import java.util.List;

import com.yt.reader.model.Book;
import com.yt.reader.model.Wifi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CurdDataBase {
    private DatabaseHelper dataBaseHelper;

    public CurdDataBase(Context context) {
        this.dataBaseHelper = new DatabaseHelper(context);
    }

    /**
     * wifi信息插入到数据库中
     * 
     * @param rf
     */
    public void save(Wifi wifi) {

        SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
        database.execSQL(
                "insert into " + DBSchema.TABLE_WIFI + "(" + DBSchema.COLUMN_SSID + ","
                        + DBSchema.COLUMN_SECTYPE + "," + DBSchema.COLUMN_WIFIPASSWORD + ","
                        + DBSchema.COLUMN_ISCURRENTAPP + "," + DBSchema.COLUMN_SIGNSTR
                        + ") values (?,?,?,?,?)",
                new Object[] {
                        wifi.getSsid(), wifi.getSectype(), wifi.getPassword(),
                        wifi.getIscurrentApp(), wifi.getSignStr()
                });
        Log.d("myDebug", "添加数据库执行成功");
        database.close();
    }

    /**
     * 根据文件名查找数据库中是否存在该信息
     * 
     * @param filename
     * @return
     */
    public Wifi queryObject(String ssid) {
        Wifi wifi = null;
        SQLiteDatabase database = dataBaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select " + DBSchema.COLUMN_SSID + ", "
                + DBSchema.COLUMN_SECTYPE + ", " + DBSchema.COLUMN_WIFIPASSWORD + ", "
                + DBSchema.COLUMN_ISCURRENTAPP + ", " + DBSchema.COLUMN_SIGNSTR + " from "
                + DBSchema.TABLE_WIFI + " where " + DBSchema.COLUMN_SSID + "=?", new String[] {
            ssid
        });
        if (cursor.moveToFirst()) {
            wifi = new Wifi();
            wifi.setSsid(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_SSID)));
            wifi.setPassword(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_WIFIPASSWORD)));
            wifi.setSectype(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_SECTYPE)));
            wifi.setIscurrentApp(cursor.getInt(cursor.getColumnIndex(DBSchema.COLUMN_ISCURRENTAPP)));
            wifi.setSignStr(cursor.getInt(cursor.getColumnIndex(DBSchema.COLUMN_SIGNSTR)));

        }
        cursor.close();
        database.close();
        return wifi;
    }

    /**
     * 根据ssid删除记录信息
     * 
     * @param ssid
     */
    public void del(String ssid) {
        try {
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL("delete from " + DBSchema.TABLE_WIFI + " where "
                    + DBSchema.COLUMN_SSID + "=?", new Object[] {
                ssid
            });
            database.close();
            Log.d("myDebug", "删除数据成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据ssid修改wifi表中的其余的状态
     */
    public void update(Wifi wifi) {
        try {
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL("update " + DBSchema.TABLE_WIFI + " set "
                    + DBSchema.COLUMN_WIFIPASSWORD + "=?," + DBSchema.COLUMN_SECTYPE + "=? where "
                    + DBSchema.COLUMN_SSID + "=?", new Object[] {
                    wifi.getPassword(), wifi.getSectype(), wifi.getSsid()
            });
            database.close();
            Log.d("myDebug", "修改数据库中数据成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBook(Book book) {
        try {
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL("update " + DBSchema.TABLE_BOOK + " set "
                    + DBSchema.COLUMN_BOOK_LAST_READING_TIME + "=?  where " + "_id=?",
                    new Object[] {
                            null, book.getId()
                    });
            database.close();
            Log.d("myDebug", "修改数据库中数据成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****
     * 查找所有的wifi资源
     * 
     * @return
     */
    public List<Wifi> queryAllWifi() {
        List<Wifi> list = new ArrayList<Wifi>();
        Wifi wifi = null;
        SQLiteDatabase database = dataBaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select " + DBSchema.COLUMN_SSID + ","
                + DBSchema.COLUMN_SECTYPE + "," + DBSchema.COLUMN_WIFIPASSWORD + ", "
                + DBSchema.COLUMN_ISCURRENTAPP + ", " + DBSchema.COLUMN_SIGNSTR + " from "
                + DBSchema.TABLE_WIFI + " order by " + DBSchema.COLUMN_SIGNSTR + " desc", null);
        while (cursor.moveToNext()) {
            wifi = new Wifi();
            wifi.setSsid(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_SSID)));
            wifi.setPassword(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_WIFIPASSWORD)));
            wifi.setSectype(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_SECTYPE)));
            wifi.setIscurrentApp(cursor.getInt(cursor.getColumnIndex(DBSchema.COLUMN_ISCURRENTAPP)));
            wifi.setSignStr(cursor.getInt(cursor.getColumnIndex(DBSchema.COLUMN_SIGNSTR)));
            list.add(wifi);
        }
        cursor.close();
        database.close();
        return list;

    }

    /***
     * 初始化wifi状态
     */
    public void updateAll() {
        try {
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL("update " + DBSchema.TABLE_WIFI + " set "
                    + DBSchema.COLUMN_ISCURRENTAPP + "=? ," + DBSchema.COLUMN_SIGNSTR + "=?",
                    new Object[] {
                            0, -1
                    });
            database.close();
            Log.d("myDebug", "修改数据库中数据成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 修改密码
     */
    public void updatePassword(String password, String ssid) {
        try {
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL("update " + DBSchema.TABLE_WIFI + " set "
                    + DBSchema.COLUMN_WIFIPASSWORD + "=?  where " + DBSchema.COLUMN_SSID + "=?",
                    new Object[] {
                            password, ssid
                    });
            database.close();
            Log.d("myDebug", "修改数据库中密码成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 修改信号强度和是否是当前的app以及加密方式
     * 
     * @param wifi
     */
    public void updateSingStrAndIsCurrent(Wifi wifi) {
        try {
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL(
                    "update " + DBSchema.TABLE_WIFI + " set " + DBSchema.COLUMN_ISCURRENTAPP
                            + "=? ," + DBSchema.COLUMN_SIGNSTR + "=? ," + DBSchema.COLUMN_SECTYPE
                            + "=? where " + DBSchema.COLUMN_SSID + "=?",
                    new Object[] {
                            wifi.getIscurrentApp(), wifi.getSignStr(), wifi.getSectype(),
                            wifi.getSsid()
                    });
            database.close();
            Log.d("myDebug", "修改数据库中数据成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
