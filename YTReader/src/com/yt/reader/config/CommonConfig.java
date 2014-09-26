
package com.yt.reader.config;

import android.graphics.Color;

public class CommonConfig {
    private Orientation orientation;

    private Animation animation;

    private boolean isNightMode;

    private Theme theme;

    private Margin margin;

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public boolean isNightMode() {
        return isNightMode;
    }

    public void setNightMode(boolean isNightMode) {
        this.isNightMode = isNightMode;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Margin getMargin() {
        return margin;
    }

    public void setMargin(Margin margin) {
        this.margin = margin;
    }

    public enum Orientation {
        Landscape, Portrait, Sensor
    }

    public enum Animation {
        Slide, Fade, None
    }

    public class Theme {
        private Color bgColor;

        private Color fgColor;

        public Color getBgColor() {
            return bgColor;
        }

        public void setBgColor(Color bgColor) {
            this.bgColor = bgColor;
        }

        public Color getFgColor() {
            return fgColor;
        }

        public void setFgColor(Color fgColor) {
            this.fgColor = fgColor;
        }

    }

    public class Margin {
        private int top;

        private int bottom;

        private int left;

        private int right;

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

    }

}
