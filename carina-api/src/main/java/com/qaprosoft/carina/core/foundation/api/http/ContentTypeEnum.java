package com.qaprosoft.carina.core.foundation.api.http;

public enum ContentTypeEnum {
    JSON(new String[] { "application/json" }),
    XML(new String[] { "application/xml", "text/xml" }),
    NA(new String[] { "n/a" }),;

    public String[] getStringValues() {
        return stringValues;
    }

    private String[] stringValues;

    ContentTypeEnum(String[] stringValues) {
        this.stringValues = stringValues;
    }
}
