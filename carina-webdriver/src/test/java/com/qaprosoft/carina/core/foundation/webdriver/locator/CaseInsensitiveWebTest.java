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
package com.qaprosoft.carina.core.foundation.webdriver.locator;

import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.LocatorConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.CaseInsensitiveConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.ParamsToConvert;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.Platform;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CaseInsensitiveWebTest {

    @Test
    public void testWebTextLocatorWithSingleQuote() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[text() = 'Text text']";
        By expectedRes = By.xpath("//div[translate(text(), 'TEXT TEXT', 'text text')=translate('Text text', 'TEXT TEXT', 'text text')]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithDoubleQuotes() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[text() = \"Text text\"]";
        By expectedRes = By.xpath("//div[translate(text(), \"TEXT TEXT\", \"text text\")=translate(\"Text text\", \"TEXT TEXT\", \"text text\")]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithSingleQuoteAndContains() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[contains(text(), 'Text text')]";
        By expectedRes = By.xpath("//div[contains(translate(text(), 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithDoubleQuotesAndContains() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[contains(text(), \"Text text\")]";
        By expectedRes = By.xpath(
                "//div[contains(translate(text(), \"TEXT TEXT\", \"text text\"),translate(\"Text text\", \"TEXT TEXT\", \"text text\"))]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithQuoteInText() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[contains(text(), \"Text's text\")]";
        By expectedRes = By.xpath(
                "//div[contains(translate(text(), \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithQuoteAndDollarSymbolInText() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[text() = 'Text text$169,90']";
        By expectedRes = By.xpath(
                "//div[translate(text(), 'TEXT TEXT$169,90', 'text text$169,90')=translate('Text text$169,90', 'TEXT TEXT$169,90', 'text text$169,90')]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testWebTextLocatorWithDoubleQuoteAndDollarSymbolInText() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[text() = \"Text text$169,90\"]";
        By expectedRes = By.xpath(
                "//div[translate(text(), \"TEXT TEXT$169,90\", \"text text$169,90\")=translate(\"Text text$169,90\", \"TEXT TEXT$169,90\", \"text text$169,90\")]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testComplexWebTextLocator() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[@class = 'someClass']/../..//h5[contains(text(), 'Text text')]//span[contains(@class, 'someClass')]";
        By expectedRes = By.xpath(
                "//div[@class = 'someClass']/../..//h5[contains(translate(text(), 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]//span[contains(@class, 'someClass')]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testComplexWebTextLocatorWithQuoteInText() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//div[@class = 'someClass']/../..//h5[contains(text(), \"Text's text\")]//span[contains(@class, 'someClass')]";
        By expectedRes = By.xpath(
                "//div[@class = 'someClass']/../..//h5[contains(translate(text(), \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]//span[contains(@class, 'someClass')]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testTextLocatorWithS() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.WEB);

        String xpath = "//*[contains(text(), \"%s\")]";
        By expectedRes = By.xpath("//*[contains(translate(text(), \"%s\", \"%s\"),translate(\"%s\", \"%s\", \"%s\"))]");
        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testId() {
        LocatorConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, false, false, false), Platform.WEB);

        String xpath = ".//*[@id='some id']";
        By expectedRes = By.xpath(
                ".//*[translate(@id, 'SOME ID', 'some id')=translate('some id', 'SOME ID', 'some id')]");
        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testClass() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, false, true), Platform.WEB);

        String xpath = "//md-icon[@class='example-class ExAmPLe-cLASS-2']";
        By expectedRes = By.xpath(
                "//md-icon[translate(@class, 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2')=translate('example-class ExAmPLe-cLASS"
                        + "-2', 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2')]");
        By result = converter.convert(By.xpath(xpath));
        System.out.println(result);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testName() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, true, false, false), Platform.WEB);

        String xpath = "//md-icon[@name='example-class ExAmPLe-cLASS-2' and @class=\"some class\"]";
        By expectedRes = By.xpath(
                "//md-icon[translate(@name, 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2')=translate('example-class ExAmPLe-"
                        + "cLASS-2', 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2') and @class=\"some class\"]");
        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

}
