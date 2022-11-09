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

package com.zebrunner.carina.utils;

import com.zebrunner.carina.utils.marshaller.MarshallerHelper;
import com.zebrunner.carina.utils.marshaller.exception.ParserException;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.annotation.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class XmlUtilsTest {

    private static final String XML_PATH = "src/test/resources/xml/testXml.xml";
    private static final String XML_FORMATTER_PATH = "src/test/resources/xml/testFormatterXml.xml";

    private static final Member MEMBER1 = new Member("Molecule Man", 29);
    private static final Member MEMBER2 = new Member("Madame Uppercut", 39);
    private static final Member MEMBER3 = new Member("Eternal Flame", 25);

    // Person is class without @XmlRootElement
    private static final Person PERSON = new Person("Jhon Eniston");
    private static final City CITY = new City("Metro City", 2016, true, new Members(Arrays.asList(MEMBER1, MEMBER2, MEMBER3)));

    private static final String WRONG_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><city><name>Metro City</name>";


    @Test
    public void testParserExceptionWithText() {
        try {
            throw new ParserException("Can't parse");
        } catch (ParserException e) {
            Assert.assertEquals(e.getMessage(), "Can't parse", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testParserExceptionWithTextAndThrowable() {
        try {
            throw new ParserException("Can't parse", new RuntimeException());
        } catch (ParserException e) {
            Assert.assertEquals(e.getMessage(), "Can't parse", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testXmlFormatter() {
        String xmlStr = MarshallerHelper.marshall(CITY);

        String actualFormatterXmlStr = XmlFormatter.prettyPrint(xmlStr);
        String expectedFormatterXmlStr = readFile(XML_FORMATTER_PATH);

        Assert.assertEquals(actualFormatterXmlStr, expectedFormatterXmlStr, "Xml string wasn't formatted properly");
    }

    @Test
    public void testXmlFormatterWithEmptyXml() {
        String actualFormatterXmlStr = XmlFormatter.prettyPrint("");

        Assert.assertEquals(actualFormatterXmlStr, "", "Xml string isn't empty");
    }

    @Test
    public void testXmlFormatterWithWrongXml() {
        String actualFormatterXmlStr = XmlFormatter.prettyPrint(WRONG_XML);

        Assert.assertEquals(actualFormatterXmlStr, WRONG_XML, "Wrong xml string was formatted");
    }

    private String readFile(String pathStr) {
        Path path = Paths.get(pathStr);

        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException ex) {
            // Handle exception
        }
        return new String(bytes, StandardCharsets.UTF_16);
    }

    @Test
    public void testMarshallUnmarshall() {
        String cityXmlStr = MarshallerHelper.marshall(CITY);

        City actualCity = MarshallerHelper.unmarshall(cityXmlStr, City.class);

        Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testUnmarshallThrowRuntimeException() {
        String cityXmlStr = MarshallerHelper.marshall(CITY);

        MarshallerHelper.unmarshall(cityXmlStr, Member.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testMarshallThrowRuntimeException() {
        MarshallerHelper.marshall(PERSON);
    }
    
    @Test
    public void testMarshallUnmarshallFile() {
        File xmlFile = new File(XML_PATH);

        MarshallerHelper.marshall(CITY, xmlFile);

        City actualCity = MarshallerHelper.unmarshall(xmlFile, City.class);

        Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testUnmarshallFileThrowRuntimeException() {
        File xmlFile = new File(XML_PATH);

        MarshallerHelper.marshall(CITY, xmlFile);

        MarshallerHelper.unmarshall(xmlFile, Member.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testMarshallFileThrowRuntimeException() {
        File xmlFile = new File(XML_PATH);

        MarshallerHelper.marshall(PERSON, xmlFile);
    }

    @Test
    public void testMarshallUnmarshallInputStream() {
        File xmlFile = new File(XML_PATH);

        try {
            OutputStream fos = new FileOutputStream(xmlFile);

            MarshallerHelper.marshall(CITY, fos);

            InputStream fis = new FileInputStream(xmlFile);

            City actualCity = MarshallerHelper.unmarshall(fis, City.class);

            Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
        } catch (FileNotFoundException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testUnmarshallInputStreamThrowRuntimeException() {
        File xmlFile = new File(XML_PATH);

        try {
            OutputStream fos = new FileOutputStream(xmlFile);

            MarshallerHelper.marshall(CITY, fos);

            InputStream fis = new FileInputStream(xmlFile);

            MarshallerHelper.unmarshall(fis, Member.class);
        } catch (FileNotFoundException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testMarshallOutputStreamThrowRuntimeException() {
        File xmlFile = new File(XML_PATH);

        try {
            OutputStream fos = new FileOutputStream(xmlFile);

            MarshallerHelper.marshall(PERSON, fos);
        } catch (FileNotFoundException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test
    public void testMarshallUnmarshallSource() {
        File xmlFile = new File(XML_PATH);

        MarshallerHelper.marshall(CITY, xmlFile);

        Source source = new StreamSource(xmlFile);

        City actualCity = MarshallerHelper.unmarshall(source, City.class);

        Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testUnmarshallSourceThrowRuntimeException() {
        File xmlFile = new File(XML_PATH);

        MarshallerHelper.marshall(CITY, xmlFile);

        Source source = new StreamSource(xmlFile);

        MarshallerHelper.unmarshall(source, Member.class);
    }

    @Test
    public void testMarshallUnmarshallWriter() {
        File xmlFile = new File(XML_PATH);

        try {
            Writer writer = new FileWriter(xmlFile);

            MarshallerHelper.marshall(CITY, writer);

            City actualCity = MarshallerHelper.unmarshall(xmlFile, City.class);

            Assert.assertEquals(actualCity, CITY, actualCity.getName() + " is different than " + CITY.getName());
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testMarshallWriterThrowRuntimeException() {
        File xmlFile = new File(XML_PATH);

        try {
            Writer writer = new FileWriter(xmlFile);

            MarshallerHelper.marshall(PERSON, writer);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
    }


    @XmlRootElement(name = "city")
    private static class City implements Serializable {
        private String name;
        private int formed;
        private boolean active;
        private Members members;

        public City(String name, int formed, boolean active, Members members) {
            this.name = name;
            this.formed = formed;
            this.active = active;
            this.members = members;
        }

        public City() { }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getFormed() {
            return formed;
        }

        public void setFormed(int formed) {
            this.formed = formed;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public Members getMembers() {
            return members;
        }

        public void setMembers(Members members) {
            this.members = members;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof XmlUtilsTest.City))
                return false;
            City city = (City) o;
            return formed == city.formed && active == city.active && Objects.equals(name, city.name) && Objects.equals(members, city.members);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, formed, active, members);
        }
    }

    @XmlRootElement(name = "members")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Members implements Serializable {
        @XmlElement(name = "member")
        private List<Member> members;

        public Members(List<Member> members) {
            this.members = members;
        }

        public Members() { }

        public List<Member> getMembers() {
            return members;
        }

        public void setMembers(List<Member> members) {
            this.members = members;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Members))
                return false;
            Members members1 = (Members) o;
            return Objects.equals(members, members1.members);
        }

        @Override
        public int hashCode() {
            return Objects.hash(members);
        }
    }

    @XmlRootElement(name = "member")
    public static class Member implements Serializable {
        private String name;
        private int age;

        public Member(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public Member() { }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Member member = (Member) o;
            return age == member.age && Objects.equals(name, member.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    public static class Person implements Serializable {
        private String name;

        public Person(String name) {
            this.name = name;
        }

        public Person() { }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
