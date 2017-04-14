package com.qaprosoft.carina.core.foundation.utils.metadata.model;

import java.util.Map;

/**
 * Created by yauhenipatotski on 4/14/17.
 */
public class ElementInfo {

    private Map<String, String> elementsAttributes;

    private Rect rect;

    private String elementName;

    public Map<String, String> getElementsAttributes() {
        return elementsAttributes;
    }

    public void setElementsAttributes(Map<String, String> elementsAttributes) {
        this.elementsAttributes = elementsAttributes;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
}
