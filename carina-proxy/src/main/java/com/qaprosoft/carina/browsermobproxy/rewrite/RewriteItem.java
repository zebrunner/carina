/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
 ******************************************************************************/
package com.qaprosoft.carina.browsermobproxy.rewrite;

import java.util.ArrayList;
import java.util.List;

public class RewriteItem {

    private String host;

    private String regex;

    private String replacement;

    private List<HeaderItem> headers = new ArrayList<HeaderItem>();

    /**
     * Leave regexp empty in case you don't want to rewrite body
     * 
     * @param host String
     * @param regexp String
     * @param replacement String
     * @param headers List
     */
    public RewriteItem(final String host, final String regexp, final String replacement, final List<HeaderItem> headers) {
        this.host = host;
        this.regex = regexp;
        this.replacement = replacement;
        this.headers = headers;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public List<HeaderItem> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HeaderItem> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "RewriteItem [host=" + host + ", regex=" + regex + ", replacement=" + replacement + ", headers=" + headers + "]";
    }

}
