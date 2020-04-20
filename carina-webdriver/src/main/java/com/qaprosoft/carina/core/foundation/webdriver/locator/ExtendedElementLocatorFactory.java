/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

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
        return new ExtendedElementLocator(searchContext, field);
    }
}