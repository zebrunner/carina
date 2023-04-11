#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.webdriver.DriverHelper;

public class AutoDownloadTest implements IAbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    @BeforeSuite()
    public void BeforeAutoDownload() {
        R.CONFIG.put("auto_download", "true");
        R.CONFIG.put("auto_screenshot", "false");
    }

    @Test()
    public void getArtifactTest() {
        String url = "https://www.free-css.com/assets/files/free-css-templates/download/page280/klassy-cafe.zip";

        LOGGER.info("Artifact's folder: {}", ReportContext.getArtifactsFolder().getAbsolutePath());

        DriverHelper driverHelper = new DriverHelper(getDriver());
        driverHelper.openURL(url);
        pause(1);

        File file = ReportContext.getArtifact(getDriver(), "klassy-cafe.zip");
        Assert.assertTrue(file.exists(), "klassy-cafe.zip is not available among downloaded artifacts");
    }
    
    @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Unable to find artifact:.*")
    public void getInvalidArtifactTest() {
        String url = "https://www.free-css.com/assets/files/free-css-templates/download/page280/klassy-cafe.zip";

        LOGGER.info("Artifact's folder: {}", ReportContext.getArtifactsFolder().getAbsolutePath());

        DriverHelper driverHelper = new DriverHelper(getDriver());
        driverHelper.openURL(url);

        ReportContext.getArtifact(getDriver(), UUID.randomUUID().toString());
    }
    
    @Test()
    public void getArtifactsTest() {
        String url1 = "https://www.free-css.com/assets/files/free-css-templates/download/page279/tropiko.zip";
        String url2 = "https://www.free-css.com/assets/files/free-css-templates/download/page280/solar.zip";

        R.CONFIG.put("auto_download", "true");

        LOGGER.info("Artifact's folder: {}", ReportContext.getArtifactsFolder().getAbsolutePath());

        DriverHelper driverHelper = new DriverHelper(getDriver());
        driverHelper.openURL(url1);
        driverHelper.openURL(url2);

        FluentWait<WebDriver> wait = new FluentWait<>(getDriver())
                .pollingEvery(Duration.ofSeconds(1))
                .withTimeout(Duration.ofSeconds(30));

        SoftAssert softAssert = new SoftAssert();

        softAssert.assertTrue(isArtifactPresent(wait, "tropiko.zip"), "tropiko.zip not found");
        softAssert.assertTrue(isArtifactPresent(wait, "solar.zip"), "solar.zip not found");

        softAssert.assertAll();

        List<File> files = ReportContext.getArtifacts(getDriver(), ".+");
        Assert.assertEquals(files.size(), 2);

        files = ReportContext.getArtifacts(getDriver(), "solar.z.+");
        Assert.assertEquals(files.size(), 1);

        files = ReportContext.getArtifacts(getDriver(), "UUID.randomUUID().toString()");
        Assert.assertEquals(files.size(), 0);

    }

    private static boolean isArtifactPresent(FluentWait<WebDriver> wait, String name) {
        boolean isFound = false;
        try {
            isFound = wait.until(dr -> {
                List<String> list = ReportContext.listArtifacts(dr);
                if (list.contains(name)) {
                    return true;
                }
                return null;
            });
        } catch (TimeoutException e) {
            // do nothing
        }
        return isFound;
    }

}
