package com.qaprosoft.carina.core.foundation.webdriver.metadata;

import java.util.Map;

/**
 * Created by yauhenipatotski on 4/12/17.
 */
public class ElementInfo {

    private String screenshot;

    private Map<String, String> elementsAttributes;

    private Rectangle rectangle;

    public Map<String, String> getElementsAttributes() {
        return elementsAttributes;
    }

    public void setElementsAttributes(Map<String, String> elementsAttributes) {
        this.elementsAttributes = elementsAttributes;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public String getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }
}
