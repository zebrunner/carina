#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.mobile.gui.pages.android;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.mobile.gui.pages.common.ContactUsPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = ContactUsPageBase.class)
public class ContactUsPage extends ContactUsPageBase {

    @FindBy(xpath = "//input[@name='Email']")
    private ExtendedWebElement emailField;

    @FindBy(xpath = "//input[@name='Name']")
    private ExtendedWebElement nameField;

    @FindBy(xpath = "//textarea[@name='Textarea']")
    private ExtendedWebElement questionField;

    @FindBy(xpath = "//button[@type='submit' and text()='Send']")
    private ExtendedWebElement submitButton;

    @FindBy(xpath = "//div[contains(@class,'t-form__errorbox-text')]")
    private ExtendedWebElement errorLabel;

    @FindBy(id = "g-recaptcha-response")
    private ExtendedWebElement recaptcha;

    @FindBy(xpath = "//div[text()='Thank you for subscribing!']")
    private ExtendedWebElement successMessage;

    public ContactUsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void typeName(String name) {
        nameField.type(name);
    }

    @Override
    public void typeEmail(String email) {
        emailField.type(email);
    }

    @Override
    public void typeQuestion(String question) {
        questionField.type(question);
    }

    @Override
    public void submit() {
        hideKeyboard();
        submitButton.clickIfPresent();
    }

    @Override
    public boolean isErrorMessagePresent() {
        return errorLabel.isElementPresent();
    }

    @Override
    public boolean isRecaptchaPresent() {
        return recaptcha.isElementPresent();
    }

    public boolean isSuccessMessagePresent() {
        return successMessage.isPresent();
    }

}
