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
package com.zebrunner.carina.webdriver.locator;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openqa.selenium.By;
import org.openqa.selenium.support.AbstractFindByBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.Base64;

import io.appium.java_client.AppiumBy;

public abstract class ExtendedFindByBuilder extends AbstractFindByBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
     
    private static final String BY_TEXT_TEMPLATE = "//*[@text = \"%s\"]";

    protected By buildByFromShortFindBy(ExtendedFindBy findByCarina) {
        if (!"".equals(findByCarina.iosPredicate())) {
            return AppiumBy.iOSNsPredicateString(findByCarina.iosPredicate());
        }
        
        if (!"".equals(findByCarina.androidUIAutomator())) {
            return AppiumBy.androidUIAutomator(findByCarina.androidUIAutomator());
          }

        if (!"".equals(findByCarina.iosClassChain())) {
            return AppiumBy.iOSClassChain(findByCarina.iosClassChain());
        }
        
        if (!"".equals(findByCarina.accessibilityId())) {
            return AppiumBy.accessibilityId(findByCarina.accessibilityId());
        }
        
        if (!"".equals(findByCarina.text())) {
            return By.xpath(String.format(BY_TEXT_TEMPLATE, findByCarina.text()));
        }
      
        if (!"".equals(findByCarina.image())) {
            if (findByCarina.image().contains("%")) {
                LOGGER.debug("Special char has been detected in the image locator. Call format method on element before interaction.");
                return AppiumBy.image(ClassLoader.getSystemResource("").getPath() + findByCarina.image());
            }
            URL fileUrl = ClassLoader.getSystemResource(findByCarina.image());
            Path path;
            if (null != fileUrl) {
                LOGGER.debug("ExtendedFindBy annotation image locator : " + fileUrl.getPath());
                try {
                    path = Paths.get(fileUrl.toURI());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error while reading system resource for ExtendedFindBy annotation. Check if image exists in class path: "
                            + ClassLoader.getSystemResource("") + findByCarina.image());
                }
            } else {
                throw new RuntimeException("Error while reading system resource for ExtendedFindBy annotation. Check if image exists in class path: "
                        + ClassLoader.getSystemResource("") + findByCarina.image());
            }
            LOGGER.debug("Path to search image template : " + path);
            String base64image = null;
            try {
                base64image = new String(Base64.encode(Files.readAllBytes(path)));
                LOGGER.debug("Base64 image representation has been successfully obtained.");
                return AppiumBy.image(base64image);
            } catch (IOException e) {
                throw new RuntimeException("Error while reading image file for ExtendedFindBy annotation : " + fileUrl.getPath(), e);
            }
        }

        if (!"".equals(findByCarina.androidDataMatcher())) {
            return AppiumBy.androidDataMatcher(findByCarina.androidDataMatcher());
        }

        if (!"".equals(findByCarina.androidViewMatcher())) {
            return AppiumBy.androidViewMatcher(findByCarina.androidViewMatcher());
        }

        if (!"".equals(findByCarina.androidViewTag())) {
            return AppiumBy.androidViewTag(findByCarina.androidViewMatcher());
        }

        if (!"".equals(findByCarina.custom())) {
            return AppiumBy.custom(findByCarina.androidViewMatcher());
        }

        return null;
    }
}
