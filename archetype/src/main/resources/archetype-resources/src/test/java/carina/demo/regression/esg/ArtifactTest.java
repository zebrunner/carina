#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.regression.esg;

import com.zebrunner.carina.core.IAbstractTest;
import ${package}.carina.demo.gui.pages.common.HomePageBase;
import ${package}.carina.demo.utils.ArtifactUtils;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.report.SessionContext;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

public class ArtifactTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void artifactTest() throws IOException {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");

        LOGGER.info("Checking the presence of artifacts (video.log, task.log)...");
        List<String> artifacts = List.of("video.log", "task.log");
        SoftAssert softAssert = new SoftAssert();
        for (String artifact : artifacts) {
            boolean isPresent = ArtifactUtils.isArtifactPresent(
                    new FluentWait<>(getDriver())
                            .pollingEvery(Duration.ofSeconds(1))
                            .withTimeout(Duration.ofSeconds(30)),
                    artifact);
            softAssert.assertTrue(isPresent, artifact + " not found.");
            if (isPresent) {
                LOGGER.info("Artifact '{}' is present.", artifact);
                boolean isNotEmpty = Files.size(SessionContext.getArtifact(getDriver(), artifact).orElseThrow()) > 0;
                if (isNotEmpty) {
                    LOGGER.info("Artifact '{}' is not empty.", artifact);
                }
                softAssert.assertTrue(isNotEmpty, String.format("Artifact '%s' is empty.", artifact));
            }
        }
        softAssert.assertAll();
    }

    @Test
    public void mitmDumpArtifactTest() throws IOException {
        R.CONFIG.put("proxy_type", "Zebrunner", true);
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");

        LOGGER.info("Checking the presence of the dump.mitm artifact...");
        Assert.assertTrue(ArtifactUtils.isArtifactPresent(new FluentWait<>(getDriver())
                                .pollingEvery(Duration.ofSeconds(1))
                                .withTimeout(Duration.ofSeconds(30)),
                        "dump.mitm"),
                "Artifact 'dump.mitm' was not found.");
        LOGGER.info("Artifact 'dump.mitm' is present.");
        Assert.assertTrue(Files.size(SessionContext.getArtifact(getDriver(), "dump.mitm").orElseThrow()) > 0,
                "Artifact 'dump.mitm' is empty.");
        LOGGER.info("Artifact 'dump.mitm' is not empty.");
    }
}