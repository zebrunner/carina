#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.mobile.gui.pages.ios;

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
import com.zebrunner.carina.webdriver.decorator.annotations.Predicate;

@DeviceType(pageType = Type.IOS_PHONE, parentClass = CarinaDescriptionPageBase.class)
public class CarinaDescriptionPage extends CarinaDescriptionPageBase {

    private static final String THIS_METHOD_IS_DEFINED_ONLY_IN_ANDROID = "This method is not yet implemented for iOS";

    @FindBy(xpath = "name = 'CARINA' AND type = 'XCUIElementTypeStaticText'")
    @Predicate
    private ExtendedWebElement webViewTitle;

    public CarinaDescriptionPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public WebViewPageBase navigateToWebViewPage() {
        throw new UnsupportedOperationException(THIS_METHOD_IS_DEFINED_ONLY_IN_ANDROID);
    }

    @Override
    public ChartsPageBase navigateToChartsPage() {
        throw new UnsupportedOperationException(THIS_METHOD_IS_DEFINED_ONLY_IN_ANDROID);
    }

    @Override
    public MapsPageBase navigateToMapPage() {
        throw new UnsupportedOperationException(THIS_METHOD_IS_DEFINED_ONLY_IN_ANDROID);
    }

    @Override
    public UIElementsPageBase navigateToUIElementsPage() {
        throw new UnsupportedOperationException(THIS_METHOD_IS_DEFINED_ONLY_IN_ANDROID);
    }

    @Override
    public boolean isPageOpened() {
        return webViewTitle.isElementPresent();
    }

}
