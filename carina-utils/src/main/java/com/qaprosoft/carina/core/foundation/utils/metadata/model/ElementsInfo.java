package com.qaprosoft.carina.core.foundation.utils.metadata.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yauhenipatotski on 4/14/17.
 */

public class ElementsInfo {

    private String currentURL;

    private ScreenShootInfo screenShootInfo;

    List<ElementInfo> elements = new ArrayList<>();

    public String getCurrentURL() {
        return currentURL;
    }

    public void setCurrentURL(String currentURL) {
        this.currentURL = currentURL;
    }

    public ScreenShootInfo getScreenshot() {
        return screenShootInfo;
    }

    public void setScreenshot(ScreenShootInfo screenShootInfo) {
        this.screenShootInfo = screenShootInfo;
    }

    public List<ElementInfo> getElements() {
        return elements;
    }

    public void addElement(ElementInfo element) {
        if (element != null) {
            elements.add(element);
        }

    }
}
