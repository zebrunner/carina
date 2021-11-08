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
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import ${package}.carina.demo.mobile.gui.pages.common.ContactUsPageBase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;


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
        submitButton.click();
    }

    @Override
    public boolean isErrorMessagePresent() {
        return errorLabel.isElementPresent();
    }

    @Override
    public boolean isRecaptchaPresent() {
        return recaptcha.isElementPresent();
    }

}
