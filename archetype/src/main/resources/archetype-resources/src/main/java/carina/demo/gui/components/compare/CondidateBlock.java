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
package ${package}.carina.demo.gui.components.compare;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractUIObject;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class CondidateBlock extends AbstractUIObject
{
	@FindBy(xpath = ".//input[contains(@id, 'sSearch')]")
	private ExtendedWebElement inputField;

	@FindBy(xpath = ".//div[contains(@class, 'autocomplete-search')]//a[not(@class)]")
	private List<ExtendedWebElement> autocompleteSearchElements;

	public CondidateBlock(WebDriver driver, SearchContext searchContext)
	{
		super(driver, searchContext);
	}

	public void sendKeysToInputField(String text)
	{
		click(inputField);
		type(inputField, text);
	}

	public void getFirstPhone()
	{
		click(autocompleteSearchElements.get(0));
	}
}
