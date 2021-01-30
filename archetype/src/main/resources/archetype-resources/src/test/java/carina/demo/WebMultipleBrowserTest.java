#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013-2021 QAPROSOFT (http://qaprosoft.com/).
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
 */
package ${package}.carina.demo;

import com.qaprosoft.carina.core.foundation.AbstractTest;
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
public class WebMultipleBrowserTest extends AbstractTest {

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