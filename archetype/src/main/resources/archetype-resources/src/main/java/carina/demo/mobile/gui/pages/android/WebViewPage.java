#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.mobile.gui.pages.android;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.mobile.gui.pages.common.ContactUsPageBase;
import ${package}.carina.demo.mobile.gui.pages.common.WebViewPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = WebViewPageBase.class)
public class WebViewPage extends WebViewPageBase {

    @FindBy(xpath = "//*[text()='Get a quote']")
    private ExtendedWebElement contactUsLink;

    public WebViewPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public ContactUsPageBase goToContactUsPage() {
        swipe(contactUsLink);
        contactUsLink.click();
        pause(7);
        return initPage(getDriver(), ContactUsPageBase.class);
    }

}
