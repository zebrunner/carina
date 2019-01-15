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
package com.qaprosoft.carina.core.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.pdf.PdfWriter;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.factory.ICustomTypePageFactory;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;

/**
 * All page POJO objects should extend this abstract page to get extra logic.
 * 
 * @author Alex Khursevich
 */
public abstract class AbstractPage extends AbstractUIObject implements ICustomTypePageFactory {
    protected static final Logger LOGGER = Logger.getLogger(AbstractPage.class);
    
	protected String pageURL = getUrl();

	public AbstractPage(WebDriver driver) {
		super(driver);
	}

    /**
     * Opens page according to specified in constructor URL.
     */
    public void open() {
        openURL(pageURL);
    }

    protected void setPageURL(String relURL) {
        String baseURL;
        // if(!"NULL".equalsIgnoreCase(Configuration.get(Parameter.ENV)))
        if (!Configuration.get(Parameter.ENV).isEmpty()) {
            baseURL = Configuration.getEnvArg("base");
        } else {
            baseURL = Configuration.get(Parameter.URL);
        }
        pageURL = baseURL + relURL;
    }

    protected void setPageAbsoluteURL(String url) {
        this.pageURL = url;
    }

    public String getPageURL() {
        return pageURL;
    }

    public boolean isPageOpened() {
        return isPageOpened(this);
    }

    public boolean isPageOpened(long timeout) {
        return isPageOpened(this, timeout);
    }

    public String savePageAsPdf(boolean scaled) throws IOException, DocumentException {
        String pdfName = "";

        // Define test screenshot root
        String test = "";
        if (TestNamingUtil.isTestNameRegistered()) {
            test = TestNamingUtil.getTestNameByThread();
        } else {
            test = "undefined";
        }

        File testRootDir = ReportContext.getTestDir();
        File artifactsFolder = ReportContext.getArtifactsFolder();

        String fileID = test.replaceAll("\\W+", "_") + "-" + System.currentTimeMillis();
        pdfName = fileID + ".pdf";

        String fullPdfPath = artifactsFolder.getAbsolutePath() + "/" + pdfName;
        // TODO: test this implementation and change back to capture if necessary
        Image image = Image.getInstance(testRootDir.getAbsolutePath() + "/" + Screenshot.captureFailure(driver, ""));
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

    public String savePageAsPdf() throws IOException, DocumentException {
        return savePageAsPdf(true);
    }

	private String getUrl() {
		String url = "";
		if (Configuration.getEnvArg(Parameter.URL.getKey()).isEmpty()) {
			url = Configuration.get(Parameter.URL);
		} else {
			url = Configuration.getEnvArg(Parameter.URL.getKey());
		}
		return url;
	}
}
