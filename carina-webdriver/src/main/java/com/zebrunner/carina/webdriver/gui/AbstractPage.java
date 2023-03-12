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
package com.zebrunner.carina.webdriver.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.pdf.PdfWriter;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.zebrunner.carina.utils.factory.ICustomTypePageFactory;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.webdriver.Screenshot;
import com.zebrunner.carina.webdriver.decorator.PageOpeningStrategy;
import com.zebrunner.carina.webdriver.screenshot.ExplicitFullSizeScreenshotRule;

/**
 * All page POJO objects should extend this abstract page to get extra logic.
 * 
 * @author Alex Khursevich
 */
public abstract class AbstractPage extends AbstractUIObject implements ICustomTypePageFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private PageOpeningStrategy pageOpeningStrategy = PageOpeningStrategy.valueOf(Configuration.get(Parameter.PAGE_OPENING_STRATEGY));
    
	public AbstractPage(WebDriver driver) {
		super(driver);
	}

    public PageOpeningStrategy getPageOpeningStrategy() {
        return pageOpeningStrategy;
    }

    public void setPageOpeningStrategy(PageOpeningStrategy pageOpeningStrategy) {
        this.pageOpeningStrategy = pageOpeningStrategy;
    }

    public boolean isPageOpened() {
        return isPageOpened(EXPLICIT_TIMEOUT);
    }

    public boolean isPageOpened(long timeout) {
        switch (pageOpeningStrategy) {
        case BY_URL:
            return super.isPageOpened(this, timeout);
        case BY_ELEMENT:
            if (uiLoadedMarker == null) {
                throw new RuntimeException("Please specify uiLoadedMarker for the page/screen to validate page opened state");
            }
            return uiLoadedMarker.isElementPresent(timeout);
        case BY_URL_AND_ELEMENT:
            boolean isOpened = super.isPageOpened(this, timeout);
            if (!isOpened) {
                return false;
            }

            if (uiLoadedMarker != null) {
                isOpened = uiLoadedMarker.isElementPresent(timeout);
            }

            if (!isOpened) {
                LOGGER.warn(String.format(
                        "Loaded page url is as expected but page loading marker element is not visible: %s",
                        uiLoadedMarker.getBy().toString()));
            }
            return isOpened;
        default:
            throw new RuntimeException("Page opening strategy was not applied properly");
        }
    }

    /**
     * Asserts whether page is opened or not. Inside there is a check for expected url matches actual page url.
     * In addition if uiLoadedMarker is specified for the page it will check whether mentioned element presents on page or not.
     */
    public void assertPageOpened() {
        assertPageOpened(EXPLICIT_TIMEOUT);
    }

    /**
     * Asserts whether page is opened or not. Inside there is a check for expected url matches actual page url.
     * In addition if uiLoadedMarker is specified for the page it will check whether mentioned element presents on page or not.
     * 
     * @param timeout Completing of page loading conditions will be verified within specified timeout
     */
    public void assertPageOpened(long timeout) {
        switch (pageOpeningStrategy) {
        case BY_URL:
            Assert.assertTrue(super.isPageOpened(this, timeout), String.format("%s not loaded: url is not as expected", getPageClassName()));
            break;
        case BY_ELEMENT:
            if (uiLoadedMarker == null) {
                throw new RuntimeException("Please specify uiLoadedMarker for the page/screen to validate page opened state");
            }
            Assert.assertTrue(uiLoadedMarker.isElementPresent(timeout), String.format("%s not loaded: page loading marker element is not visible: %s",
                    getPageClassName(), uiLoadedMarker.getBy().toString()));
            break;
        case BY_URL_AND_ELEMENT:
            if (!super.isPageOpened(this, timeout)) {
                Assert.fail(String.format("%s not loaded: url is not as expected", getPageClassName()));
            }

            if (uiLoadedMarker != null) {
                Assert.assertTrue(uiLoadedMarker.isElementPresent(timeout),
                        String.format("%s not loaded: url is correct but page loading marker element is not visible: %s", getPageClassName(),
                                uiLoadedMarker.getBy().toString()));
            }
            break;
        default:
            throw new RuntimeException("Page opening strategy was not applied properly");
        }
    }

	private String getPageClassName() {
		return String.join(" ", this.getClass().getSimpleName().split("(?=\\p{Upper})"));
	}

    /**
     * Save page as pdf<br>
     * If you want to set fileName as test name, use TestNamingService.getTestName()
     */
    public String savePageAsPdf(boolean scaled, String fileName) throws IOException, DocumentException {
        String pdfName = "";

        // Define test screenshot root
        // use fileName instead
        // String test = TestNamingService.getTestName();

        File artifactsFolder = ReportContext.getArtifactsFolder();


        ExplicitFullSizeScreenshotRule screenshotRule = new ExplicitFullSizeScreenshotRule();
        Optional<String> screenshot = Screenshot.capture(getDriver(), getDriver(), screenshotRule, "");
        if (screenshot.isEmpty()) {
            return pdfName;
        }

        String fileID = fileName.replaceAll("\\W+", "_") + "-" + System.currentTimeMillis();
        pdfName = fileID + ".pdf";
        String fullPdfPath = artifactsFolder.getAbsolutePath() + "/" + pdfName;

        Image image = Image.getInstance(screenshotRule.getSaveFolder().toFile().getAbsolutePath() + "/" + screenshot.get());
        Document document = null;
        if (scaled) {
            document = new Document(PageSize.A4, 10, 10, 10, 10);
            if (image.getHeight() > (document.getPageSize().getHeight() - 20)
                    || image.getScaledWidth() > (document.getPageSize().getWidth() - 20)) {
                image.scaleToFit(document.getPageSize().getWidth() - 20, document.getPageSize().getHeight() - 20);
            }
        } else {
            document = new Document(new RectangleReadOnly(image.getScaledWidth(), image.getScaledHeight()));
        }
        PdfWriter.getInstance(document, new FileOutputStream(fullPdfPath));
        document.open();
        document.add(image);
        document.close();
        return fullPdfPath;
    }

    /**
     * Save page as pdf<br>
     * If you want to set fileName as test name, use TestNamingService.getTestName()
     */
    public String savePageAsPdf(String fileName) throws IOException, DocumentException {
        return savePageAsPdf(true, fileName);
    }

    /**
     * Waits till JS and jQuery (if applicable for the page) are completely processed on the page
     */
    public void waitForJSToLoad() {
        waitForJSToLoad(EXPLICIT_TIMEOUT);
    }

    /**
     * Waits till JS and jQuery (if applicable for the page) are completely processed on the page
     * 
     * @param timeout Completing of JS loading will be verified within specified timeout
     */
    public void waitForJSToLoad(long timeout) {
        // wait for jQuery to load
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    return ((Long) executor.executeScript("return jQuery.active") == 0);
                } catch (Exception e) {
                    return true;
                }
            }
        };
        // wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return executor.executeScript("return document.readyState").toString().equals("complete");
            }
        };
        String errMsg = "JS was not loaded on page during expected time";
        if ((Boolean) executor.executeScript("return window.jQuery != undefined")) {
            Assert.assertTrue(waitUntil(jQueryLoad, timeout) && waitUntil(jsLoad, timeout), errMsg);
        } else {
            Assert.assertTrue(waitUntil(jsLoad, timeout), errMsg);
        }
    }
}
