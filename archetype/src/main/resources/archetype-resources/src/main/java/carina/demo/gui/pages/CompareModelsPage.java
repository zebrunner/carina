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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractPage;
import ${package}.carina.demo.gui.components.compare.CondidateBlock;
import ${package}.carina.demo.gui.components.compare.ModelSpecs;

public class CompareModelsPage extends AbstractPage
{
	@FindBy(xpath = "//div[contains(@class, 'candidate-search')]")
	private List<CondidateBlock> condidateBlocks;

	public CompareModelsPage(WebDriver driver)
	{
		super(driver);
	}

	public List<ModelSpecs> compareModels(String... models)
	{
		CondidateBlock condidateBlock;
		List<ModelSpecs> modelSpecs = new ArrayList<>();
		ModelSpecs modelSpec;
		for (int index = 0; index < models.length; index++)
		{
			modelSpec = new ModelSpecs();
			condidateBlock = condidateBlocks.get(index);
			condidateBlock.sendKeysToInputField(models[index]);
			condidateBlock.getFirstPhone();
			for (ModelSpecs.SpecType type : ModelSpecs.SpecType.values())
			{
				ExtendedWebElement spec = findExtendedWebElement(By.xpath(
						String.format("//tr[.//a[text()='%s']]//td[@class='nfo'][%d]", type.getType(), index + 1)));
				modelSpec.setToModelSpecsMap(type, spec.getText());
			}
			modelSpecs.add(modelSpec);
		}
		return modelSpecs;
	}
}
