All the described methods are implemented inside `IDriverPool` interface and accessible by default from any test class. 
To access this functionality from another places like your services, just implement this interface.

> Each thread has their own named driver pool

Supported Browsers: Chrome, Firefox, Microsoft Edge, Opera, Safari, etc.

## Initialization

* **getDriver()** is the core method to start any Selenium/Appium session. It will create a RemoteWebDriver named "default" based on the default capabilities from configuration.
  > 1st call of the method in the current thread should starta  new driver. Next calls will return existing object.

* **getDriver(String name)** start named driver session using default capabilities from configuration. This allows to start several drivers (up to 3 according to `max_driver_count` property).

* **getDriver(String name, MutableCapabilities capabilities)** start named driver session using custom capabilities.

* **getDriver(String name, MutableCapabilities capabilities, String seleniumHost)** start named driver session using custom capabilities vs custom selenium URL.

Example:
```
@Test
public void carinaCapsTest() {
    // Chrome default capabilities builder usage:
    WebDriver chromeDriver = getDriver("chrome", new ChromeCapabilities().getCapability("Chrome Browser"));
    HomePage homePageChrome = new HomePage(chromeDriver);
    homePageChrome.open();

    // Firefox default capabilities builder usage:
    getDriver("firefox", new FirefoxCapabilities().getCapability("Firefox Browser"));
    HomePage homePageFirefox = new HomePage(getDriver("firefox")); // return named "firefox" driver from the pool to init HomePage.
    homePageFirefox.open();

    // Safari, Edge and Opera default capabilities builder usage:
    HomePage homePageSafari = new HomePage(getDriver("safari", new SafariCapabilities().getCapability("Safari Browser")));
    homePageSafari.open();
    
    getDriver("edge", new EdgeCapabilities().getCapability("Edge Browser"));
    getDriver("opera", new OperaCapabilities().getCapability("Opera Browser"));
}

@Test
public void mutableCapsTest() {
    // Manage MutableCapabilities on your own to build complicated caps structure:
    MutableCapabilities capabilities = new MutableCapabilities();
    capabilities.setCapability(CapabilityType.BROWSER_NAME, Browser.SAFARI.browserName());
    capabilities.setCapability(CapabilityType.PLATFORM_NAME, SpecialKeywords.MAC);

    HomePage safariHomePage = new HomePage(getDriver("safari", capabilities));
    safariHomePage.open();

    Assert.assertTrue(safariHomePage.isPageOpened());
}
```

### Capabilities 

Primitive Selenium/Appium capabilities can be provided in `_config.properties` using `capabilities.name=value`:
```
capabilities.automationName=uiautomator2
capabilities.deviceName=Samsung_Galaxy_S10
capabilities.platformName=ANDROID
capabilities.app=https://qaprosoft.s3-us-west-2.amazonaws.com/carinademoexample.apk
capabilities.newCommandTimeout=180
```

