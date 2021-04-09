package com.torutk.gadget.support;

class Configuration {
    static final int MIN_WIDTH = 128;
    static final int MIN_HEIGHT = 128;

    private double x;
    private double y;
    private double width = MIN_WIDTH;
    private double height = MIN_HEIGHT;
    private boolean alwaysOnTop;

    Configuration x(double v) {
        x = v;
        return this;
    }

    double x() {
        return x;
    }

    Configuration y(double v) {
        y = v;
        return this;
    }

    double y() {
        return y;
    }

    Configuration width(double v) {
        width = v;
        return this;
    }

    double width() {
        return width;
    }

    Configuration height(double v) {
        height = v;
        return this;
    }

    double height() {
        return height;
    }

    Configuration alwaysOnTop(boolean v) {
        alwaysOnTop = v;
        return this;
    }

    boolean alwaysOnTop() {
        return alwaysOnTop;
    }
}
