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
package ${package}.carina.demo.mobile.gui.pages.common;

import com.qaprosoft.carina.core.gui.AbstractPage;
import org.openqa.selenium.WebDriver;

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
