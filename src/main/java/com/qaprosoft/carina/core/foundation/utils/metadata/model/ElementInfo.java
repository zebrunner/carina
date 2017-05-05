package com.qaprosoft.carina.core.foundation.utils.metadata.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Created by yauhenipatotski on 4/14/17.
 */
public class ElementInfo {

    private Map<String, String> elementsAttributes;

    private Rect rect;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ElementInfo textInfo;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ElementInfo getTextInfo() {
        return textInfo;
    }

    public void setTextInfo(ElementInfo textInfo) {
        this.textInfo = textInfo;
    }
}
