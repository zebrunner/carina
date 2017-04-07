package com.qaprosoft.carina.core.foundation.webdriver.appium.status.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

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
