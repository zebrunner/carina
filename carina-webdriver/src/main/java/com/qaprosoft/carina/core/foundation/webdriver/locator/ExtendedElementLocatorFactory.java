/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.lang.reflect.Field;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.CaseInsensitiveXPath;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Localized;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.CaseInsensitiveConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.ParamsToConvert;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.Platform;
import com.zebrunner.carina.utils.Configuration;

public final class ExtendedElementLocatorFactory implements ElementLocatorFactory {
    private final SearchContext searchContext;
    
    private boolean isRootElementUsed;

    public ExtendedElementLocatorFactory(SearchContext searchContext, boolean isRootElementUsed) {
        this.searchContext = searchContext;
        this.isRootElementUsed = isRootElementUsed;
    }
    
	public boolean isRootElementUsed() {
		return isRootElementUsed;
	}

    public ElementLocator createLocator(Field field) {
        ExtendedElementLocator locator = null;
        if (field.isAnnotationPresent(FindBy.class) || field.isAnnotationPresent(ExtendedFindBy.class)) {
            LocalizedAnnotations annotations = new LocalizedAnnotations(field);
            By by = annotations.buildBy();
            if (field.isAnnotationPresent(CaseInsensitiveXPath.class)) {
                CaseInsensitiveXPath csx = field.getAnnotation(CaseInsensitiveXPath.class);
                Platform platform = Objects.equals(Configuration.getMobileApp(), "") ? Platform.WEB : Platform.MOBILE;
                by = new CaseInsensitiveConverter(new ParamsToConvert(csx.id(), csx.name(), csx.text(), csx.classAttr()), platform)
                        .convert(by);
            }


            locator = new ExtendedElementLocator(searchContext, field, by);
            locator.setIsLocalized(field.isAnnotationPresent(Localized.class));
        }
        return locator;
    }
}