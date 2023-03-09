/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.api.resolver;

import com.zebrunner.carina.api.http.HttpMethodType;

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
