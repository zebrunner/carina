#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;

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
        pause(1);

        List<String> fileNames = ReportContext.listArtifacts(getDriver());
        Assert.assertTrue(fileNames.contains("tropiko.zip"), "tropiko.zip not found");
        Assert.assertTrue(fileNames.contains("solar.zip"), "solar.zip not found");

        List<File> files = ReportContext.getArtifacts(getDriver(), ".+");
        Assert.assertEquals(files.size(), 2);

        files = ReportContext.getArtifacts(getDriver(), "solar.z.+");
        Assert.assertEquals(files.size(), 1);

        files = ReportContext.getArtifacts(getDriver(), "UUID.randomUUID().toString()");
        Assert.assertEquals(files.size(), 0);

    }

}
