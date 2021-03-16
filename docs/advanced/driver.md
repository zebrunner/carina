#Driver usage

Currently supported browsers by Carina:

* Chrome
* Firefox
* Internet Explorer
* Microsoft Edge
* Opera
* Safari

##Initialization
Driver can be started in several ways:

* **getDriver()** is the core method to start any Selenium/Appium session. It will create a RemoteWebDriver named "default" based on default capabilities from configuration.

1st call of the method in current thread should start new driver. Next calls will return existing object. Based on the driver lifecycle carina do automatic driver quit.

* **getDriver(String name)** start named driver session using default capabilities from configuration. That's allow to start several drivers (up to 3 according to `max_driver_count` property)

* **getDriver(String name, DesiredCapabilities capabilities).** start named driver session using custom capabilities.

Example:
```
@Test
public void carinaCapsTest() {
    // Chrome default capabilities builder usage:
    WebDriver chromeDriver = getDriver("chrome", new ChromeCapabilities().getCapability("Chrome Test"));
    HomePage homePageChrome = new HomePage(chromeDriver);
    homePageChrome.open();

    // Firefox default capabilities builder usage:
    getDriver("firefox", new FirefoxCapabilities().getCapability("Firefox Test"));
    HomePage homePageFirefox = new HomePage(getDriver("firefox")); // return named "firefox" driver from the pool to init HomePage.
    homePageFirefox.open();

    // Safari, Edge, Opera and IE default capabilities builder usage:
    HomePage homePageSafari = new HomePage(getDriver("safari", new SafariCapabilities().getCapability("Safari test")));
    homePageSafari.open();
    
    getDriver("edge", new EdgeCapabilities().getCapability("Edge test"));
    getDriver("opera", new OperaCapabilities().getCapability("Opera test"));
    getDriver("ie", new IECapabilities().getCapability("Internet Explorer test"));

}

@Test
public void desiredCapsTest() {
    // Manage DesiredCapabilities on your own to build complicated caps structure:
    DesiredCapabilities capabilities = DesiredCapabilities.safari();
    capabilities.setCapability(CapabilityType.PLATFORM_NAME, SpecialKeywords.MAC);

    HomePage safariHomePage = new HomePage(getDriver("safari", capabilities));
    safariHomePage.open();

    Assert.assertTrue(safariHomePage.isPageOpened());
}
```

* **getDriver(String name, DesiredCapabilities capabilities, String seleniumHost).** start named driver session using custom capabilities using custom selenium url.
 
##Capabilities 

Simple key value Selenium/Appium capability pairs can be provided in _config.properties using **capabilities.** prefix.
```
capabilities.automationName=uiautomator2
capabilities.deviceName=Samsung_Galaxy_S10
capabilities.platformName=ANDROID
capabilities.app=https://qaprosoft.s3-us-west-2.amazonaws.com/carinademoexample.apk
capabilities.newCommandTimeout=180
```

> Visit [selenium](https://github.com/SeleniumHQ/selenium/wiki/DesiredCapabilities) or [appium](https://appium.io/docs/en/writing-running-appium/caps/) to see all capabilities.

##Options 

Options and arguments could be provided through _config.properties as well using comma separated values for multiple options/args.
```
firefox_args=--no-first-run,--disable-notifications
firefox_preferences=
chrome_args=
chrome_experimental_opts=
chrome_mobile_emulation_opts=
```

To be able to provide complicated structures use advanced approach to build capabilities/options/arguments
```
public void someTest() {
    FirefoxOptions options = new FirefoxOptions();
    options.addArguments("--no-first-run");
    options.addArguments("--disable-notifications");

    DesiredCapabilities capabilities = DesiredCapabilities.firefox();
    capabilities.setCapability(CapabilityType.PLATFORM_NAME, SpecialKeywords.MAC);
    capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);

    HomePage homePage = new HomePage(getDriver("firefox", capabilities));
    homePage.open();

    Assert.assertTrue(homePage.isPageOpened());
}
```
