package com.yt.reader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.yt.reader.database.DBSchema;

public class FactoryUtils {
	/**
	 * 设置系统默认的文本显示样式
	 * 
	 * @param context
	 */
	public static void setDefaultBookStyle(Context context) {
		SharedPreferences style = context.getSharedPreferences(
				Constant.STYLE_REFERENCE, 0);
		SharedPreferences.Editor editor = style.edit();
		if (null != style.getString(DBSchema.COLUMN_STYLE_TYPEFACE, null))
			return;
		editor.putInt(DBSchema.COLUMN_STYLE_SIZE, Constant.STYLE_DEFAULT_SIZE);
		editor.putInt(DBSchema.COLUMN_STYLE_MARGIN_WIDTH,
				Constant.STYLE_DEFAULT_MARGIN_WIDTH);
		editor.putInt(DBSchema.COLUMN_STYLE_MARGIN_HEIGHT,
				Constant.STYLE_DEFAULT_MARGIN_HEIGHT);
		editor.putInt(DBSchema.COLUMN_STYLE_LINE_SPACING,
				Constant.STYLE_DEFAULT_LINE_SPACING);
		editor.putString(DBSchema.COLUMN_STYLE_TYPEFACE,
				Constant.STYLE_DEFAULT_TYPEFACE);
		editor.putString(DBSchema.COLUMN_STYLE_FONT_STYLE,
				Constant.STYLE_DEFAULT_FONT_STYLE);
		editor.putInt(DBSchema.COLUMN_STYLE_TEXT_COLOR,
				Constant.STYLE_DEFAULT_TEXT_COLOR);
		editor.putInt(DBSchema.COLUMN_STYLE_BG_COLOR,
				Constant.STYLE_DEFAULT_BG_COLOR);
		editor.putBoolean(DBSchema.COLUMN_STYLE_IS_DEFAULT, true);
		editor.putString(Constant.COLUMN_PAGETURN,
				Constant.STYLE_DEFAULT_PAGETURN);
		editor.commit();
	}

	public static Paint getTextStyle(Context context) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		SharedPreferences style = context.getSharedPreferences(//获取字体样式，存在数据库里面的
				Constant.STYLE_REFERENCE, 0);
		paint.setTextSize(style.getInt(DBSchema.COLUMN_STYLE_SIZE,//获取字体的大小
				Constant.STYLE_DEFAULT_SIZE));
		paint.setColor(style.getInt(DBSchema.COLUMN_STYLE_TEXT_COLOR,//获取字体的颜色
				Constant.STYLE_DEFAULT_TEXT_COLOR));
		String font = style.getString(DBSchema.COLUMN_STYLE_TYPEFACE,//获取字体
				Constant.STYLE_DEFAULT_TYPEFACE);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(),
				"fonts/" + font);
		paint.setTypeface(typeface);
		return paint;
	}

	/**
	 * 得到翻页效果模式
	 * 
	 * @param context
	 * @return
	 */
	public static String getPageTurnMode(Context context) {
		SharedPreferences pageTurnPre = context.getSharedPreferences(
				Constant.STYLE_REFERENCE, 0);
		return pageTurnPre.getString(Constant.COLUMN_PAGETURN,
				Constant.STYLE_DEFAULT_PAGETURN);
	}
}
