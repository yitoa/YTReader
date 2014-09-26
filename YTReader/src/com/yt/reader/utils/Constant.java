package com.yt.reader.utils;

import android.graphics.Color;

public class Constant {
	public static enum FileType {
		PDF, EPUB, FB2, TXT, CHM, UMD, HTM, PDB, MOBI, DJVU, RTF, HTML
	}

	public static final String STYLE_REFERENCE = "style_ref";
	public static final String STYLE_DEFAULT_TYPEFACE = "arial.ttf";// 字体，如宋体、Serif等
	public static final int STYLE_DEFAULT_SIZE = 20;// 字号
	public static final String STYLE_DEFAULT_FONT_STYLE = "normal";// 字形，如常规、斜体、加粗等
	public static final int STYLE_DEFAULT_LINE_SPACING = 4;// 行距
	public static final int STYLE_DEFAULT_TEXT_COLOR = Color.BLACK;// 字体颜色
	public static final int STYLE_DEFAULT_BG_COLOR = Color.WHITE;// 背景颜色
	public static final int STYLE_DEFAULT_MARGIN_WIDTH = 25;// 左右与边缘的距离
	public static final int STYLE_DEFAULT_MARGIN_HEIGHT = 25; // 上下与边缘的距离
	public static final String STYLE_IS_DEFAULT = "true";// 是否为系统默认设置

	public static final int STYLE_SIZE1 = 14;// 字号
	public static final int STYLE_SIZE2 = 16;// 字号
	public static final int STYLE_SIZE3 = 18;// 字号
	public static final int STYLE_SIZE4 = STYLE_DEFAULT_SIZE;// 字号
	public static final int STYLE_SIZE5 = 22;// 字号
	public static final int STYLE_SIZE6 = 24;// 字号
	public static final int STYLE_SIZE7 = 26;// 字号

	public static final String[] STYLE_TYPEFACE = new String[] { "arial.ttf",
			"simhei.ttf", "calibri.ttf", "timesNewRoman.ttf",
			"verdana.ttf" };
	public static final String[] STYLE_TYPEFACE_TITLE = new String[] { "Arial",
			"Simhei", "Calibri", "Times New Roman", "Verdana" };

	public static final int STYLE_LINE_SPACING1 = 2;
	public static final int STYLE_LINE_SPACING2 = STYLE_DEFAULT_LINE_SPACING;
	public static final int STYLE_LINE_SPACING3 = 6;

	public static final int STYLE_MARGIN_WIDTH1 = 15;
	public static final int STYLE_MARGIN_WIDTH2 = STYLE_DEFAULT_MARGIN_WIDTH;
	public static final int STYLE_MARGIN_WIDTH3 = 35;
	public static final int STYLE_MARGIN_HEIGHT1 = 15;
	public static final int STYLE_MARGIN_HEIGHT2 = STYLE_DEFAULT_MARGIN_HEIGHT;
	public static final int STYLE_MARGIN_HEIGHT3 = 35;
	
	
	public static final String STYLE_DEFAULT_LANG="CN";
	
	public static final String COLUMN_LANG="lang";
	
	public static final String STYLE_DEFAULT_PAGETURN="NO";
	public static final String STYLE_PAGETURN_CURL="CURL";
	public static final String COLUMN_PAGETURN="pageturn";	

}
