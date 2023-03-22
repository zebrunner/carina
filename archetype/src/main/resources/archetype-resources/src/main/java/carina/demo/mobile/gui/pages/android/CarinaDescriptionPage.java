#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.mobile.gui.pages.android;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.mobile.gui.pages.common.CarinaDescriptionPageBase;
import ${package}.carina.demo.mobile.gui.pages.common.ChartsPageBase;
import ${package}.carina.demo.mobile.gui.pages.common.MapsPageBase;
import ${package}.carina.demo.mobile.gui.pages.common.UIElementsPageBase;
import ${package}.carina.demo.mobile.gui.pages.common.WebViewPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.utils.factory.DeviceType.Type;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

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
