package com.qaprosoft.carina.core.foundation.api.resolver;

import java.util.Optional;

public class RequestBodyContainer {

    private Object body;
    private boolean json;

    public RequestBodyContainer(Object body, boolean json) {
        this.body = body;
        this.json = json;
    }

    public Optional<Object> getBody() {
        return Optional.ofNullable(body);
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public boolean isJson() {
        return json;
    }

    public void setJson(boolean json) {
        this.json = json;
    }
}