Visit these resources to see all capabilities:
- [Selenium](https://w3c.github.io/webdriver/#capabilities)
- [Appium - XCUITest](https://github.com/appium/appium-xcuitest-driver#capabilities)
- [Appium - UiAutomator2](https://github.com/appium/appium-uiautomator2-driver#capabilities)
- [Appium - Espresso](https://github.com/appium/appium-espresso-driver#capabilities)
- [Appium - Gecko](https://github.com/appium/appium-geckodriver#usage)
- [Appium - Safari](https://github.com/appium/appium-safari-driver#usage)
- [Appium - Windows](https://github.com/appium/appium-windows-driver#usage)

You can also look at outdated list of capabilities:
- [Selenium](https://www.selenium.dev/documentation/legacy/desired_capabilities/)
- [Appium](https://appium.io/docs/en/writing-running-appium/caps/)

**Important**: capabilities should not be added to the configuration file with the `appium:` prefix!
If you want the capabilities to be handled according to w3c, set the value of the w3c parameter in the config file to true.
Also, if you use w3c mode, and at the same time use the capabilities specific to the service where you 
run the tests, add `capabilities.provider` to the config file, for example `capabilities.provider=zebrunner`.

The most popular capabilities sets can be declared in properties files and reused via `custom_capabilities` configuration parameter.
It can be convenient for external hub providers like Zebrunner Device Farm, BrowserStack, Sauce Labs, etc.

Collect device/browser specific capabilities and put into `src/main/resources/browserstack-iphone_12.properties`:

```
capabilities.realMobile=true
capabilities.platformName=iOS
capabilities.deviceName=iPhone 12
capabilities.osVersion=14
#capabilities.app=bs://444bd0308813ae0dc236f8cd461c02d3afa7901d
#capabilities.browserstack.local=false
#capabilities.appiumVersion=1.20.2
#capabilities.deviceOrientation=portrait
```

Put `custom_capabilities=browserstack-iphone_12.properties` into the **_config.properties** to start all tests on this device.

### Options 

Options and arguments can be provided through `_config.properties` using comma-separated values for multiple options/args, for example:
```
firefox_args=--no-first-run,--disable-notifications
firefox_preferences=
chrome_args=
chrome_experimental_opts=
chrome_mobile_emulation_opts=
```

To provide complicated structures, use the advanced approach to build capabilities/options/arguments:
```
public void someTest() {
  FirefoxOptions options = new FirefoxOptions();
  options.addArguments("--no-first-run");
  options.addArguments("--disable-notifications");
  options.setPlatformName(SpecialKeywords.MAC);

  HomePage homePage = new HomePage(getDriver("firefox", options));
  homePage.open();

  Assert.assertTrue(homePage.isPageOpened());
}
```

## Quit
Quit driver operation is executed automatically based on the driver init phase, i.e. **no need to do it inside your test code**.

* `@BeforeSuite` drivers belong to all tests/classes and will be closed at `@AfterSuite` only
* `@BeforeTest` drivers belong to all `<test>` classes and will be closed at `@AfterTest`.
* `@BeforeClass` drivers belong to all tests inside current class and will be closed at `@AfterClass`
* `@BeforeMethod` or inside `Test Method` drivers belong to current test method and will be closed at `@AfterMethod`
  > For dependent test methods Carina preserve started driver(s) by default.

To quit driver forcibly, use **quitDriver()** or **quitDriver(name)**

To disable driver quit strategy completely and cotrol drivers init/quit on your own, provide `forcibly_disable_driver_quit=true` or execute from any place of your test code `CarinaListener.disableDriversCleanup();`

## Restart
* **restartDriver()** quit the current driver and start a new one with the same capabilities
* **restartDriver(boolean isSameDevice)** quit the current driver and start a new one on the same device using `uuid` capability. It is fully compatible with [MCloud](https://github.com/zebrunner/mcloud) farm.

## FAQ

**Where is a valid place to init drivers and pages?**

Init pages and drivers inside test methods where they are actually used. Escape declaring pages and drivers on the class level as it produces extra complexity in execution, maintenance and support!
```
public class TestSample implements IAbstractTest {
  @Test(){
  public void someTest(){
     //Page declared and initialized right before using it
     HomePage homePage = new HomePage(getDriver());
     homePage.open();
     FooterMenu footerMenu = homePage.getFooterMenu();
     CompareModelsPage comparePage = footerMenu.openComparePage();
     List<ModelSpecs> specs = comparePage.compareModels("Samsung Galaxy J3", "Samsung Galaxy J5", "Samsung Galaxy J7 Pro");
  }
}
```

Anti-pattern:
```
public class TestSample implements IAbstractTest {
  HomePage homePage = new HomePage(driver);
  WebDriver driver;
  FooterMenu footerMenu;
  
  @BeforeSuite
  public void driverInit(){
      driver = getDriver();
  }
  
  @Test(){
  public void someTest(){
     homePage.open();
     footerMenu = homePage.getFooterMenu();
     CompareModelsPage comparePage = footerMenu.openComparePage();
     List<ModelSpecs> specs = comparePage.compareModels("Samsung Galaxy J3", "Samsung Galaxy J5", "Samsung Galaxy J7 Pro");
  }
}
```

**May I init page/driver on static layer?**

Initialization of drivers and pages on the static layer is prohibited. CarinaListener cannot be even integrated at the compilation stage. For details, please visit [#1550](https://github.com/zebrunner/carina/issues/1550).
The earliest stage you can start driver is `@BeforeSuite()`.

**How to start different tests on different devices?**

Start driver with custom MutableCapabilities to launch on different devices. Also, you can use `CapabilitiesLoader` to manage capabilities at run-time:

```
// Update default capabilities globally to start future drivers **for all tests** on iPhone_12 
new CapabilitiesLoader().loadCapabilities("browserstack-iphone_12.properties");

// Update default capabilities to start future drivers **for this test only** on iPhone_12 
new CapabilitiesLoader().loadCapabilities("browserstack-iphone_12.properties", true);

// start new driver with generated capabilities from properties file:
WebDriver drv = getDriver("iPhone12", new CapabilitiesLoader().getCapabilities("browserstack-iphone_12.properties"))
```
