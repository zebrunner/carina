#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import com.zebrunner.carina.core.IAbstractTest;
import ${package}.carina.demo.utils.ArtifactUtils;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.report.SessionContext;
import com.zebrunner.carina.webdriver.DriverHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        LOGGER.info("Artifact's folder: {}", SessionContext.getArtifactsFolder());

        DriverHelper driverHelper = new DriverHelper(getDriver());
        driverHelper.openURL(url);
        pause(1);

        Optional<Path> file = SessionContext.getArtifact(getDriver(), "klassy-cafe.zip");
        Assert.assertTrue(file.isPresent() && Files.exists(file.get()), "klassy-cafe.zip is not available among downloaded artifacts");
    }
    
    @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Unable to find artifact:.*")
    public void getInvalidArtifactTest() {
        String url = "https://www.free-css.com/assets/files/free-css-templates/download/page280/klassy-cafe.zip";

        LOGGER.info("Artifact's folder: {}", SessionContext.getArtifactsFolder());

        DriverHelper driverHelper = new DriverHelper(getDriver());
        driverHelper.openURL(url);

        Optional<Path> path = SessionContext.getArtifact(getDriver(), UUID.randomUUID().toString());
        Assert.assertTrue(path.isEmpty(), "artifact with random name available among downloaded artifacts");    }
    
    @Test()
    public void getArtifactsTest() {
        String url1 = "https://www.free-css.com/assets/files/free-css-templates/download/page279/tropiko.zip";
        String url2 = "https://www.free-css.com/assets/files/free-css-templates/download/page280/solar.zip";

        R.CONFIG.put("auto_download", "true");

        LOGGER.info("Artifact's folder: {}", SessionContext.getArtifactsFolder());

        DriverHelper driverHelper = new DriverHelper(getDriver());
        driverHelper.openURL(url1);
        driverHelper.openURL(url2);

        FluentWait<WebDriver> wait = new FluentWait<>(getDriver())
                .pollingEvery(Duration.ofSeconds(1))
                .withTimeout(Duration.ofSeconds(30));

        SoftAssert softAssert = new SoftAssert();

        softAssert.assertTrue(ArtifactUtils.isArtifactPresent(wait, "tropiko.zip"), "tropiko.zip not found");
        softAssert.assertTrue(ArtifactUtils.isArtifactPresent(wait, "solar.zip"), "solar.zip not found");

        softAssert.assertAll();

        List<Path> files = SessionContext.getArtifacts(getDriver(), ".+");
        Assert.assertEquals(files.size(), 2);

        files = SessionContext.getArtifacts(getDriver(), "solar.z.+");
        Assert.assertEquals(files.size(), 1);

        files = SessionContext.getArtifacts(getDriver(), "UUID.randomUUID().toString()");
        Assert.assertEquals(files.size(), 0);

    }

}
