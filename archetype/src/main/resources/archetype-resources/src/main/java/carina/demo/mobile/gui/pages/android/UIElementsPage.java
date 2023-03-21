#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.mobile.gui.pages.android;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.mobile.gui.pages.common.UIElementsPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.utils.mobile.IMobileUtils;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = UIElementsPageBase.class)
public class UIElementsPage extends UIElementsPageBase implements IMobileUtils {

    @FindBy(id = "editText")
    private ExtendedWebElement textField;

    @FindBy(id = "editText2")
    private ExtendedWebElement emailField;

    @FindBy(id = "editText3")
    private ExtendedWebElement dateField;

    @FindBy(id = "checkBox2")
    private ExtendedWebElement checkBoxButton;

    @FindBy(id = "radioButton")
    private ExtendedWebElement maleRadioButton;

    @FindBy(id = "radioButton3")
    private ExtendedWebElement femaleRadioButton;

    @FindBy(id = "radioButton5")
    private ExtendedWebElement otherRadioButton;

    @FindBy(id = "radioButton5")
    private ExtendedWebElement seekBarRadioButton;

    @FindBy(className = "android.widget.ScrollView")
    private ExtendedWebElement container;

    public UIElementsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void typeText(String text) {
        textField.type(text);
    }

    @Override
    public void typeEmail(String email) {
        emailField.type(email);
    }

    @Override
    public String getEmail(){
        return emailField.getText();
    }

    @Override
    public String getText(){
        return textField.getText();
    }

    @Override
    public String getDate(){
        return dateField.getText();
    }

    @Override
    public void typeDate(String date) {
        dateField.type(date);
    }

    @Override
    public void clickOnMaleRadioButton() {
        maleRadioButton.click();
    }

    @Override
    public void clickOnFemaleRadioButton() {
        femaleRadioButton.click();
    }

    @Override
    public void clickOnOtherRadioButton() {
        otherRadioButton.click();
    }

    @Override
    public boolean isFemaleRadioButtonSelected(){
        return femaleRadioButton.isChecked();
    }

    @Override
    public boolean isOthersRadioButtonSelected(){
        return otherRadioButton.isChecked();
    }

    @Override
    public void checkCopy() {
        checkBoxButton.click();
    }

    @Override
    public boolean isCopyChecked(){
        return checkBoxButton.isChecked();
    }

    public void swipeToFemaleRadioButton() {
        swipe(femaleRadioButton, container, 10);
    }

}
