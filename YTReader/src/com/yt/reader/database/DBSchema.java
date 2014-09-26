
package com.yt.reader.database;

import android.net.Uri;

public class DBSchema {
    public static final String DB_NAME = "reader";

    public static final int DB_VERSION = 1;

    public static final String TABLE_BOOK = "book";// 表名，书

    public static final String COLUMN_BOOK_NAME = "name";// 文件名，如“abc.txt“

    public static final String COLUMN_BOOK_REALNAME = "realName";// 书名，可能不同与文件名，需要解析文件才能找到，如果未定义，则为空。

    public static final String COLUMN_BOOK_AUTHOR = "author";// 作者

    public static final String COLUMN_BOOK_SIZE = "size";// 文件大小

    public static final String COLUMN_BOOK_FILETYPE = "fileType";// 文件类型，默认由文件名来识别，也可能通过解析文件得到。

    public static final String COLUMN_BOOK_CURRENT_LOCATION = "currentLocation";// 当前阅读的页码或者偏移地址

    public static final String COLUMN_BOOK_TOTAL_PAGE = "totalPage";// 总页数，只有部分格式用到

    public static final String COLUMN_BOOK_LAST_MODIFY_TIME = "lastModifyTime";// 最后一次修改时间，存入数据库的是greenwich时间

    public static final String COLUMN_BOOK_LAST_READING_TIME = "lastReadingTime";// 最后一次阅读时间，存入数据库的是greenwich时间

    public static final String COLUMN_BOOK_ADDED_TIME = "addedTime";// 加入数据库的时间，存入数据库的是greenwich时间

    public static final String COLUMN_BOOK_PATH = "path";// 文件的父目录，如/mnt/sdcard

    public static final String COLUMN_BOOK_BITMAP = "bitmap";// 书的封面图
    
    public static final String COLUMN_BOOK_COVER_PATH = "coverPath";// 书的封面图片路劲+文件名+扩展名

    public static final String COLUMN_BOOK_ISDRM = "isDRM";// TODO DRM

    public static final String TABLE_BOOKMARK = "bookmark";// 表名，书签

    public static final String COLUMN_BOOKMARK_BOOKID = "bookId";// 外键，指向book._id

    public static final String COLUMN_BOOKMARK_LOCATION = "location";// 书签位置，页码或者偏移地址

    public static final String COLUMN_BOOKMARK_DESCRIPTION = "description";

    public static final String TABLE_CHAPTER = "chapter";// 表名，章节

    public static final String COLUMN_CHAPTER_BOOKID = "bookId";// 外键，指向book._id

    public static final String COLUMN_CHAPTER_LOCATION = "location";// 章节起始位置，页码或者偏移地址

    public static final String COLUMN_CHAPTER_TITLE = "title";// 章节的标题

    public static final String TABLE_STYLE = "style";// 表名，文本样式

    public static final String COLUMN_STYLE_TYPEFACE = "typeface";// 字体，如宋体、Serif等

    public static final String COLUMN_STYLE_SIZE = "size";// 字号

    public static final String COLUMN_STYLE_FONT_STYLE = "fontStyle";// 字形，如常规、斜体、加粗等

    public static final String COLUMN_STYLE_LINE_SPACING = "lineSpacing";// 行距

    public static final String COLUMN_STYLE_TEXT_COLOR = "textColor";// 字体颜色

    public static final String COLUMN_STYLE_BG_COLOR = "bgColor";// 背景颜色

    public static final String COLUMN_STYLE_MARGIN_WIDTH = "marginWidth";// 左右与边缘的距离

    public static final String COLUMN_STYLE_MARGIN_HEIGHT = "marginHeight"; // 上下与边缘的距离

    public static final String COLUMN_STYLE_IS_DEFAULT = "isDefault";// 是否为系统默认设置

    public static final String AUTHORITY = "com.yt.reader.database.ReaderProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_BOOK);

    public static final Uri CONTENT_URI_BOOK = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_BOOK);

    public static final Uri CONTENT_URI_BOOKMARK = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_BOOKMARK);

    public static final Uri CONTENT_URI_CHAPTER = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_CHAPTER);

    public static final String BOOKS_TYPE = "vnd.android.cursor.dir/" + TABLE_BOOK;

    public static final String BOOKS_ITEM_TYPE = "vnd.android.cursor.item/" + TABLE_BOOK;

    public static final String TABLE_WIFI = "wifi";// wifi

    public static final String COLUMN_SSID = "ssid";// ssid

    public static final String COLUMN_SECTYPE = "sectype";// 加密方式

    public static final String COLUMN_WIFIPASSWORD = "password";// 密码

    public static final String COLUMN_ISCURRENTAPP = "iscurrentapp";// 是否当前的app,0:不是，1：是

    public static final String COLUMN_SIGNSTR = "signstr";// 信号等级

}
