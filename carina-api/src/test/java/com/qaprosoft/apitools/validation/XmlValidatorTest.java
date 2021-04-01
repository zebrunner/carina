package com.qaprosoft.apitools.validation;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;

public class XmlValidatorTest {

    @Test
    public void testValidateXmlSuccess() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/actual.xml"))
                .collect(Collectors.joining("\n"));
        XmlValidator.validateXml(actualXmlData,
                "src/test/resources/validation/xml_file/expected.xml", XmlCompareMode.STRICT);
    }

    @Test
    public void testValidateXmlSuccess2() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/actual1.xml"))
                .collect(Collectors.joining("\n"));
        XmlValidator.validateXml(actualXmlData,
                "src/test/resources/validation/schema/schema_xml/expected1.xml", XmlCompareMode.STRICT);
    }

    @Test
    public void testValidateXmlError() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/actual_error.xml"))
                .collect(Collectors.joining("\n"));
        String expectedError = Files.lines(Path.of("src/test/resources/validation/xml_file/error_message/error.xml"))
                .collect(Collectors.joining("\n"));
        boolean isErrorThrown = false;
        try {
            XmlValidator.validateXml(actualXmlData, "src/test/resources/validation/xml_file/expected.xml",
                    XmlCompareMode.STRICT);
        } catch (AssertionError e) {
            isErrorThrown = true;
            System.out.println(e.getMessage());
            Assert.assertTrue(normalizeSpace(e.getMessage()).contains(normalizeSpace(expectedError)),
                    "Error message not as expected");
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }

    @Test
    public void testValidateXmlNotStrictOrderSuccess() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/actual_order.xml"))
                .collect(Collectors.joining("\n"));
        XmlValidator.validateXml(actualXmlData, "src/test/resources/validation/xml_file/actual.xml",
                XmlCompareMode.NON_STRICT);
    }

    @Test
    public void testValidateNonExtensibleError() throws IOException {
        String expectedXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/expected_extensible.xml"))
                .collect(Collectors.joining("\n"));
        String expectedError = Files.lines(Path.of("src/test/resources/validation/xml_file/error_message/error_extensible.xml"))
                .collect(Collectors.joining("\n"));
        boolean isErrorThrown = false;
        try {
            XmlValidator.validateXml(expectedXmlData,
                    "src/test/resources/validation/xml_file/actual.xml", XmlCompareMode.NON_STRICT);
        } catch (AssertionError e) {
            isErrorThrown = true;
            Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError),
                    "Error message not as expected");
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }
}
