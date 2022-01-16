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

package com.qaprosoft.carina.core.foundation.api.log;

import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.qaprosoft.carina.core.foundation.api.http.ContentTypeEnum;

import groovy.xml.XmlParser;
import io.restassured.internal.path.json.JsonPrettifier;
import io.restassured.internal.path.xml.XmlPrettifier;
import io.restassured.response.ResponseBody;
import io.restassured.specification.FilterableRequestSpecification;

public class CarinaBodyPrinter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String HIDDEN_PATTERN = "****************";

    private static final String NONE = "<none>";
    private static final String TAB = "\t";

    private static final Configuration JSON_PARSE_CFG = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider()).build();

    /**
     * Prints the response to the print stream
     *
     * @param responseBody ResponseBody&lt;?&gt;
     *
     * @param stream PrintStream
     *
     * @param shouldPrettyPrint boolean
     *
     * @param hiddenPaths Set&lt;String&gt;
     *
     * @param contentType ContentTypeEnum
     * @return A string of representing the response
     */
    public static String printResponseBody(ResponseBody<?> responseBody, PrintStream stream, boolean shouldPrettyPrint, Set<String> hiddenPaths,
            ContentTypeEnum contentType) {
        final StringBuilder builder = new StringBuilder();
        String responseBodyToAppend = new String(responseBody.asString());

        // replace values by paths
        responseBodyToAppend = replaceValues(responseBodyToAppend, hiddenPaths, contentType);

        // pretty print
        if (shouldPrettyPrint) {
            builder.append(prettify(responseBodyToAppend, contentType));
        } else {
            builder.append(responseBodyToAppend);
        }

        String response = builder.toString();
        stream.println(response);
        return response;
    }

    /**
     * Prints the request to the print stream
     *
     * @param requestSpec FilterableRequestSpecification
     *
     * @param stream PrintStream
     *
     * @param shouldPrettyPrint boolean
     *
     * @param hiddenPaths Set&lt;String&gt;
     *
     * @param contentType ContentTypeEnum
     */
    public static void printRequestBody(FilterableRequestSpecification requestSpec, PrintStream stream, boolean shouldPrettyPrint,
            Set<String> hiddenPaths, ContentTypeEnum contentType) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Body:");
        if (requestSpec.getBody() != null) {
            String body = new String((String) requestSpec.getBody());

            // replace values by paths
            body = replaceValues(body, hiddenPaths, contentType);

            // pretty print
            if (shouldPrettyPrint) {
                body = prettify(body, contentType);
            }
            builder.append(SystemUtils.LINE_SEPARATOR).append(body);
        } else {
            appendTab(appendTab(appendTab(builder))).append(NONE);
        }
        String response = builder.toString();
        stream.println(response);
    }

    private static String replaceValues(String body, Set<String> hiddenPaths, ContentTypeEnum contentType) {
        if (!hiddenPaths.isEmpty() && body != null && !StringUtils.isEmpty(body)) {
            switch (contentType) {
            case JSON:
                for (String p : hiddenPaths) {
                    body = JsonPath.using(JSON_PARSE_CFG).parse(body).set(p, HIDDEN_PATTERN).jsonString();
                }
                break;
            case XML:
                for (String p : hiddenPaths) {
                    try {
                        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder;
                        builder = builderFactory.newDocumentBuilder();

                        InputSource is = new InputSource(new StringReader(body));
                        Document xmlDocument = builder.parse(is);
                        XPath xpath = XPathFactory.newInstance().newXPath();
                        NodeList myNodeList = (NodeList) xpath.compile(p).evaluate(xmlDocument, XPathConstants.NODESET);
                        for (int i = 0; i < myNodeList.getLength(); i++) {
                            myNodeList.item(i).setNodeValue(HIDDEN_PATTERN);
                        }

                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer transformer = tf.newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        StringWriter writer = new StringWriter();
                        transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));
                        body = writer.getBuffer().toString().replaceAll("\n|\r", "");
                    } catch (Exception e) {
                        LOGGER.warn("Exception during parsing XML", e);
                    }
                }
                break;
            default:
                LOGGER.warn(String.format("Content type '%s' is not supported for body parts hiding in logs", contentType));
            }
        }
        
        return body;
    }

    private static String prettify(String body, ContentTypeEnum contentType) {
        String prettifiedBody;
        try {
            switch (contentType) {
            case JSON:
                prettifiedBody = JsonPrettifier.prettifyJson(body);
                break;
            case XML:
                prettifiedBody = XmlPrettifier.prettify(new XmlParser(false, false), body);
                break;
            default:
                prettifiedBody = body;
                break;
            }
        } catch (Exception e) {
            // Parsing failed, probably because the content was not of expected type.
            prettifiedBody = body;
        }
        return prettifiedBody;
    }

    private static StringBuilder appendTab(StringBuilder builder) {
        return builder.append(TAB);
    }

}
