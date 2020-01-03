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
package com.qaprosoft.carina.core.foundation.webdriver.locator;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CaseInsensitiveTest {
    
    @Test()
    public void testMobileTextLocatorWithSingleQuote() {
    	String xpath = "//android.widget.Button[@text = 'Text text']";
    	By expectedRes = By.xpath("//android.widget.Button[translate(@text, 'TEXT TEXT', 'text text')=translate('Text text', 'TEXT TEXT', 'text text')]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testWebTextLocatorWithSingleQuote() {
    	String xpath = "//div[text() = 'Text text']";
    	By expectedRes = By.xpath("//div[translate(text(), 'TEXT TEXT', 'text text')=translate('Text text', 'TEXT TEXT', 'text text')]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testMobileTextLocatorWithDoubleQuotes() {
    	String xpath = "//android.widget.Button[@text = \"Text text\"]";
    	By expectedRes = By.xpath("//android.widget.Button[translate(@text, \"TEXT TEXT\", \"text text\")=translate(\"Text text\", \"TEXT TEXT\", \"text text\")]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testWebTextLocatorWithDoubleQuotes() {
    	String xpath = "//div[text() = \"Text text\"]";
    	By expectedRes = By.xpath("//div[translate(text(), \"TEXT TEXT\", \"text text\")=translate(\"Text text\", \"TEXT TEXT\", \"text text\")]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testMobileTextLocatorWithSingleQuoteAndContains() {
    	String xpath = "//android.widget.Button[contains(@text, 'Text text')]";
    	By expectedRes = By.xpath("//android.widget.Button[contains(translate(@text, 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testWebTextLocatorWithSingleQuoteAndContains() {
    	String xpath = "//div[contains(text(), 'Text text')]";
    	By expectedRes = By.xpath("//div[contains(translate(text(), 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testMobileTextLocatorWithDoubleQuotesAndContains() {
    	String xpath = "//android.widget.Button[contains(@text, \"Text text\")]";
    	By expectedRes = By.xpath("//android.widget.Button[contains(translate(@text, \"TEXT TEXT\", \"text text\"),translate(\"Text text\", \"TEXT TEXT\", \"text text\"))]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testWebTextLocatorWithDoubleQuotesAndContains() {
    	String xpath = "//div[contains(text(), \"Text text\")]";
    	By expectedRes = By.xpath("//div[contains(translate(text(), \"TEXT TEXT\", \"text text\"),translate(\"Text text\", \"TEXT TEXT\", \"text text\"))]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testMobileTextLocatorWithQuoteInText() {
    	String xpath = "//android.widget.Button[contains(@text, \"Text's text\")]";
    	By expectedRes = By.xpath("//android.widget.Button[contains(translate(@text, \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testWebTextLocatorWithQuoteInText() {
    	String xpath = "//div[contains(text(), \"Text's text\")]";
    	By expectedRes = By.xpath("//div[contains(translate(text(), \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testComplexMobileTextLocator() {
    	String xpath = "//android.widget.LinearLayout[./android.widget.TextView[contains(@text, 'Text text')]]//*[contains(@resource-id, 'id/someId')]";
    	By expectedRes = By.xpath("//android.widget.LinearLayout[./android.widget.TextView[contains(translate(@text, 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]]//*[contains(@resource-id, 'id/someId')]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testComplexWebTextLocator() {
    	String xpath = "//div[@class = 'someClass']/../..//h5[contains(text(), 'Text text')]//span[contains(@class, 'someClass')]";
    	By expectedRes = By.xpath("//div[@class = 'someClass']/../..//h5[contains(translate(text(), 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]//span[contains(@class, 'someClass')]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testComplexMobileTextLocatorWithQuoteInText() {
    	String xpath = "//android.widget.LinearLayout[./android.widget.TextView[contains(@text, \"Text's text\")]]//*[contains(@resource-id, 'id/someId')]";
    	By expectedRes = By.xpath("//android.widget.LinearLayout[./android.widget.TextView[contains(translate(@text, \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]]//*[contains(@resource-id, 'id/someId')]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }
    
    @Test()
    public void testComplexWebTextLocatorWithQuoteInText() {
    	String xpath = "//div[@class = 'someClass']/../..//h5[contains(text(), \"Text's text\")]//span[contains(@class, 'someClass')]";
    	By expectedRes = By.xpath("//div[@class = 'someClass']/../..//h5[contains(translate(text(), \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]//span[contains(@class, 'someClass')]");
    	
    	By result = ExtendedElementLocator.toCaseInsensitive(xpath);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

}
