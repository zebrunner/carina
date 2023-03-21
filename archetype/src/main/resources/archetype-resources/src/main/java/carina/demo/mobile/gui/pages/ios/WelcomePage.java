#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.mobile.gui.pages.ios;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.mobile.gui.pages.common.LoginPageBase;
import ${package}.carina.demo.mobile.gui.pages.common.WelcomePageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.utils.factory.DeviceType.Type;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.decorator.annotations.ClassChain;
import com.zebrunner.carina.webdriver.decorator.annotations.Predicate;

@DeviceType(pageType = Type.IOS_PHONE, parentClass = WelcomePageBase.class)
public class WelcomePage extends WelcomePageBase {

    @FindBy(xpath = "name = 'Welcome to Carina World!'")
    @Predicate
    private ExtendedWebElement title;

    @FindBy(xpath = "**/XCUIElementTypeButton[`name == 'NEXT'`]")
    @ClassChain
    private ExtendedWebElement nextBtn;

    public WelcomePage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isPageOpened() {
        return title.isElementPresent();
    }

    @Override
    public LoginPageBase clickNextBtn() {
        nextBtn.click();
        return initPage(getDriver(), LoginPageBase.class);
    }

}
