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
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.api;

import static com.qaprosoft.carina.core.foundation.api.http.Headers.ACCEPT_ALL_TYPES;
import static com.qaprosoft.carina.core.foundation.api.http.Headers.JSON_CONTENT_TYPE;
import static com.qaprosoft.carina.core.foundation.api.http.Headers.XML_CONTENT_TYPE;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Properties;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.apitools.builder.PropertiesProcessorMain;
import com.qaprosoft.apitools.message.TemplateMessage;
import com.qaprosoft.apitools.validation.JsonKeywordsComparator;
import com.qaprosoft.apitools.validation.JsonValidator;
import com.qaprosoft.apitools.validation.XmlValidator;
import com.qaprosoft.carina.core.foundation.api.annotation.ContentType;
import com.qaprosoft.carina.core.foundation.api.annotation.RequestTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.ResponseTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.SuccessfulHttpStatus;

import io.restassured.response.Response;


public abstract class AbstractApiMethodV2 extends AbstractApiMethod {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private Properties properties;
    private String rqPath;
    private String rsPath;
    private String actualRsBody;

    /**
     * When this constructor is called then paths to request and expected response templates are taken from @RequestTemplatePath
     * and @ResponseTemplatePath if present
     */
    public AbstractApiMethodV2() {
        super();
        setHeaders(ACCEPT_ALL_TYPES.getHeaderValue());
        initPathsFromAnnotation();
        setProperties(new Properties());
    }

    public AbstractApiMethodV2(String rqPath, String rsPath, String propertiesPath) {
        super();
        setHeaders(ACCEPT_ALL_TYPES.getHeaderValue());
        setProperties(propertiesPath);
        this.rqPath = rqPath;
        this.rsPath = rsPath;
    }

    public AbstractApiMethodV2(String rqPath, String rsPath, Properties properties) {
        super();
        setHeaders(ACCEPT_ALL_TYPES.getHeaderValue());
        if (properties != null) {
            this.properties = PropertiesProcessorMain.processProperties(properties);
        }
        this.rqPath = rqPath;
        this.rsPath = rsPath;
    }

    private void initPathsFromAnnotation() {
        RequestTemplatePath requestTemplatePath = this.getClass().getAnnotation(RequestTemplatePath.class);
        if (requestTemplatePath != null) {
            this.rqPath = requestTemplatePath.path();
        }
        ResponseTemplatePath responseTemplatePath = this.getClass().getAnnotation(ResponseTemplatePath.class);
        if (responseTemplatePath != null) {
            this.rsPath = responseTemplatePath.path();
        }
    }

    public AbstractApiMethodV2(String rqPath, String rsPath) {
        this(rqPath, rsPath, new Properties());
    }

    /**
     * Sets path to freemarker template for request body
     * 
     * @param path String
     */
    public void setRequestTemplate(String path) {
        this.rqPath = path;
    }

    /**
     * Sets path to freemarker template for expected response body
     * 
     * @param path String
     */
    public void setResponseTemplate(String path) {
        this.rsPath = path;
    }

    @Override
    public Response callAPI() {
        if (rqPath != null) {
            TemplateMessage tm = new TemplateMessage();
            tm.setTemplatePath(rqPath);
            tm.setPropertiesStorage(properties);
            setBodyContent(tm.getMessageText());
        }
        Response rs = super.callAPI();
        actualRsBody = rs.asString();
        return rs;
    }

    /**
     * Calls API expecting http status in response taken from @SuccessfulHttpStatus value
     * 
     * @return restassured Response object
     */
    public Response callAPIExpectSuccess() {
        SuccessfulHttpStatus successfulHttpStatus = this.getClass().getAnnotation(SuccessfulHttpStatus.class);
        if (successfulHttpStatus == null) {
            throw new RuntimeException("To use this method please declare @SuccessfulHttpStatus for your AbstractApiMethod class");
        }
        expectResponseStatus(successfulHttpStatus.status());
        return callAPI();
    }

