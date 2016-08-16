package com.qaprosoft.carina.core.foundation.cucumber;

import com.qaprosoft.carina.core.foundation.utils.image.ImageProcessing;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class CucumberBaseTest extends CucumberRunner {


    /**
     * Check is it Cucumber Test or not.
     *
     * @throws Throwable
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
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            screenshot = ImageProcessing.imageResize(screenshot);
            scenario.embed(screenshot, "image/png"); //stick it in the report
        }

    }
}
