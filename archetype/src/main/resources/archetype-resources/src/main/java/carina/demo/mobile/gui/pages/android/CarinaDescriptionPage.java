#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*******************************************************************************
 * Copyright 2018-2022 Zebrunner Inc (https://www.zebrunner.com).
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
 *******************************************************************************/
package ${package}.carina.demo.mobile.gui.pages.android;

import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import ${package}.carina.demo.mobile.gui.pages.common.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

@DeviceType(pageType = Type.ANDROID_PHONE, parentClass = CarinaDescriptionPageBase.class)
public class CarinaDescriptionPage extends CarinaDescriptionPageBase {

    @FindBy(id = "content_frame")
    private ExtendedWebElement webViewContent;

    @FindBy(xpath = "//android.view.View[@text = 'CARINA facts']")
    private ExtendedWebElement carinaFactsSubTitle;

    @FindBy(xpath = "//android.widget.CheckedTextView[@text = 'Web View']")
    private ExtendedWebElement webViewLink;

    @FindBy(xpath = "//android.widget.CheckedTextView[@text = 'ChartsPage']")
    private ExtendedWebElement chartsLink;

    @FindBy(xpath = "//android.widget.CheckedTextView[@text = 'Map']")
    private ExtendedWebElement mapLink;

    @FindBy(xpath = "//android.widget.CheckedTextView[@text = 'UI elements']")
    private ExtendedWebElement uiElementsLink;

    @FindBy(className = "android.widget.ImageButton")
    private ExtendedWebElement leftMenuButton;

    public CarinaDescriptionPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public WebViewPageBase navigateToWebViewPage() {
        leftMenuButton.click();
        webViewLink.click();
        return initPage(getDriver(), WebViewPageBase.class);
    }

    @Override
    public ChartsPageBase navigateToChartsPage() {
        leftMenuButton.click();
        chartsLink.click();
        return initPage(getDriver(), ChartsPageBase.class);
    }

    @Override
    public MapsPageBase navigateToMapPage() {
        leftMenuButton.click();
        mapLink.click();
        return initPage(getDriver(), MapsPageBase.class);
    }

    @Override
    public UIElementsPageBase navigateToUIElementsPage() {
        leftMenuButton.click();
        uiElementsLink.click();
        return initPage(getDriver(), UIElementsPageBase.class);
    }

    @Override
    public boolean isPageOpened() {
        return webViewContent.isElementPresent();
    }

}
