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