    /**
     * Sets path to .properties file which stores properties list for declared API method
     * 
     * @param propertiesPath String path to properties file
     */
    public void setProperties(String propertiesPath) {
        URL baseResource = ClassLoader.getSystemResource(propertiesPath);
        if (baseResource != null) {
            properties = new Properties();
            try {
                properties.load(baseResource.openStream());
            } catch (IOException e) {
                throw new RuntimeException("Properties can't be loaded by path: " + propertiesPath, e);
            }
            LOGGER.info("Base properties loaded: " + propertiesPath);
        } else {
            throw new RuntimeException("Properties can't be found by path: " + propertiesPath);
        }
        properties = PropertiesProcessorMain.processProperties(properties);
    }

    /**
     * Sets properties list for declared API method
     * 
     * @param properties Properties object with predefined properties for declared API method
     */
    public void setProperties(Properties properties) {
        this.properties = PropertiesProcessorMain.processProperties(properties);
    }

    public void addProperty(String key, Object value) {
        if (properties == null) {
            throw new RuntimeException("API method properties are not initialized!");
        }
        properties.put(key, value);
    }

    public void removeProperty(String key) {
        if (properties == null) {
            throw new RuntimeException("API method properties are not initialized!");
        }
        properties.remove(key);
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * Validates JSON response using custom options
     * 
     * @param mode
     *            - determines how to compare 2 JSONs. See type description for more details. Mode is not applied for
     *            arrays comparison
     * @param validationFlags
     *            - used for JSON arrays validation when we need to check presence of some array items in result array.
     *            Use JsonCompareKeywords.ARRAY_CONTAINS.getKey() construction for that
     */
    public void validateResponse(JSONCompareMode mode, String... validationFlags) {
        if (rsPath == null) {
            throw new RuntimeException("Please specify rsPath to make Response body validation");
        }
        if (properties == null) {
            properties = new Properties();
        }
        if (actualRsBody == null) {
            throw new RuntimeException("Actual response body is null. Please make API call before validation response");
        }
        TemplateMessage tm = new TemplateMessage();
        tm.setTemplatePath(rsPath);
        tm.setPropertiesStorage(properties);
        String expectedRs = tm.getMessageText();
        try {
            JSONAssert.assertEquals(expectedRs, actualRsBody, new JsonKeywordsComparator(mode, validationFlags));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param validationFlags
     *            parameter that specifies how to validate JSON response. Currently only array validation flag is supported.
     *            Use JsonCompareKeywords.ARRAY_CONTAINS enum value for that
     */
    public void validateResponse(String... validationFlags) {
        validateResponse(JSONCompareMode.NON_EXTENSIBLE, validationFlags);
    }

    /**
     * Validates actual API response per schema (JSON or XML depending on response body type).
     * Annotation {@link ContentType} on your AbstractApiMethodV2 class is used to determine whether to validate JSON or XML.
     * If ContentType is not specified then JSON schema validation will be applied by default.
     * 
     * @param schemaPath Path to schema file in resources
     */
    public void validateResponseAgainstSchema(String schemaPath) {
        if (actualRsBody == null) {
            throw new RuntimeException("Actual response body is null. Please make API call before validation response");
        }
        ContentType contentType = this.getClass().getAnnotation(ContentType.class);
        if (contentType == null || contentType.type().equals(JSON_CONTENT_TYPE.getHeaderValue())) {
            TemplateMessage tm = new TemplateMessage();
            tm.setTemplatePath(schemaPath);
            String schema = tm.getMessageText();
            JsonValidator.validateJsonAgainstSchema(schema, actualRsBody);
        } else if (contentType.type().equals(XML_CONTENT_TYPE.getHeaderValue())) {
            XmlValidator.validateXmlAgainstSchema(schemaPath, actualRsBody);
        } else {
            throw new RuntimeException("Unsupported argument of content type");
        }
    }

    public void setAuth(String jSessionId) {
        addCookie("pfJSESSIONID", jSessionId);
    }
}
