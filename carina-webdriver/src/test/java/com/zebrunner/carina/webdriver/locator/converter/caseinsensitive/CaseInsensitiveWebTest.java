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
package com.zebrunner.carina.webdriver.locator.converter.caseinsensitive;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CaseInsensitiveWebTest {

    @Test
    public void testWebTextLocatorWithSingleQuote() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[text() = 'Text text']")
                .toString();
        String expected = By.xpath("//div[translate(text(), 'TEXT TEXT', 'text text')=translate('Text text', 'TEXT TEXT', 'text text')]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithDoubleQuotes() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[text() = \"Text text\"]")
                .toString();
        String expected = By.xpath("//div[translate(text(), \"TEXT TEXT\", \"text text\")=translate(\"Text text\", \"TEXT TEXT\", \"text text\")]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithSingleQuoteAndContains() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[contains(text(), 'Text text')]")
                .toString();
        String expected = By.xpath("//div[contains(translate(text(), 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithDoubleQuotesAndContains() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[contains(text(), \"Text text\")]")
                .toString();
        String expected = By.xpath(
                "//div[contains(translate(text(), \"TEXT TEXT\", \"text text\"),translate(\"Text text\", \"TEXT TEXT\", \"text text\"))]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithQuoteInText() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[contains(text(), \"Text's text\")]")
                .toString();
        String expected = By.xpath(
                "//div[contains(translate(text(), \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithQuoteAndDollarSymbolInText() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[text() = 'Text text$169,90']")
                .toString();
        String expected = By.xpath(
                "//div[translate(text(), 'TEXT TEXT$169,90', 'text text$169,90')=translate('Text text$169,90', 'TEXT TEXT$169,90', 'text text$169,90')]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithDoubleQuoteAndDollarSymbolInText() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[text() = \"Text text$169,90\"]")
                .toString();
        String expected = By.xpath(
                "//div[translate(text(), \"TEXT TEXT$169,90\", \"text text$169,90\")=translate(\"Text text$169,90\", \"TEXT TEXT$169,90\", \"text text$169,90\")]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testComplexWebTextLocator() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[@class = 'someClass']/../..//h5[contains(text(), 'Text text')]//span[contains(@class, 'someClass')]")
                .toString();
        String expected = By.xpath(
                "//div[@class = 'someClass']/../..//h5[contains(translate(text(), 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]//span[contains(@class, 'someClass')]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testComplexWebTextLocatorWithQuoteInText() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(true)
                .setClassAttr(false);

        String xpath = By.xpath("//div[@class = 'someClass']/../..//h5[contains(text(), \"Text's text\")]//span[contains(@class, 'someClass')]")
                .toString();
        String expected = By.xpath(
                "//div[@class = 'someClass']/../..//h5[contains(translate(text(), \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]//span[contains(@class, 'someClass')]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testId() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(true)
                .setName(false)
                .setText(false)
                .setClassAttr(false);

        String xpath = By.xpath(".//*[@id='some id']")
                .toString();
        String expected = By.xpath(".//*[translate(@id, 'SOME ID', 'some id')=translate('some id', 'SOME ID', 'some id')]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testClass() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(false)
                .setText(false)
                .setClassAttr(true);

        String xpath = By.xpath("//md-icon[@class='example-class ExAmPLe-cLASS-2']")
                .toString();
        String expected = By.xpath(
                "//md-icon[translate(@class, 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2')=translate('example-class ExAmPLe-cLASS"
                        + "-2', 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2')]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testName() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(false)
                .setId(false)
                .setName(true)
                .setText(false)
                .setClassAttr(false);

        String xpath = By.xpath("//md-icon[@name='example-class ExAmPLe-cLASS-2' and @class=\"some class\"]")
                .toString();
        String expected = By.xpath(
                "//md-icon[translate(@name, 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2')=translate('example-class ExAmPLe-"
                        + "cLASS-2', 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2') and @class=\"some class\"]")
                .toString();
        Assert.assertEquals(converter.convert(xpath), expected, "Incorrect converting to caseinsensitive xpath!");
    }
}
