/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.utils.metadata.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

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
