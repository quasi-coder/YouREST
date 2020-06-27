package com.dwiveddi.restapi.utils;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by dwiveddi on 4/23/2018.
 */
public class XmlValidator {
    public static boolean validateXMLBySchema(String xsdPath, String xmlAsString){
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlAsString)));
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Exception while validating with xsd at path = '%s', xml = '%s'", xsdPath, xmlAsString),e);
        }
        return true;
    }
}
