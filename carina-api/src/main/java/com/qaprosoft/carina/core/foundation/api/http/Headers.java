package com.qaprosoft.carina.core.foundation.api.http;

public enum Headers {
    ACCEPT_ALL_TYPES("Accept=*/*"),
    JSON_CONTENT_TYPE("application/json"),
    XML_CONTENT_TYPE("application/xml");

    public String getHeaderValue() {
        return headerValue;
    }

    private String headerValue;

    Headers(String headerValue) {
        this.headerValue = headerValue;
    }
}
