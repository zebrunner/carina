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
package com.zebrunner.carina.api.apitools.validation;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.api.mock.apimethod.NoContentTypeMethod;
import com.zebrunner.carina.api.mock.apimethod.XmlContentTypeMethod;
import com.zebrunner.carina.api.mock.server.MockServer;

public class DiffContentTypesValidatorTest {

    private MockServer server;

    @BeforeSuite
    public void up() {
        server = new MockServer();
        server.start();
        
        //override api_url using dynamic port
        R.CONFIG.put("DEMO.api_url", server.getBaseUrl());
        //configure client to use dynamic port 
        WireMock.configureFor(server.getPort());
    }

    @Test
    public void testValidationNoContentTypeMethodSuccess() throws IOException {
        String actualJsonData = Files.lines(Path.of("src/test/resources/validation/array/duplicate/array_act.json"))
                .collect(Collectors.joining("\n"));
        server.createResponse("/mock1", actualJsonData);
        NoContentTypeMethod noContentTypeMethod = new NoContentTypeMethod();
        noContentTypeMethod.callAPI();
        noContentTypeMethod.validateResponse();
    }

    @Test
    public void testValidationXmlContentTypeMethodSuccess() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/object/actual_res.xml"))
                .collect(Collectors.joining("\n"));
        server.createResponse("/mock2", actualXmlData);
        XmlContentTypeMethod getUserMethod = new XmlContentTypeMethod();
        getUserMethod.callAPI();
        getUserMethod.validateResponse();
    }

    @Test
    public void testValidationXmlContentTypeMethodError() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/object/error_res.xml"))
                .collect(Collectors.joining("\n"));
        String expectedError = Files.lines(Path.of("src/test/resources/validation/xml_file/error_message/users_method_error.xml"))
                .collect(Collectors.joining("\n"));
        server.createResponse("/mock2", actualXmlData);
        XmlContentTypeMethod getUserMethod = new XmlContentTypeMethod();
        getUserMethod.callAPI();
        boolean isErrorThrown = false;
        try {
            getUserMethod.validateResponse();
        } catch (AssertionError e) {
        isErrorThrown = true;

        Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError),
                "Error message not as expected");
    }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }

    @AfterSuite
    public void down() {
        server.stop();
    }
}
