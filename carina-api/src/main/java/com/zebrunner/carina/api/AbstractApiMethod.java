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
package com.zebrunner.carina.api;

import static io.restassured.RestAssured.given;

import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.zebrunner.carina.api.http.HttpResponseStatus;
import com.zebrunner.carina.api.http.HttpResponseStatusType;
import com.zebrunner.carina.api.interceptor.InterceptorChain;
import com.zebrunner.carina.api.resolver.ContextResolverChain;
import com.zebrunner.carina.api.resolver.RequestStartLine;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.xml.HasXPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.zebrunner.carina.api.http.ContentTypeEnum;
import com.zebrunner.carina.api.http.HttpClient;
import com.zebrunner.carina.api.http.HttpMethodType;
import com.zebrunner.carina.api.log.CarinaRequestBodyLoggingFilter;
import com.zebrunner.carina.api.log.CarinaResponseBodyLoggingFilter;
import com.zebrunner.carina.api.log.LoggingOutputStream;
import com.zebrunner.carina.api.ssl.NullHostnameVerifier;
import com.zebrunner.carina.api.ssl.NullX509TrustManager;
import com.zebrunner.carina.api.ssl.SSLContextBuilder;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.zebrunner.carina.utils.R;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractApiMethod extends HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AnnotatedElement anchorElement;
    private final InterceptorChain interceptorChain;

    private StringBuilder bodyContent;
    protected String methodPath;
    protected HttpMethodType methodType;
    protected Object response;
    public RequestSpecification request;
    protected ContentTypeEnum contentTypeEnum;
    private boolean logRequest = Configuration.getBoolean(Parameter.LOG_ALL_JSON);
    private boolean logResponse = Configuration.getBoolean(Parameter.LOG_ALL_JSON);
    private boolean ignoreSSL = Configuration.getBoolean(Parameter.IGNORE_SSL);

    public AbstractApiMethod() {
        this(null);
        getInterceptorChain().onInstantiation();
    }

    AbstractApiMethod(AnnotatedElement anchorElement) {
        this.anchorElement = anchorElement != null
                ? anchorElement
                : this.getClass();

        this.interceptorChain = new InterceptorChain(this, this.anchorElement);

        RequestStartLine requestStartLine = ContextResolverChain.resolveUrl(this.anchorElement)
                .orElseThrow(() -> new RuntimeException("Method type and path are not specified for: " + this.getClass().getSimpleName()));

        this.methodPath = requestStartLine.getUrl();
        this.methodType = requestStartLine.getMethodType();
        this.bodyContent = new StringBuilder();
        this.request = given();

        initContentTypeFromAnnotation();
        replaceUrlPlaceholders();

        ContextResolverChain.resolveQueryParams(anchorElement)
                .ifPresent(queryParams -> request.queryParams(queryParams));
    }

    private void initContentTypeFromAnnotation() {
        String contentType = ContextResolverChain.resolveContentType(this.anchorElement)
                .orElse(ContentTypeEnum.JSON.getMainStringValue());
        this.request.contentType(contentType);
        this.contentTypeEnum = Arrays.stream(ContentTypeEnum.values())
                .filter(type -> ArrayUtils.contains(type.getStringValues(), contentType))
                .findFirst()
                .orElse(ContentTypeEnum.NA);
    }

    private void replaceUrlPlaceholders() {
        final String envParam = "config.env.";
        final String configParam = "config.";
        List<String> params = getParamsFromUrl();
        for (String param : params) {
            if (param != null) {
                if (param.startsWith(envParam)) {
                    String newParam = StringUtils.substringAfter(param, envParam);
                    replaceUrlPlaceholder(param, Configuration.getEnvArg(newParam));
                } else if (param.startsWith(configParam)) {
                    String newParam = StringUtils.substringAfter(param, configParam);
                    replaceUrlPlaceholder(param, R.CONFIG.get(newParam));
                }
            }
        }

        ContextResolverChain.resolvePathParams(anchorElement)
                .ifPresent(pathParams -> pathParams.forEach((key, value) -> replaceUrlPlaceholder(key, value.toString())));
    }

    private List<String> getParamsFromUrl() {
        List<String> params = new ArrayList<>();
        String path = methodPath;
        while (path.contains("{")) {
            String param = StringUtils.substringBetween(path, "${", "}");
            params.add(param);
            path = StringUtils.substringAfter(path, "}");
        }
        return params;
    }

    public void setHeaders(String... headerKeyValues) {
        for (String headerKeyValue : headerKeyValues) {
            String key = headerKeyValue.split("=", 2)[0];
            String value = headerKeyValue.split("=", 2)[1];
            setHeader(key, value);
        }
    }

    public void setHeader(String key, Object value) {
        request.header(key, value);
    }

    public void addUrlParameter(String key, String value) {
        if (value != null) {
            request.queryParam(key, value);
        }
    }

    public void addParameter(String key, String value) {
        request.param(key, value.replace(" ", "%20"));
    }

    public void addParameterIfNotNull(String key, String value) {
        if (value != null) {
            this.addParameter(key, value);
        }
    }

    public void addBodyParameter(String key, Object value) {
        if (bodyContent.length() != 0) {
            bodyContent.append("&");
        }
        bodyContent.append(key + "=" + value);
    }

    protected void addBodyParameterIfNotNull(String key, Object value) {
        if (value != null) {
            addBodyParameter(key, value);
        }
    }

    public void addCookie(String key, String value) {
        request.given().cookie(key, value);
    }

    public void addCookies(Map<String, ?> cookies) {
        request.given().cookies(cookies);
    }

    public void replaceUrlPlaceholder(String placeholder, String value) {
        if (value != null) {
            methodPath = methodPath.replace("${" + placeholder + "}", value);
        } else {
            methodPath = methodPath.replace("${" + placeholder + "}", "");
            methodPath = StringUtils.removeEnd(methodPath, "/");
        }
    }

    public void expectResponseStatus(HttpResponseStatusType status) {
        expectResponseStatus(status.getResponseStatus());
    }

    public void expectResponseStatus(HttpResponseStatus status) {
        request.expect().statusCode(status.getCode());
        request.expect().statusLine(Matchers.containsString(status.getMessage()));
    }

    public <T> void expectResponseContains(Matcher<T> key, Matcher<T> value) {
        request.expect().body(key, value);
    }

    public void expectValueByXpath(String xPath, String value) {
        request.expect().body(Matchers.hasXPath(xPath), Matchers.containsString(value));
    }

    public void expectValueByXpath(String xPath, String value1, String value2) {
        request.expect().body(Matchers.hasXPath(xPath), Matchers.anyOf(Matchers.containsString(value1), Matchers.containsString(value2)));
    }

    public <T> void expectResponseContains(Matcher<T> value) {
        request.expect().body(value);
    }

    public <T> void expectResponseContains(String key, Matcher<T> value) {
        request.expect().body(key, value);
    }

    public <T> void expectResponseContainsXpath(String xPath) {
        request.expect().body(HasXPath.hasXPath(xPath));
    }

    private void initLogging(PrintStream ps) {
        if (logRequest) {
            Set<String> headers = ContextResolverChain.resolveHiddenRequestHeadersInLogs(this.anchorElement)
                    .orElse(Collections.emptySet());
            RequestLoggingFilter fHeaders = new RequestLoggingFilter(LogDetail.HEADERS, true, ps, true, headers);

            RequestLoggingFilter fCookies = new RequestLoggingFilter(LogDetail.COOKIES, ps);
            RequestLoggingFilter fParams = new RequestLoggingFilter(LogDetail.PARAMS, ps);
            RequestLoggingFilter fMethod = new RequestLoggingFilter(LogDetail.METHOD, ps);
            RequestLoggingFilter fUri = new RequestLoggingFilter(LogDetail.URI, ps);

            RequestLoggingFilter fBody = ContextResolverChain.resolveHiddenRequestBodyPartsInLogs(this.anchorElement)
                    .<RequestLoggingFilter>map(paths -> new CarinaRequestBodyLoggingFilter(true, ps, paths, contentTypeEnum))
                    .orElseGet(() -> new RequestLoggingFilter(LogDetail.BODY, ps));
            request.filters(fMethod, fUri, fParams, fCookies, fHeaders, fBody);
        }

        if (logResponse) {
            ResponseLoggingFilter fStatus = new ResponseLoggingFilter(LogDetail.STATUS, ps);
            ResponseLoggingFilter fHeaders = new ResponseLoggingFilter(LogDetail.HEADERS, ps);
            ResponseLoggingFilter fCookies = new ResponseLoggingFilter(LogDetail.COOKIES, ps);

            ResponseLoggingFilter fBody = ContextResolverChain.resolveHiddenResponseBodyPartsInLogs(this.anchorElement)
                    .<ResponseLoggingFilter>map(paths -> new CarinaResponseBodyLoggingFilter(true, ps, Matchers.any(Integer.class), paths, contentTypeEnum))
                    .orElseGet(() -> new ResponseLoggingFilter(LogDetail.BODY, ps));
            request.filters(fBody, fCookies, fHeaders, fStatus);
        }
    }

    public Response callAPI() {
        return callAPI(new LoggingOutputStream(LOGGER, Level.INFO));
    }

    Response callAPI(LoggingOutputStream outputStream) {
        if (ignoreSSL) {
            ignoreSSLCerts();
        }

        if (bodyContent.length() != 0) {
            request.body(bodyContent.toString());
        }

        PrintStream ps = null;
        if (logRequest || logResponse) {
            ps = new PrintStream(outputStream);
            initLogging(ps);
        }

        getInterceptorChain().onBeforeCall();

        Response rs;
        try {
            rs = HttpClient.send(request, methodPath, methodType);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }

        getInterceptorChain().onAfterCall();

        return rs;
    }

    public void expectInResponse(Matcher<?> matcher) {
        request.expect().body(matcher);
    }

    public void expectInResponse(String locator, Matcher<?> value) {
        request.expect().body(locator, value);
    }

    public String getMethodPath() {
        return methodPath;
    }

    public void setMethodPath(String methodPath) {
        RestAssured.reset();
        this.methodPath = methodPath;
    }

    public void setBodyContent(String content) {
        this.bodyContent = new StringBuilder(content);
    }

    public RequestSpecification getRequest() {
        return request;
    }

    public String getRequestBody() {
        return bodyContent.toString();
    }
    
    public Object getResponse() {
        return response;
    }

    public void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }

    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }

    public void ignoreSSLCerts() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        TrustManager[] trustManagerArray = { new NullX509TrustManager() };
        try {
            sslContext.init(null, trustManagerArray, null);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }

        SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, new NullHostnameVerifier());
        SSLConfig sslConfig = new SSLConfig();
        sslConfig = sslConfig.sslSocketFactory(socketFactory);
        sslConfig = sslConfig.x509HostnameVerifier(new NullHostnameVerifier());

        RestAssuredConfig cfg = new RestAssuredConfig();
        cfg = cfg.sslConfig(sslConfig);
        request = request.config(cfg);
    }

    public void setSSLContext(SSLContext sslContext) {
        SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext);
        SSLConfig sslConfig = new SSLConfig();
        sslConfig = sslConfig.sslSocketFactory(socketFactory);

        RestAssuredConfig cfg = new RestAssuredConfig();
        cfg = cfg.sslConfig(sslConfig);
        request = request.config(cfg);
    }

    public void setDefaultTLSSupport() {
        setSSLContext(new SSLContextBuilder(true).createSSLContext());
    }

    protected AnnotatedElement getAnchorElement() {
        return anchorElement;
    }

    InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }
}
