/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.cucumber;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.qaprosoft.carina.core.foundation.utils.image.ImageProcessing;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class CucumberBaseTest extends CucumberRunner {

    /**
     * Check is it Cucumber Test or not.
     *
     * @throws Throwable java.lang.Throwable
     */
    @Before
    public void beforeScenario() throws Throwable {
        if (!isCucumberTest()) {
            throw new Exception("Not Cucumber Test. Please check your configuration and config.properties file.");
        }
    }

    /**
     * take Screenshot Of Failure - this step should be added manually in common step definition
     * files if it will not be executed automatically
     *
     * @param scenario Scenario
     */
    @After
    public void takeScreenshotOfFailure(Scenario scenario) {
        LOGGER.info("In  @After takeScreenshotOfFailure");
        if (scenario.isFailed()) {
            LOGGER.error("Cucumber Scenario FAILED! Creating screenshot.");
            // TODO: remove reference onto the DriverPool reusing functionality from Screenshot object!
            byte[] screenshot = ((TakesScreenshot) DriverPool.getDriver()).getScreenshotAs(OutputType.BYTES);
            screenshot = ImageProcessing.imageResize(screenshot);
            scenario.embed(screenshot, "image/png"); // stick it in the report
        }

    }
}
