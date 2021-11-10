#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.FirefoxCapabilities;
import ${package}.carina.demo.gui.components.NewsItem;
import ${package}.carina.demo.gui.pages.HomePage;
import ${package}.carina.demo.gui.pages.NewsPage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This sample shows how initialize multiple drivers and run the tests on different browsers.
 *
 * @author qpsdemo
 */
public class WebMultipleBrowserTest implements IAbstractTest {

    @Test
    @MethodOwner(owner = "qpsdemo")
    public void multipleBrowserTest() {
        HomePage chromeHomePage = new HomePage(getDriver("chrome", new ChromeCapabilities().getCapability("Chrome Test")));
        chromeHomePage.open();
        Assert.assertTrue(chromeHomePage.isPageOpened(), "Chrome home page is not opened!");

        HomePage firefoxHomePage = new HomePage(getDriver("firefox", new FirefoxCapabilities().getCapability("Firefox Test")));
        firefoxHomePage.open();
        Assert.assertTrue(firefoxHomePage.isPageOpened(), "Firefox home page is not opened!");
        Assert.assertEquals(firefoxHomePage.getDriver().getTitle(), "GSMArena.com - mobile phone reviews, news, specifications and more...");
        Screenshot.capture(firefoxHomePage.getDriver(), "Firefox capture!");

        NewsPage newsPage = chromeHomePage.getFooterMenu().openNewsPage();
        final String searchQ = "iphone";
        List<NewsItem> news = newsPage.searchNews(searchQ);
        Screenshot.capture(chromeHomePage.getDriver(), "Chrome capture!");
        Assert.assertFalse(CollectionUtils.isEmpty(news), "News not found!");

        for(NewsItem n : news) {
            System.out.println(n.readTitle());
            Assert.assertTrue(StringUtils.containsIgnoreCase(n.readTitle(), searchQ), "Invalid search results!");
        }

    }
}