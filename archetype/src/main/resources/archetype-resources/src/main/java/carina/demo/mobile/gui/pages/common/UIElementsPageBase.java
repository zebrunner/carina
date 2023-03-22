#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.mobile.gui.pages.common;

import org.openqa.selenium.WebDriver;

import com.zebrunner.carina.webdriver.gui.AbstractPage;

public abstract class UIElementsPageBase extends AbstractPage {

    public UIElementsPageBase(WebDriver driver) {
        super(driver);
    }

    public abstract void typeText(String text);

    public abstract void typeEmail(String email);

    public abstract String getEmail();

    public abstract String getText();

    public abstract String getDate();

    public abstract void typeDate(String date);

    public abstract void clickOnMaleRadioButton();

    public abstract void clickOnFemaleRadioButton();

    public abstract boolean isFemaleRadioButtonSelected();

    public abstract void clickOnOtherRadioButton();

    public abstract boolean isOthersRadioButtonSelected();

    public abstract void checkCopy();

    public abstract boolean isCopyChecked();

    public abstract void swipeToFemaleRadioButton();

}
