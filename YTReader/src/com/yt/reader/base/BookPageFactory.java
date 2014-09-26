package com.yt.reader.base;

import java.io.IOException;

import android.graphics.Canvas;

import com.yt.reader.model.Book;

public abstract class BookPageFactory {
    protected int mWidth;

    protected int mHeight;

    public BookPageFactory(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * 打开图书，book的name和path不能为空。
     * 
     * @param book
     * @throws IOException
     */
    public abstract void openbook(Book book) throws IOException;

    /**
     * 绘制页面，对匹配的searchText进行高亮显示
     * 
     * @param c
     * @param searchText
     */
    public abstract void onDraw(Canvas c, String searchText);

    /**
     * 往前翻一页，只得到要绘制的内容，draw由onDraw(Canvas c)来完成。
     * 
     * @throws IOException
     */
    public abstract void prePage() throws IOException;

    /**
     * 往后翻一页，只得到要绘制的内容，draw由onDraw(Canvas c)来完成。
     * 
     * @throws IOException
     */
    public abstract void nextPage() throws IOException;

    /**
     * 跳转到指定位置，通过onDraw将内容画到bitmap上，以供BookView显示
     * 
     * @param location 指定的跳转位置
     * @param handleMessyCode 为true,则需要处理乱码，指定的location可能不是最终跳转的location。
     * @throws IOException
     */
    public abstract void gotoPage(long location, boolean handleMessyCode) throws IOException;

    public abstract boolean isfirstPage();

    public abstract boolean islastPage();

    /**
     * 得到当前阅读位置
     * 
     * @return
     */
    public abstract long getReadingLocation();

    /**
     * 设置当前阅读位置
     */
    public abstract void setReadingLocation(long location);

    /**
     * 在当前阅读位置增加书签
     */
    public abstract void addBookmark();

    /**
     * 重新设置文本显示样式
     */
    public abstract void setTextStyle();
}
