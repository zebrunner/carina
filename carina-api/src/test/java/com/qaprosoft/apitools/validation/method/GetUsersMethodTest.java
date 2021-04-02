package com.qaprosoft.apitools.validation.method;

import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;

public class GetUsersMethodTest {

    private MockServer server;

    @BeforeSuite
    public void up() {
        server = new MockServer();
        server.start();
    }

    @Test
    public void testValidateResponseSuccess() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/object/actual_res.xml"))
                .collect(Collectors.joining("\n"));
        server.createResponse("/users", actualXmlData);
        GetUsersMethod getUserMethod = new GetUsersMethod();
        getUserMethod.callAPI();
        getUserMethod.validateResponse();
    }

    @Test
    public void testValidateResponseError() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/object/error_res.xml"))
                .collect(Collectors.joining("\n"));
        String expectedError = Files.lines(Path.of("src/test/resources/validation/xml_file/error_message/getUsersMethodErrorMessage.txt"))
                .collect(Collectors.joining("\n"));
        server.createResponse("/users", actualXmlData);
        GetUsersMethod getUserMethod = new GetUsersMethod();
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
