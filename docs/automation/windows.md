#Windows

To do Windows application automation, you need to:

* Download windows [driver](https://github.com/Microsoft/WinAppDriver/releases)
* Enable developers mode for [Windows](https://docs.microsoft.com/en-us/windows/apps/get-started/enable-your-device-for-development)
* Start `Appium` or `WinAppDriver`
* Set carina's configuration

```
#path to your Appium/WinAppDriver server
selenium_url=http://localhost:4723/wd/hub

#Should be set to specify windows automation session
capabilities.platformName=Windows
capabilities.automationName=Windows

#path to application you want to test	
capabilities.app=C:/Windows/system32/win32calc.exe
```

That's all. To get access to the driver use getDriver(). Write and run tests as usual. If you want to pass specific parameters to WindowsDriver refer to Appium [documentation](https://github.com/appium/appium-windows-driver#windowsdriver-specific-capabilities).

Code example:

```java
//page.class example
public class CalculatorHomePage extends AbstractPage {
    @FindBy(xpath = "/Window/Pane/Button[10]")
    ExtendedWebElement fiveButton;
    
    @FindBy(xpath = "/Window/Pane/Button[5]")
    ExtendedWebElement oneButton;
    
    @FindBy(xpath = "/Window/Pane/Button[23]")
    ExtendedWebElement plusButton;
    
    @FindBy(xpath = "/Window/Pane/Button[28]")
    ExtendedWebElement resultButton;
    
    @FindBy(xpath = "/Window/Pane/Text[3]")
    ExtendedWebElement resultField;
    
    public CalculatorHomePage(WebDriver driver) {
        super(driver);
    }
    
    public void sumOneAndFive(){
        fiveButton.click();
        plusButton.click();
        oneButton.click();
        resultButton.click();
    }
    
    public String getResult(){
        return resultField.getText();
    }
}
```


```java
//test.class example
public class CalculatorTest implements IAbstractTest {
    @Test
    public void calculatorSumTest(){
        CalculatorHomePage calculator = new CalculatorHomePage(getDriver());
        calculator.sumOneAndFive();
        Assert.assertEquals(calculator.getResult().trim(), "6");
    }
}
```
