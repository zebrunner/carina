package com.qaprosoft.apitools.validation;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;


public class XmlSchemaValidatorTest {

    @Test
    public void testValidateXmlSchemaSuccess1() throws IOException {
        String schema = "src/test/resources/validation/schema/schema_xml/schema.xml";
        String expectedRs = IOUtils.toString(XmlSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema_xml/expected.xml"), Charset.forName("UTF-8").toString());
        XmlValidator.validateXmlAgainstSchema(schema, expectedRs);
    }

    @Test
    public void testValidateXmlSchemaSuccess2() throws IOException {
        String schema = "src/test/resources/validation/schema/schema_xml/schema1.xml";
        String expectedRs = IOUtils.toString(XmlSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema_xml/expected1.xml"), Charset.forName("UTF-8").toString());
        XmlValidator.validateXmlAgainstSchema(schema, expectedRs);
    }

    @Test
    public void testValidateXmlSchemaError() throws IOException {
        String schema = "src/test/resources/validation/schema/schema_xml/error_schema.xml";
        String expectedRs = IOUtils.toString(XmlSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema_xml/expected.xml"), Charset.forName("UTF-8").toString());
        boolean isErrorThrown = false;
        try {
            XmlValidator.validateXmlAgainstSchema(schema, expectedRs);
        } catch (AssertionError e) {
            isErrorThrown = true;
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }
}
