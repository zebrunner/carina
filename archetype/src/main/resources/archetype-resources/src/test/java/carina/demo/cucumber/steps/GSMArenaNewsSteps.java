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
package ${package}.carina.demo.cucumber.steps;


import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.cucumber.CucumberRunner;
import ${package}.carina.demo.gui.components.NewsItem;
import ${package}.carina.demo.gui.pages.HomePage;
import ${package}.carina.demo.gui.pages.NewsPage;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;


public class GSMArenaNewsSteps extends CucumberRunner {
    
    HomePage homePage = null;
    NewsPage newsPage = null;

    @Given("^I am on main page")
    public boolean iAmOnMainPage() {
        homePage = new HomePage(getDriver());
        homePage.open();
        return homePage.isPageOpened();
    }
    
    @When("^I open 'News' page${symbol_dollar}")
    public void iOpenNewsPage()  {
        newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");
    }

    @Then("^page 'News' should be open${symbol_dollar}")
    public void pageSettingsShouldBeOpen() {
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");
    }
    
    @And("^page 'News' should contains all items${symbol_dollar}")
    public void pageSettingsShouldContainsAllItems() {
        final String searchQ = "iphone";
        List<NewsItem> news = newsPage.searchNews(searchQ);
        Assert.assertFalse(CollectionUtils.isEmpty(news), "News not found!");
        for(NewsItem n : news) {
            System.out.println(n.readTitle());
            Assert.assertTrue(StringUtils.containsIgnoreCase(n.readTitle(), searchQ), "Invalid search results!");
        }
    }

}
