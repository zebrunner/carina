#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.mobile.gui.pages.common;

import org.openqa.selenium.WebDriver;

import com.zebrunner.carina.webdriver.gui.AbstractPage;

public abstract class CarinaDescriptionPageBase extends AbstractPage {

    public CarinaDescriptionPageBase(WebDriver driver) {
        super(driver);
    }

    public abstract WebViewPageBase navigateToWebViewPage();

    public abstract ChartsPageBase navigateToChartsPage();

    public abstract MapsPageBase navigateToMapPage();

    public abstract UIElementsPageBase navigateToUIElementsPage();

}
