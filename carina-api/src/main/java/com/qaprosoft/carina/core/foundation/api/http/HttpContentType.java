package com.qaprosoft.carina.core.foundation.api.http;

public enum HttpContentType {
    XML("application/xml"),
    JSON("application/json");

    private String contentType;

    HttpContentType(String contentType) {
        this.contentType = contentType;
    }
}
