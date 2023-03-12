package com.zebrunner.carina.webdriver.decorator;

import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class ExtendedWebElementTest {

    @Test
    public void test_isElementPresent_shouldReturnTrue_forVisibleElement() {
        // Create the mocks
        By by = Mockito.mock(By.class);
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        // Define the behavior for the mocks
        Mockito.when(driver.findElement(by)).thenReturn(foundElement);
        Mockito.when(foundElement.isDisplayed()).thenReturn(true);
        // Execute the test
        ExtendedWebElement element = new ExtendedWebElement(by, "testElementName", driver, driver);
        Assert.assertTrue(element.isElementPresent());
    }

    @Test
    public void test_isElementPresent_shouldReturnFalse_forNotVisibleElement() {
        // Create the mocks
        By by = Mockito.mock(By.class);
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        // Define the behavior for the mocks
        Mockito.when(driver.findElement(by)).thenReturn(foundElement);
        Mockito.when(foundElement.isDisplayed()).thenReturn(false);
        // Execute the test
        ExtendedWebElement element = new ExtendedWebElement(by, "testElementName", driver, driver);
        Assert.assertFalse(element.isElementPresent(0L));
    }

    @Test
    public void test_isElementPresent_shouldReturnFalse_forNotFoundElement() {
        // Create the mocks
        By by = Mockito.mock(By.class);
        WebDriver driver = Mockito.mock(WebDriver.class);
        // Define the behavior for the mocks
        Mockito.when(driver.findElement(by)).thenThrow(new NoSuchElementException("noElementFound"));
        // Execute the test
        ExtendedWebElement element = new ExtendedWebElement(by, "testElementName", driver, driver);
        Assert.assertFalse(element.isElementPresent(0L));
    }

    @Test
    public void test_isElementPresent_shouldReturnFalse_forStaleFoundElement() {
        // Create the mocks
        By by = Mockito.mock(By.class);
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        // Define the behavior for the mocks
        Mockito.when(driver.findElement(by)).thenReturn(foundElement);
        Mockito.when(foundElement.isDisplayed()).thenThrow(new StaleElementReferenceException("staleFoundElement"));
        // Execute the test
        ExtendedWebElement element = new ExtendedWebElement(by, "testElementName", driver, driver);
        Assert.assertFalse(element.isElementPresent(0L));
    }

    @Test
    public void test_isElementPresent_shouldReturnTrue_forVisibleElement_fromElementSearchContext() {
        // Create the mocks
        By by = Mockito.mock(By.class);
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement context = Mockito.mock(WebElement.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        // Define the behavior for the mocks
        Mockito.when(context.findElements(by)).thenReturn(List.of(foundElement));
        Mockito.when(foundElement.isDisplayed()).thenReturn(true);
        // Execute the test
        ExtendedWebElement element = new ExtendedWebElement(by, "testElementName", driver, context);
        Assert.assertTrue(element.isElementPresent());
    }

    @Test
    public void test_isElementPresent_shouldReturnFalse_forStaleSearchContext() {
        // Create the mocks
        By by = Mockito.mock(By.class);
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement context = Mockito.mock(WebElement.class);
        // Define the behavior for the mocks
        Mockito.when(context.findElements(by)).thenThrow(new StaleElementReferenceException("staleSearchContext"));
        // Execute the test
        ExtendedWebElement element = new ExtendedWebElement(by, "testElementName", driver, context);
        Assert.assertFalse(element.isElementPresent());
    }
}
