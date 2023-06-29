#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.regression.esg;

import com.zebrunner.carina.core.IAbstractTest;
import ${package}.carina.demo.gui.pages.common.HomePageBase;
import ${package}.carina.demo.websocket.EndpointUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import static ${package}.carina.demo.websocket.EndpointUtils.getHttpUrl;

public class ClipboardTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String clipboardString = "Example text for the clipboard.";

    @BeforeSuite
    public void beforeSuite() {
        getDriver();
    }

    @Test
    public void clipboardWriteTest() throws IOException, URISyntaxException {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");
        LOGGER.info("HomePage is opened.");

        LOGGER.info("Creating output connection with the clipboard endpoint....");
        HttpURLConnection con = (HttpURLConnection) new URI(getHttpUrl(getDriver(), "clipboard/"))
                .toURL()
                .openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setInstanceFollowRedirects(true);
        EndpointUtils.getHttpAuthorization().ifPresent(con::setAuthenticator);
        LOGGER.info("Created connection with the clipboard endpoint.");
        LOGGER.info("Trying to put '{}' text to the clipboard.", clipboardString);
        try (OutputStream os = con.getOutputStream();
                DataOutputStream ostream = new DataOutputStream(os)) {
            ostream.writeBytes(clipboardString);
            ostream.flush();
        }
        LOGGER.info("Text '{}' sent to the clipboard.", clipboardString);
        Assert.assertEquals(con.getResponseCode(), 200, "Response code is not as expected.");
        LOGGER.info("Response code of connection is correct.");
    }

    @Test(dependsOnMethods = "clipboardWriteTest")
    public void clipboardReadTest() throws IOException, URISyntaxException {
        LOGGER.info("Creating input connection with the clipboard endpoint....");
        HttpURLConnection con = (HttpURLConnection) new URI(getHttpUrl(getDriver(), "clipboard/"))
                .toURL()
                .openConnection();
        con.setRequestMethod("GET");
        con.setDoInput(true);
        con.setInstanceFollowRedirects(false);
        EndpointUtils.getHttpAuthorization().ifPresent(con::setAuthenticator);
        LOGGER.info("Created input connection with the clipboard endpoint.");

        Assert.assertEquals(con.getResponseCode(), 200, "Response code is not as expected.");
        LOGGER.info("Response code of connection is correct: 200.");
        LOGGER.info("Trying to get text from the clipboard...");
        try (InputStream is = con.getInputStream();
                InputStreamReader ir = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(ir)) {
            String inputLine;
            StringBuilder sb = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            Assert.assertEquals(sb.toString().trim(), clipboardString,
                    "The string placed on the clipboard does not match the string taken from the clipboard.");
            LOGGER.info("Got text from the clipboard. Text as expected: '{}'", sb);
        }
    }
}