#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.regression.esg;

import com.zebrunner.carina.core.IAbstractTest;
import ${package}.carina.demo.gui.pages.common.HomePageBase;
import ${package}.carina.demo.websocket.WebsocketClientEndpoint;
import com.zebrunner.carina.utils.R;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static ${package}.carina.demo.websocket.EndpointUtils.getWebSocketUrl;
import static ${package}.carina.demo.websocket.EndpointUtils.isMessagePresent;

public class VNCTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void vncTest() throws URISyntaxException {
        R.CONFIG.put("capabilities.zebrunner:enableVNC", "true", true);
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");

        LOGGER.info("Trying to create connection with the VNC endpoint...");
        final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI(getWebSocketUrl(getDriver(), "ws/vnc/")));
        List<Object> messages = new ArrayList<>();
        clientEndPoint.addMessageHandler(messages::add);

        FluentWait<WebDriver> wait = new FluentWait<>(getDriver())
                .pollingEvery(Duration.ofSeconds(1))
                .withTimeout(Duration.ofSeconds(20));

        Assert.assertTrue(isMessagePresent(wait, messages), "There are no messages from the VNC endpoint.");
        LOGGER.info("Created connection with the VNC endpoint.");
    }

}