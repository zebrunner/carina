/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
