package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openqa.selenium.By;
import org.openqa.selenium.support.AbstractFindByBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.Base64;

import io.appium.java_client.MobileBy;

public abstract class ExtendedFindByBuilder extends AbstractFindByBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
     
    private static final String BY_TEXT_TEMPLATE = "//*[@text = \"%s\"]";

    protected By buildByFromShortFindBy(ExtendedFindBy findByCarina) {
        if (!"".equals(findByCarina.iosPredicate())) {
          return MobileBy.iOSNsPredicateString(findByCarina.iosPredicate());
        }
        
        if (!"".equals(findByCarina.androidUIAutomator())) {
            return MobileBy.AndroidUIAutomator(findByCarina.androidUIAutomator());
          }

        if (!"".equals(findByCarina.iosClassChain())) {
          return MobileBy.iOSClassChain(findByCarina.iosClassChain());
        }
        
        if (!"".equals(findByCarina.accessibilityId())) {
            return MobileBy.AccessibilityId(findByCarina.accessibilityId());
        }
        
        if (!"".equals(findByCarina.text())) {
            return By.xpath(String.format(BY_TEXT_TEMPLATE, findByCarina.text()));
        }
      
        if (!"".equals(findByCarina.image())) {
            if (findByCarina.image().contains("%")) {
                LOGGER.debug("Special char has been detected in the image locator. Call format method on element before interaction.");
                return MobileBy.image(ClassLoader.getSystemResource("").getPath() + findByCarina.image());
            }
            String annotationLocator;
            URL fileUrl = ClassLoader.getSystemResource(findByCarina.image());
            if (null != fileUrl) {
                annotationLocator = fileUrl.getPath();
            } else {
                throw new RuntimeException("Error while reading system resource for ExtendedFindBy annotation. Check if image exists in class path: "
                        + ClassLoader.getSystemResource("") + findByCarina.image());
            }  
            LOGGER.debug("ExtendedFindBy annotation image locator : " + annotationLocator);
            Path path = Paths.get(annotationLocator);
            LOGGER.debug("Path to search image template : " + path);
            String base64image = null;
            try {
                base64image = new String(Base64.encode(Files.readAllBytes(path)));
                LOGGER.debug("Base64 image representation has been successfully obtained.");
                return MobileBy.image(base64image);
            } catch (IOException e) {
                throw new RuntimeException("Error while reading image file for ExtendedFindBy annotation : " + annotationLocator, e);
            }
        }
        return null;
    }
}
