package com.qaprosoft.carina.core.foundation.webdriver.locator;

import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.CaseInsensitiveConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.ParamsToConvert;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.Platform;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CaseInsensitiveMobileTest {

    @Test
    public void testMobileTextLocatorWithSingleQuote() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        converter.setParamsToConvert(new ParamsToConvert(true, true, true, true));
        String xpath = "//android.widget.Button[@text = 'Text text']";
        By expectedRes = By.xpath(
                "//android.widget.Button[translate(@text, 'TEXT TEXT', 'text text')=translate('Text text', 'TEXT TEXT', 'text text')]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to case-insensitive xpath!");
    }

    @Test
    public void testMobileTextLocatorWithContentDesc() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        String xpath = "//*[@content-desc=\"{L10N:anySelect}\"]";
        By expectedRes = By.xpath(
                "//*[translate(@content-desc, \"{L10N:ANYSELECT}\", \"{l10n:anyselect}\")=translate(\"{L10N:anySelect}\", \"{L10N:ANYSELECT}\", \"{l10n:anyselect}\")]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testMobileTextLocatorWithDoubleQuotes() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        String xpath = "//android.widget.Button[@text = \"Text text\"]";
        By expectedRes = By.xpath(
                "//android.widget.Button[translate(@text, \"TEXT TEXT\", \"text text\")=translate(\"Text text\", \"TEXT TEXT\", \"text text\")]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testMobileTextLocatorWithSingleQuoteAndContains() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        String xpath = "//android.widget.Button[contains(@text, 'Text text')]";
        By expectedRes = By.xpath(
                "//android.widget.Button[contains(translate(@text, 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testMobileTextLocatorWithDoubleQuotesAndContains() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        String xpath = "//android.widget.Button[contains(@text, \"Text text\")]";
        By expectedRes = By.xpath(
                "//android.widget.Button[contains(translate(@text, \"TEXT TEXT\", \"text text\"),translate(\"Text text\", \"TEXT TEXT\", \"text text\"))]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testMobileTextLocatorWithQuoteInText() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        String xpath = "//android.widget.Button[contains(@text, \"Text's text\")]";
        By expectedRes = By.xpath(
                "//android.widget.Button[contains(translate(@text, \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testMobileTextLocatorWithQuoteAndDollarSymbolInText() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        String xpath = "//android.widget.Button[@text = 'Text text$169,90']";
        By expectedRes = By.xpath(
                "//android.widget.Button[translate(@text, 'TEXT TEXT$169,90', 'text text$169,90')=translate('Text text$169,90', 'TEXT TEXT$169,90', 'text text$169,90')]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testMobileTextLocatorWithDoubleQuoteAndDollarSymbolInText() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        String xpath = "//android.widget.Button[@text = \"Text text$169,90\"]";
        By expectedRes = By.xpath(
                "//android.widget.Button[translate(@text, \"TEXT TEXT$169,90\", \"text text$169,90\")=translate(\"Text text$169,90\", \"TEXT TEXT$169,90\", \"text text$169,90\")]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testComplexMobileTextLocator() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, true, true, true), Platform.MOBILE);

        String xpath = "//android.widget.LinearLayout[./android.widget.TextView[contains(@text, 'Text text')]]//*[contains(@resource-id, 'id/someId')]";
        By expectedRes = By.xpath(
                "//android.widget.LinearLayout[./android.widget.TextView[contains(translate(@text, 'TEXT TEXT', 'text text'),translate('Text text', 'TEXT TEXT', 'text text'))]]//*[contains(@resource-id, 'id/someId')]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testComplexMobileTextLocatorWithQuoteInText() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, true, true, true), Platform.MOBILE);

        String xpath = "//android.widget.LinearLayout[./android.widget.TextView[contains(@text, \"Text's text\")]]//*[contains(@resource-id, 'id/someId')]";
        By expectedRes = By.xpath(
                "//android.widget.LinearLayout[./android.widget.TextView[contains(translate(@text, \"TEXT'S TEXT\", \"text's text\"),translate(\"Text's text\", \"TEXT'S TEXT\", \"text's text\"))]]//*[contains(@resource-id, 'id/someId')]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testComplexTextLocatorWithOr() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, true, false), Platform.MOBILE);

        String xpath = "//android.widget.HorizontalScrollView//*[contains(@text, '{L10N:Text}') or contains(@text, '{L10N:Other}') or contains(@text, 'Some') or contains(@text, '{L10N:Any}')]";
        By expectedRes = By.xpath(
                "//android.widget.HorizontalScrollView//*[contains(translate(@text, '{L10N:TEXT}', '{l10n:text}'),translate('{L10N:Text}', '{L10N:TEXT}', '{l10n:text}'))"
                        + " or contains(translate(@text, '{L10N:OTHER}', '{l10n:other}'),translate('{L10N:Other}', '{L10N:OTHER}', '{l10n:other}'))"
                        + " or contains(translate(@text, 'SOME', 'some'),translate('Some', 'SOME', 'some'))"
                        + " or contains(translate(@text, '{L10N:ANY}', '{l10n:any}'),translate('{L10N:Any}', '{L10N:ANY}', '{l10n:any}'))]");

        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testTextLocatorWithS() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, true, true, true), Platform.MOBILE);

        String xpath = "//*[contains(@text, \"%s\")]";
        By expectedRes = By.xpath("//*[contains(translate(@text, \"%s\", \"%s\"),translate(\"%s\", \"%s\", \"%s\"))]");
        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testResourceId() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(true, false, false, false), Platform.MOBILE);

        String xpath = "//android.widget.LinearLayout[./android.widget.TextView[contains(@text, \"Text's text\")]]//*[contains(@resource-id, 'id/someId')]";
        By expectedRes = By.xpath(
                "//android.widget.LinearLayout[./android.widget.TextView[contains(@text, \"Text's text\")]]//*[contains(translate(@resource-id, 'ID/SOMEID', 'id/someid'),translate('id/someId', 'ID/SOMEID', 'id/someid'))]");
        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

    @Test
    public void testClass() {
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, false, true), Platform.MOBILE);

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
        CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, true, false, false), Platform.MOBILE);

        String xpath = "//md-icon[@name='example-class ExAmPLe-cLASS-2' and @class=\"some class\"]";
        By expectedRes = By.xpath(
                "//md-icon[translate(@name, 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2')=translate('example-class ExAmPLe-"
                        + "cLASS-2', 'EXAMPLE-CLASS EXAMPLE-CLASS-2', 'example-class example-class-2') and @class=\"some class\"]");
        By result = converter.convert(By.xpath(xpath));
        Assert.assertEquals(result, expectedRes, "Incorrect converting to caseinsensitive xpath!");
    }

}
