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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

/**
 * Class wrapper for RequestFilter. Rewrite rules can be configured as separate
 * Java Beans and can be passed into this class. Fitler's will be applied.
 *
 */
public class CustomRqFilter implements RequestFilter {
    
    private static final Logger LOGGER = Logger.getLogger(CustomRqFilter.class);
    
    private List<RewriteItem> rewrites = new ArrayList<>();

    @Override
    public HttpResponse filterRequest(HttpRequest rq, HttpMessageContents contents, HttpMessageInfo messageInfo) {
        if (rewrites.isEmpty()) {
            return null;
        }
        String reqUrl = rq.getUri();
        for (RewriteItem rewriteItem : rewrites) {
            if(reqUrl.matches(rewriteItem.getHost())) {
                // headers rewrite
                LOGGER.debug("Rewrite rule will be applied for host: ".concat(reqUrl));
                rq = applyHeaders(rq, rewriteItem.getHeaders());

                // body rewrite
                String content = contents.getTextContents();
                content.replaceAll(rewriteItem.getRegex(), rewriteItem.getReplacement());
                contents.setTextContents(content);
            }
        }
        
        return null;
    }
    
    
    /**
     * Apply headers to request
     * @param req
     * @param headers
     * @return updated request
     */
    private HttpRequest applyHeaders(HttpRequest req, List<HeaderItem> headers) {
        for (HeaderItem headerItem : headers) {
            switch (headerItem.getMethod()) {
            case ADD:
                req.headers().add(headerItem.getHeader().getKey(), headerItem.getHeader().getValue());
                break;
            case REMOVE:
                req.headers().remove(headerItem.getHeader().getKey());
                break;
            case UPDATE:
                req.headers().set(headerItem.getHeader().getKey(), headerItem.getHeader().getValue());
                break;
            default:
                break;
            }
        }
        return req;
    }
    
    public CustomRqFilter (List<RewriteItem> rewrites) {
        this.rewrites = rewrites;
    }

}
