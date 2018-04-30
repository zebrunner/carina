package com.qaprosoft.carina.browsermobproxy.rewrite;

import org.apache.commons.lang3.tuple.Pair;

public class HeaderItem {
    
    private HeaderMethod method;
    
    private Pair<String, String> header;
    
    public HeaderItem (final HeaderMethod method, final Pair<String, String> header) {
        this.method = method;
        this.header = header;
    }

    public HeaderMethod getMethod() {
        return method;
    }

    public void setMethod(HeaderMethod method) {
        this.method = method;
    }

    public Pair<String, String> getHeader() {
        return header;
    }

    public void setHeader(Pair<String, String> header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return "HeaderItem [method=" + method + ", header=" + header + "]";
    }

}
