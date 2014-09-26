package com.yt.reader.model;

public class Style {
	private int size;// 字号
	private int marginWidth;// 左右与边缘的距离
	private int marginHeight;// 上下与边缘的距离
	private int lineSpacing;// 行距
	private String typeface;// 字体，如宋体、Serif等
	private String fontStyle;// 字形，如常规、斜体、加粗等
	private int textColor;// 字体颜色
	private int bgColor;// 背景颜色
	private boolean isDefault;// 是否为系统默认设置

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getMarginWidth() {
		return marginWidth;
	}

	public void setMarginWidth(int marginWidth) {
		this.marginWidth = marginWidth;
	}

	public int getMarginHeight() {
		return marginHeight;
	}

	public void setMarginHeight(int marginHeight) {
		this.marginHeight = marginHeight;
	}

	public String getTypeface() {
		return typeface;
	}

	public void setTypeface(String typeface) {
		this.typeface = typeface;
	}

	public String getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}

	public int getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing(int lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public String toString() {
		return "Style [size=" + size + ", marginWidth=" + marginWidth
				+ ", marginHeight=" + marginHeight + ", typeface=" + typeface
				+ ", fontStyle=" + fontStyle + ", lineSpacing=" + lineSpacing
				+ ", textColor=" + textColor + ", bgColor=" + bgColor
				+ ", isDefault=" + isDefault + "]";
	}
}
