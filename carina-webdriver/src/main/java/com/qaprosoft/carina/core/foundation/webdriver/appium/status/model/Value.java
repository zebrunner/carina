/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.appium.status.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "isShuttingDown" })
public class Value {

    private Build build;

    private List<String> supportedApps;

    private List<String> supportedDevices;

    private Os os;

    @JsonIgnore
    private boolean isShuttingDown;

    public Os getOs() {
        return os;
    }

    public void setOs(Os os) {
        this.os = os;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public List<String> getSupportedApps() {
        if (supportedApps == null) {
            supportedApps = new ArrayList<>();
        }
        return this.supportedApps;
    }

    public List<String> getSupportedDevices() {
        if (supportedDevices == null) {
            supportedDevices = new ArrayList<>();
        }
        return this.supportedDevices;
    }

    public boolean isShuttingDown() {
        return isShuttingDown;
    }

    public void setShuttingDown(boolean is) {
        this.isShuttingDown = is;
    }
}
