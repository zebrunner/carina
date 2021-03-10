#Driver configuration

In Carina, WebDriver is initialized by method **getDriver()**.
It will create a WebDriver named "default" with capabilities from _config.properties.
But what if we need to launch several different browsers? Or do it with different capabilities and browser options? Carina is got a solution.

Currently supported browsers by Carina:
- Chrome
- Firefox
- Microsoft Edge
- Opera
- Safari

##Carina's webdriver initialization
WebDriver can be created in several ways:

- **getDriver(String name).**

In this case we may need to specify -Dwebdriver when launching selenium server. If there is no driver in Driver pool with name we passed, Carina will create a new WebDriver with capabilities from _config.properties file. We can access the driver by his name. By using different names we can create several Automation sessions.

- **getDriver() without arguments.**

In this case we need to specify -Dwebdriver when launching selenium server. The parameter name will get the default value "default", it means, that we can access this driver by simply calling method getDriver() or getDriver("default"). If default driver is null Carina will initialize new driver with capabilities from _config.properties file.

- **getDriver(String name, DesiredCapabilities capabilities).**

In this case we need to describe capabilities in code. You can do it by your own or use Carina's default capabilities for different browsers. Carina’s default:
```java
getDriver("chrome", new ChromeCapabilities().getCapability("Chrome Test"))
getDriver("firefox", new FirefoxCapabilities().getCapability("Firefox Test"))
getDriver("safari", new SafariCapabilities().getCapability("Safari test"))
getDriver("edge", new EdgeCapabilities().getCapability("Edge test"))
getDriver("opera", new OperaCapabilities().getCapability("Opera test"))
```
You can decide the max number of drivers by parameter max_driver_count in _config.properties. By default it's 3.
New driver could be accessed by name.
```
getDriver("safari");
```
In Carina you can also create your own capabilities and send them to the driver. For example:
``` java
DesiredCapabilities capabilities = DesiredCapabilities.safari();
capabilities.setBrowserName("safari");
capabilities.setCapability(CapabilityType.PLATFORM_NAME, SpecialKeywords.MAC);
HomePage safariHomePage = new HomePage(getDriver("safari",capabilities));
safariHomePage.open();
Assert.assertTrue(safariHomePage.isPageOpened());
```
List of all selenium [capabilities](https://github.com/SeleniumHQ/selenium/wiki/DesiredCapabilities).

There is also a possibility to send capabilities to Carina through _config.properties. All parameters with prefix capabilities. will be added to the driver’s capabilities.
```
capabilities.platformName=MAC
```
- **getDriver(String name, DesiredCapabilities capabilities, String seleniumHost).**

You can select the selenium server host. By default Carina will use your _config.properties selenium_host parameter.

##Options 

Options are used to tune your browser for tests.
``` java
DesiredCapabilities capabilities = DesiredCapabilities.safari();
capabilities.setBrowserName("safari");
capabilities.setCapability(CapabilityType.PLATFORM_NAME, SpecialKeywords.MAC);
ChromeOptions options = new ChromeOptions();
options.addArguments("--no-first-run");
options.addArguments("--disable-notifications");
capabilities.setCapability(ChromeOptions.CAPABILITY,options);
HomePage safariHomePage = new HomePage(getDriver("safari",capabilities));
safariHomePage.open();
Assert.assertTrue(safariHomePage.isPageOpened());
```
Options also could be setted through _config.properties. If there are multiple options to pass, split them by comma. Available options for now:
```
chrome_args=
chrome_experimental_opts=
chrome_mobile_emulation_opts=
firefox_args=
firefox_preferences=
```