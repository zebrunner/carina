# Driver configuration

In Carina WebDriver is initialized by method **getDriver()**.

Currently supported browsers by Carina:

* Chrome
* Firefox
* Internet Explorer
* Microsoft Edge
* Opera
* Safari

## Carina's webdriver initialization

* **getDriver()**

If method called for the first time Carina will initialize new driver with capabilities from _config.properties file. The driver's name will be set as **"default"**. So it means that we can access this driver by calling method getDriver() or getDriver("default").

* **getDriver(String name).**

If there is no driver in Driver pool with name we passed Carina will create a new WebDriver with capabilities from _config.properties file. We can access the driver by his name. By using different names we can create several automation sessions with same capabilities.

* **getDriver(String name, DesiredCapabilities capabilities).**

In this case we need to declare our custom capabilities. There are two ways: 

1. Use Carina's capabilities templates for different browsers.
2. Describe DesiredCapabilities inside your Test.class. 

### Carina's capabilities templates:
```java
@Test
public void someTest() {

    WebDriver chromeDriver = getDriver("chrome", new ChromeCapabilities().getCapability("Chrome Test"));
    HomePage homePageChrome = new HomePage(chromeDriver);
    homePageChrome.open();
    //or
    getDriver("firefox", new FirefoxCapabilities().getCapability("Firefox Test"));
    HomePage homePageFirefox = new HomePage(getDriver("firefox"));
    homePageFirefox.open();
    //or
    HomePage homePageSafari = new HomePage(
        getDriver("safari", new SafariCapabilities().getCapability("Safari test")));
    homePageSafari.open();
    //etc
    getDriver("edge", new EdgeCapabilities().getCapability("Edge test"));
    getDriver("opera", new OperaCapabilities().getCapability("Opera test"));
    getDriver("ie", new IECapabilities().getCapability("Internet Explorer test"));
}
```
You can decide the max number of drivers by parameter **max_driver_count** in **_config.properties** (3 by default).

New driver could be accessed by init name.
```
getDriver("safari");
```

### Creating custom capabilities:
```java
@Test
public void someTest() {
    DesiredCapabilities capabilities = DesiredCapabilities.safari();
    capabilities.setBrowserName("safari");
    capabilities.setCapability(CapabilityType.PLATFORM_NAME, SpecialKeywords.MAC);
    
    HomePage safariHomePage = new HomePage(getDriver("safari",capabilities));
    safariHomePage.open();
    
    Assert.assertTrue(safariHomePage.isPageOpened());
}
```

List of all selenium [capabilities](https://github.com/SeleniumHQ/selenium/wiki/DesiredCapabilities).

* **getDriver(String name, DesiredCapabilities capabilities, String seleniumHost).**

There we also need to pass selenium server host. In case of seleniumHost == null Carina will use your _config.properties selenium_host parameter.
 
### Additional capabilities

There is also a possibility to send additional capabilities to Carina through _config.properties. All parameters with prefix **capabilities.** will be added to the driver’s capabilities.
```
capabilities.platformName=MAC
```

## Options 

Options are used to tune your browser for tests. Available for Chrome and Firefox. 
``` java
public void someTest() {
    FirefoxOptions options = new FirefoxOptions();
    options.addArguments("--no-first-run");
    options.addArguments("--disable-notifications");

    DesiredCapabilities capabilities = DesiredCapabilities.firefox();
    capabilities.setBrowserName("firefox");
    capabilities.setCapability(CapabilityType.PLATFORM_NAME, SpecialKeywords.MAC);
    capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS,options);

    HomePage safariHomePage = new HomePage(getDriver("firefox",capabilities));
    safariHomePage.open();

    Assert.assertTrue(safariHomePage.isPageOpened());
}
```
Options also could be set through _config.properties. If there are multiple options to pass split them by comma. Available attributes to pass options:
```
*chrome
chrome_args=
chrome_experimental_opts=
chrome_mobile_emulation_opts=

#firefox
firefox_args=
firefox_preferences=
```
