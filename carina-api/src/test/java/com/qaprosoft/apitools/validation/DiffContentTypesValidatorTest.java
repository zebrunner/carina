package com.qaprosoft.apitools.validation;

import com.qaprosoft.apitools.validation.mock.method.MockServer;
import com.qaprosoft.apitools.validation.mock.method.NoContentTypeMethod;
import com.qaprosoft.apitools.validation.mock.method.XmlContentTypeMethod;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;

public class DiffContentTypesValidatorTest {

    private MockServer server;

    @BeforeSuite
    public void up() {
        server = new MockServer();
        server.start();
    }

    @Test
    public void testValidationNoContentTypeMethodSuccess() throws IOException {
        String actualJsonData = Files.lines(Path.of("src/test/resources/validation/array/duplicate/array_act.json"))
                .collect(Collectors.joining("\n"));
        MockServer server = new MockServer();
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
