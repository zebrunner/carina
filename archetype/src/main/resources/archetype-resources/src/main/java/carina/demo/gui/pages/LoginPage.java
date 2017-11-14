#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013-2017 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ${package}.carina.demo.gui.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.webdriver.ai.FindByAI;
import com.qaprosoft.carina.core.foundation.webdriver.ai.Label;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractPage;

public class LoginPage extends AbstractPage
{
	@FindBy(name = "email")
	private ExtendedWebElement emailTextField;

	@FindBy(name = "password")
	private ExtendedWebElement passwordTextField;

	@FindBy(xpath = "//input[@value='Log in']")
	private ExtendedWebElement signInButton;

	@FindByAI(caption = "Google", label = Label.BUTTON)
	private ExtendedWebElement googleButton;

	public LoginPage(WebDriver driver)
	{
		super(driver);
		setPageAbsoluteURL("https://stackoverflow.com/users/login");
	}

	public void signIn(String email, String password)
	{
		emailTextField.type(email);
		passwordTextField.type(password);
		signInButton.click();
	}

	// AI usage
	public void signInViaGoogle()
	{
		googleButton.click();
	}
}