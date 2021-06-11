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

import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.PageOpeningStrategy;
import ${package}.carina.demo.gui.pages.CompareModelsPage;
import ${package}.carina.demo.gui.pages.HomePage;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * This sample shows how works page opening strategy
 *
 * @author qpsdemo
 */
public class PageOpeningStrategySampleTest implements IAbstractTest {

    @Test
    @MethodOwner(owner = "qpsdemo")
    public void testByElementOpeningStrategy(){

        SoftAssert softAssert = new SoftAssert();

        HomePage homePage = new HomePage(getDriver());
        homePage.open();

        homePage.setPageOpeningStrategy(PageOpeningStrategy.BY_ELEMENT);

        softAssert.assertTrue(homePage.isPageOpened(),"Home page is not opened");

        CompareModelsPage compareModelsPage = homePage.getFooterMenu().openComparePage();
        compareModelsPage.setPageOpeningStrategy(PageOpeningStrategy.BY_ELEMENT);

        softAssert.assertTrue(compareModelsPage.isPageOpened(), "Compare page is not opened");

        softAssert.assertAll();
    }

    @Test
    @MethodOwner(owner = "qpsdemo")
    public void testByURLOpeningStrategy(){
        SoftAssert softAssert = new SoftAssert();
        HomePage homePage = new HomePage(getDriver());
        homePage.open();

        homePage.setPageOpeningStrategy(PageOpeningStrategy.BY_URL);

        softAssert.assertTrue(homePage.isPageOpened(),"Home page is not opened");

        CompareModelsPage compareModelsPage = homePage.getFooterMenu().openComparePage();
        compareModelsPage.setPageOpeningStrategy(PageOpeningStrategy.BY_URL);

        softAssert.assertTrue(compareModelsPage.isPageOpened(), "Compare page is not opened");

        softAssert.assertAll();
    }

    @Test
    @MethodOwner(owner = "qpsdemo")
    public void testByUrlAndElementOpeningStrategy() {
        SoftAssert softAssert = new SoftAssert();
        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        homePage.setPageOpeningStrategy(PageOpeningStrategy.BY_URL_AND_ELEMENT);

        softAssert.assertTrue(homePage.isPageOpened(),"Home page is not opened");

        CompareModelsPage compareModelsPage = homePage.getFooterMenu().openComparePage();
        compareModelsPage.setPageOpeningStrategy(PageOpeningStrategy.BY_URL_AND_ELEMENT);

        softAssert.assertTrue(compareModelsPage.isPageOpened(), "Compare page is not opened");

        softAssert.assertAll();
    }
}
