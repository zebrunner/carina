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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.utils.tag.Priority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestPriority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestTag;
import ${package}.carina.demo.gui.components.FooterMenu;
import ${package}.carina.demo.gui.components.NewsItem;
import ${package}.carina.demo.gui.components.compare.ModelSpecs;
import ${package}.carina.demo.gui.components.compare.ModelSpecs.SpecType;
import ${package}.carina.demo.gui.pages.BrandModelsPage;
import ${package}.carina.demo.gui.pages.CompareModelsPage;
import ${package}.carina.demo.gui.pages.HomePage;
import ${package}.carina.demo.gui.pages.ModelInfoPage;
import ${package}.carina.demo.gui.pages.NewsPage;

/**
 * This sample shows how create Web test.
 * 
 * @author qpsdemo
 */
public class WebSampleTest extends AbstractTest {
    @Test(description = "JIRA${symbol_pound}AUTO-0008")
    @MethodOwner(owner = "qpsdemo")
    @TestPriority(Priority.P3)
    public void testModelSpecs() {
        // Open GSM Arena home page and verify page is opened
        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");
        
        //Closing advertising if it's displayed
        homePage.getWeValuePrivacyAd().closeAdIfPresent();
        
        // Select phone brand
        homePage = new HomePage(getDriver());
        BrandModelsPage productsPage = homePage.selectBrand("Samsung");
        // Select phone model
        ModelInfoPage productInfoPage = productsPage.selectModel("Galaxy S10+");
        // Verify phone specifications
        Assert.assertEquals(productInfoPage.readDisplay(), "6.4\"", "Invalid display info!");
        Assert.assertEquals(productInfoPage.readCamera(), "16MP", "Invalid camera info!");
        Assert.assertEquals(productInfoPage.readRam(), "8/12GB RAM", "Invalid ram info!");
        Assert.assertEquals(productInfoPage.readBattery(), "4100mAh", "Invalid battery info!");
    }


    @Test(description = "JIRA${symbol_pound}AUTO-0009")
    @MethodOwner(owner = "qpsdemo")
    @TestPriority(Priority.P1)
    @TestTag(name = "area test", value = "web")
    public void testCompareModels() {
        // Open GSM Arena home page and verify page is opened
        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");
        // Open model compare page
        FooterMenu footerMenu = homePage.getFooterMenu();
        Assert.assertTrue(footerMenu.isUIObjectPresent(2), "Footer menu wasn't found!");
        CompareModelsPage comparePage = footerMenu.openComparePage();
        // Compare 3 models
        List<ModelSpecs> specs = comparePage.compareModels("Samsung Galaxy J3", "Samsung Galaxy J5", "Samsung Galaxy J7 Pro");
        // Verify model announced dates
        Assert.assertEquals(specs.get(0).readSpec(SpecType.ANNOUNCED), "2016, March 31");
        Assert.assertEquals(specs.get(1).readSpec(SpecType.ANNOUNCED), "2015, June 19");
        Assert.assertEquals(specs.get(2).readSpec(SpecType.ANNOUNCED), "2017, June");
    }
    
    @Test(description = "JIRA${symbol_pound}AUTO-0010")
    @MethodOwner(owner = "qpsdemo")
    public void testNewsSearch() {
        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");
        
        NewsPage newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");
        
        final String searchQ = "iphone";
        List<NewsItem> news = newsPage.searchNews(searchQ);
        Assert.assertFalse(CollectionUtils.isEmpty(news), "News not found!");
        for(NewsItem n : news) {
            System.out.println(n.readTitle());
            Assert.assertTrue(StringUtils.containsIgnoreCase(n.readTitle(), searchQ), "Invalid search results!");
        }
    }

}
