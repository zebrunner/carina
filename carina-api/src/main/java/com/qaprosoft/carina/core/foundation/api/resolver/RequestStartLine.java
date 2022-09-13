package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;

public class RequestStartLine {

    private String url;
    private HttpMethodType methodType;

    public RequestStartLine(String url, HttpMethodType methodType) {
        this.url = url;
        this.methodType = methodType;
    }

    public RequestStartLine(HttpMethodType methodType) {
        this.methodType = methodType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(HttpMethodType methodType) {
        this.methodType = methodType;
    }
}
