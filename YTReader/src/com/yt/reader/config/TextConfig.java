
package com.yt.reader.config;

import android.graphics.Color;

public class TextConfig {
    private int fontSize;

    private int lineSpace;

    private int paraSpace;

    private int wordSpace;

    private int fontFamily;

    private int luminance;

    private int firstLineIndent;

    private Color fgColor;

    private Color bgColor;

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(int lineSpace) {
        this.lineSpace = lineSpace;
    }

    public int getParaSpace() {
        return paraSpace;
    }

    public void setParaSpace(int paraSpace) {
        this.paraSpace = paraSpace;
    }

    public int getWordSpace() {
        return wordSpace;
    }

    public void setWordSpace(int wordSpace) {
        this.wordSpace = wordSpace;
    }

    public int getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(int fontFamily) {
        this.fontFamily = fontFamily;
    }

    public int getLuminance() {
        return luminance;
    }

    public void setLuminance(int luminance) {
        this.luminance = luminance;
    }

    public int getFirstLineIndent() {
        return firstLineIndent;
    }

    public void setFirstLineIndent(int firstLineIndent) {
        this.firstLineIndent = firstLineIndent;
    }

    public Color getFgColor() {
        return fgColor;
    }

    public void setFgColor(Color fgColor) {
        this.fgColor = fgColor;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public final int[] FontSize = {
            12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72
    };

    public enum Hyphenation {// 连词符
        True, False
    }

    public enum FontFamily {
        Sans, SansFallback, Mono, Serif
    }

    public enum FontType {
        Normal, Italic, Bold, ItalicBold
    }

    public enum Alignment {
        Left, Rigth, Center, Justify
    }
}
