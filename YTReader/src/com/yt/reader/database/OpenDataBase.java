
package com.yt.reader.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;

import com.yt.reader.model.Book;
import com.yt.reader.utils.DateUtils;

/***
 * @author sbp 数据库类
 */
public class OpenDataBase {
    
    /***
     * 判断书籍库中是否存该书的信息，存在不添加，不存在添加到数据库中
     * 
     * @param bookList
     * @param resolver
     */
    public void saveBooks(List<Book> bookList, ContentResolver resolver) {
        List<String> list = new ArrayList<String>();
        // 查找本地书籍信息是否在书籍库中
        for (Book book : bookList) {
            // 把本地的书籍都放到list
            list.add(book.getPath() + "/" + book.getName());
            Cursor cursor = resolver.query(DBSchema.CONTENT_URI_BOOK, new String[] {
                    DBSchema.COLUMN_BOOK_NAME, "_id", DBSchema.COLUMN_BOOK_AUTHOR,
                    DBSchema.COLUMN_BOOK_LAST_READING_TIME
            }, DBSchema.COLUMN_BOOK_NAME + "=? AND " + DBSchema.COLUMN_BOOK_PATH + "=?",
                    new String[] {
                            book.getName(), book.getPath()
                    }, null);
            if (!cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(DBSchema.COLUMN_BOOK_NAME, book.getName());
                values.put(DBSchema.COLUMN_BOOK_REALNAME, book.getName());
                values.put(DBSchema.COLUMN_BOOK_SIZE, book.getSize());
                values.put(DBSchema.COLUMN_BOOK_FILETYPE, book.getFileType());
                values.put(DBSchema.COLUMN_BOOK_PATH, book.getPath());
                values.put(DBSchema.COLUMN_BOOK_CURRENT_LOCATION, 0);
                values.put(DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME, book.getLastModifyTime());
                values.put(DBSchema.COLUMN_BOOK_ADDED_TIME, book.getAddedTime());
                values.put(DBSchema.COLUMN_BOOK_TOTAL_PAGE, book.getTotalPage());
                resolver.insert(DBSchema.CONTENT_URI_BOOK, values);
            } else {
                continue;
            }
        }
        // 查找数据库中的书籍信息本地可还有，要是没有就删除掉，保证本地信息和书籍库中的信息同步
        Cursor cursor = resolver.query(DBSchema.CONTENT_URI_BOOK, new String[] {
                DBSchema.COLUMN_BOOK_NAME, DBSchema.COLUMN_BOOK_PATH
        }, null, null, null);
        while (cursor.moveToNext()) {
            // 获取文件名
            String path = cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_PATH));
            String name = cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_NAME));
            // 数据库中的记录没有在本地存在
            if (!list.contains(path + "/" + name)) {
                // 删除数据库中多余的信息
                resolver.delete(DBSchema.CONTENT_URI_BOOK, DBSchema.COLUMN_BOOK_NAME + "=? AND "
                        + DBSchema.COLUMN_BOOK_PATH + "=?", new String[] {
                        name, path
                });
                // TODO 删除书签
            }

        }

    }

    /****
     * 获取正在阅读的书籍信息
     * 
     * @return
     */
    public Book getReaderingBook(Cursor cursor) {
        Book book = null;
        if (cursor.moveToFirst()) {
            book = new Book();
            book.setPath(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_PATH)));
            book.setName(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_NAME)));
            book.setCoverPath(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_COVER_PATH)));
            String fileType = cursor
                    .getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_FILETYPE));
            if (null != fileType)
                book.setFileType(fileType);
            book.setCurrentLocation(cursor.getLong(cursor
                    .getColumnIndex(DBSchema.COLUMN_BOOK_CURRENT_LOCATION)));
            book.setTotalPage(cursor.getLong(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_TOTAL_PAGE)));
        }
        cursor.close();
        return book;
    }

    /***
     * 获取最近的阅读记录
     * 
     * @return
     */
    public List<Book> geRecentlyBook(Cursor cursor) {
        // SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        // Cursor cursor =
        // db.rawQuery("select * from book order by lastReadingTime desc",
        // null);

        List<Book> list = new ArrayList<Book>();
        while (cursor.moveToNext()) {
            Book book = new Book();
            book.setName(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_NAME)));
            book.setId(cursor.getLong(cursor.getColumnIndex("_id")));
            book.setAuthor(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_AUTHOR)));
            book.setLastReadingTime(cursor.getLong(cursor
                    .getColumnIndex(DBSchema.COLUMN_BOOK_LAST_READING_TIME)));
            book.setAddedTime(cursor.getLong(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_ADDED_TIME)));
            book.setPath(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_PATH)));
            book.setSize(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_SIZE)));
            list.add(book);
        }
        cursor.close();
        // 去掉第一个正在阅读的书籍
        if (!list.isEmpty())
            list.remove(0);
        return list;
    }

    /***
     * 获取新添加的书籍信息
     * 
     * @return
     */
    public List<Book> getNewBookAdd(Cursor cursor) {
        List<Book> list = new ArrayList<Book>();
        while (cursor.moveToNext()) {
            Book book = new Book();
            book.setName(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_NAME)));
            book.setId(cursor.getLong(cursor.getColumnIndex("_id")));
            book.setPath(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_PATH)));
            book.setCoverPath(cursor.getString(cursor.getColumnIndex(DBSchema.COLUMN_BOOK_COVER_PATH)));
            list.add(book);
        }
        cursor.close();
        return list;
    }

    /**
     * 检索符合条件的书，如果不存在，则插入该书
     * 
     * @param book
     * @param resolver
     * @return
     */
    public Cursor getOrInsertBook(Book book, ContentResolver resolver) {// TODO

        String[] projection = new String[] {
                BaseColumns._ID, DBSchema.COLUMN_BOOK_NAME, DBSchema.COLUMN_BOOK_REALNAME,
                DBSchema.COLUMN_BOOK_AUTHOR, DBSchema.COLUMN_BOOK_SIZE,
                DBSchema.COLUMN_BOOK_FILETYPE, DBSchema.COLUMN_BOOK_CURRENT_LOCATION,
                DBSchema.COLUMN_BOOK_TOTAL_PAGE, DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME,
                DBSchema.COLUMN_BOOK_ADDED_TIME, DBSchema.COLUMN_BOOK_PATH,
                DBSchema.COLUMN_BOOK_BITMAP, DBSchema.COLUMN_BOOK_ISDRM,DBSchema.COLUMN_BOOK_COVER_PATH
        };
        Cursor cursor = resolver.query(DBSchema.CONTENT_URI_BOOK, projection,
                DBSchema.COLUMN_BOOK_NAME + "=? AND " + DBSchema.COLUMN_BOOK_PATH + "=?",
                new String[] {
                        book.getName(), book.getPath()
                }, null);
        if (!cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(DBSchema.COLUMN_BOOK_NAME, book.getName());
            values.put(DBSchema.COLUMN_BOOK_FILETYPE, book.getFileType());
            values.put(DBSchema.COLUMN_BOOK_REALNAME, book.getRealName());
            values.put(DBSchema.COLUMN_BOOK_AUTHOR, book.getAuthor());
            values.put(DBSchema.COLUMN_BOOK_SIZE, book.getSize());
            values.put(DBSchema.COLUMN_BOOK_CURRENT_LOCATION, 0);
            values.put(DBSchema.COLUMN_BOOK_TOTAL_PAGE, book.getTotalPage());
            values.put(DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME, book.getLastModifyTime());
            values.put(DBSchema.COLUMN_BOOK_ADDED_TIME, DateUtils.getGreenwichDate(null).getTime());
            values.put(DBSchema.COLUMN_BOOK_PATH, book.getPath());
            // values.put(DBSchema.COLUMN_BOOK_BITMAP, book.getBitmap());TODO
            values.put(DBSchema.COLUMN_BOOK_ISDRM, book.isDRM());
            resolver.insert(DBSchema.CONTENT_URI_BOOK, values);
            return resolver.query(DBSchema.CONTENT_URI_BOOK, projection, DBSchema.COLUMN_BOOK_NAME
                    + "=? AND " + DBSchema.COLUMN_BOOK_PATH + "=?", new String[] {
                    book.getName(), book.getPath()
            }, null);
        } else {
            return cursor;
        }
    }
}
