/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils.marshaller;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

public class MarshallerHelper {
    /**
     * Marshaller/Unmarshaller implementation
     */
    private static final Marshaller marshaller = Marshaller.getInstance();

    public static void marshall(Object jaxbElement, Result paramResult) {
        marshaller.marshall(jaxbElement, paramResult);
    }

    /**
     * Serializes JAXBElement into File
     * 
     * @param jaxbElement
     *            - JAXBElement
     * @param resultFile
     *            - File
     */
    public static void marshall(Object jaxbElement, File resultFile) {
        marshall(jaxbElement, new StreamResult(resultFile));
    }

    /**
     * Serializes JAXBElement to OutputStream
     * 
     * @param jaxbElement
     *            - JAXBElement
     * @param os
     *            - OutputStream
     */
    public static void marshall(Object jaxbElement, OutputStream os) {
        marshall(jaxbElement, new StreamResult(os));
    }

    /**
     * Serializes JAXBElement to String
     * 
     * @param jaxbElement
     *            - JAXBElement
     * @return String
     */
    public static String marshall(Object jaxbElement) {
        return marshaller.marshall(jaxbElement);
    }

    /**
     * Create JAXBElement from Source
     * 
     * @param <T> Generic
     * @param source Source
     * @param resultClazz expected class
     * 
     * @return T &lt;T&gt;
     */
    public static <T> T unmarshall(Source source, Class<T> resultClazz) {
        return marshaller.unmarshall(source, resultClazz);
    }

    /**
     * Create JAXBElement from File
     * 
     * @param <T> Generic
     * @param file File
     * @param resultClazz expected class
     * 
     * @return T &lt;T&gt;
     */
    public static <T> T unmarshall(File file, Class<T> resultClazz) {
        return marshaller.unmarshall(file, resultClazz);
    }

    /**
     * Create JAXBElement from File
     * 
     * @param <T> Generic
     * @param is Input Stream
     * @param resultClazz expected class
     * 
     * @return T &lt;T&gt;
     */
    public static <T> T unmarshall(InputStream is, Class<T> resultClazz) {
        return marshaller.unmarshall(is, resultClazz);
    }

    public static <T> T unmarshall(String string, Class<T> resultClazz) {
        return marshaller.unmarshall(string, resultClazz);
    }

    /**
     * Serializes JAXBElement into Writer
     * 
     * @param jaxbElement jaxbElement
     * @param writer writer
     */
    public static void marshall(Object jaxbElement, Writer writer) {
        marshaller.marshall(jaxbElement, writer);
    }
}
