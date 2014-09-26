
package com.yt.reader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/***
 * 恢复出厂设置的时候用 初始化数据库
 * 
 * @author sbp
 */

public class InitDataBase {

    private DatabaseHelper dataBaseHelper;

    public InitDataBase(Context context) {
        this.dataBaseHelper = new DatabaseHelper(context);
    }

    /**
     * 删除wifi表 ,书签表,章节表,文本样式表中的信息
     * 
     * @param ssid
     */
    public void del() {
        try {
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL("delete from " + DBSchema.TABLE_WIFI);
            database.execSQL("delete from " + DBSchema.TABLE_BOOKMARK);
            database.execSQL("delete from " + DBSchema.TABLE_CHAPTER);
            database.execSQL("delete from " + DBSchema.TABLE_STYLE);
            database.close();
            Log.d("myDebug", "删除数据成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 修改book表中的信息,把最近阅读时间置空
     */
    public void updateBook() {
        try {
            SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
            database.execSQL("update " + DBSchema.TABLE_BOOK + " set "
                    + DBSchema.COLUMN_BOOK_LAST_READING_TIME + "=?,"
                    + DBSchema.COLUMN_BOOK_CURRENT_LOCATION + "=?", new Object[] {
                    null, 0
            });
            database.close();
            Log.d("myDebug", "修改数据库中数据成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
