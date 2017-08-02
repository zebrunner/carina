package com.qaprosoft.carina.core.foundation.utils.metadata.model;

/**
 * Created by yauhenipatotski on 4/16/17.
 */
public class ScreenShootInfo {

    private String screenshotPath;
    public int width;
    public int height;

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
