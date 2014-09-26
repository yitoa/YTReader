
package com.yt.reader.utils;

public class StringUtils {

    /***
     * 截取书名
     * 
     * @param bookName 书名
     * @param count 显示书名的几个字
     * @return
     */
    public static String subString(String bookName, int count) {
        // 文件夹
        if (!bookName.contains(".")) {
            if (bookName.length() <= count) {
                return bookName;
            } else {
                return bookName.substring(0, count) + "...";
            }
        } else {
            if (bookName.lastIndexOf(".") < 0) {
                return bookName;
            } else {
                String fontName = bookName.substring(0, bookName.lastIndexOf("."));
                if (fontName.length() <= count) {
                    return bookName;
                } else {
                    String hzName = bookName.substring(bookName.lastIndexOf("."));//后缀
                    return fontName.substring(0, count-1) + "..." + hzName;
                }
            }
        }

    }

}
