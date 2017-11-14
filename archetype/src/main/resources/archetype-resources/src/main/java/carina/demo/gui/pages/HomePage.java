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

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractPage;
import ${package}.carina.demo.gui.components.FooterMenu;

public class HomePage extends AbstractPage
{
	@FindBy(id = "footmenu")
	private FooterMenu footerMenu;
	
	@FindBy(xpath = "//div[contains(@class, 'brandmenu-v2')]//a")
	private List<ExtendedWebElement> brandLinks;

	public HomePage(WebDriver driver)
	{
		super(driver);
	}

	public FooterMenu getFooterMenu()
	{
		return footerMenu;
	}
	
	public BrandModelsPage selectBrand(String brand)
	{
		for(ExtendedWebElement brandLink : brandLinks)
		{
			if(brand.equalsIgnoreCase(brandLink.getText()))
			{
				brandLink.click();
				return new BrandModelsPage(driver);
			}
		}
		throw new RuntimeException("Unable to open brand: " + brand);
	}
}
