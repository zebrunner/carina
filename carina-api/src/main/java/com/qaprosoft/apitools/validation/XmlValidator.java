package com.qaprosoft.apitools.validation;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;


public class XmlValidator {
    private static final Logger LOGGER = Logger.getLogger(XmlValidator.class);

    private XmlValidator() {
    }

    public static void validateXmlAgainstSchema(String xmlSchemaPath, String xmlData) {

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = schemaFactory.newSchema(new File(xmlSchemaPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlData)));
        } catch (SAXException e) {
            throw new AssertionError("Validation against Xml schema failed "  + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Can't read xml from String: " + e.getMessage(), e);
        }
        LOGGER.info("Validation against Xml schema successfully passed");
    }

}

