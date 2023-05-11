Migration guide from [7.4.31](https://github.com/zebrunner/carina/releases/tag/7.4.31) to [1.0.0](https://github.com/zebrunner/carina/releases/tag/1.0.0).

### Selenium/Appium versions updated

The Selenium version has been updated from [3.141.59](https://github.com/SeleniumHQ/selenium/releases/tag/selenium-3.141.59) to [4.4.0](https://github.com/SeleniumHQ/selenium/releases/tag/selenium-4.4.0). 
Check out the [official migration guide](https://www.selenium.dev/documentation/webdriver/getting_started/upgrade_to_selenium_4/).

The Appium version has been updated from [7.6.0](https://github.com/appium/java-client/releases/tag/7.6.0) to [8.2.0](https://github.com/appium/java-client/releases/tag/v8.2.0). 
Check out the [official migration guide](https://github.com/appium/java-client/blob/master/docs/v7-to-v8-migration-guide.md).

### Renamed packages

```
com.qaprosoft.carina.core.foundation.utils.ownership.* -> com.zebrunner.carina.core.registrar.ownership.*
com.qaprosoft.carina.core.foundation.utils.tag.* -> com.zebrunner.carina.core.registrar.tag.*
com.qaprosoft.carina.core.foundation.utils.Messager -> com.zebrunner.carina.utils.messager.Messager
com.qaprosoft.carina.core.foundation.utils.ZebrunnerNameResolver -> com.zebrunner.carina.core.testng.ZebrunnerNameResolver
com.qaprosoft.carina.core.foundation.utils.ParameterGenerator -> com.zebrunner.carina.utils.ParameterGenerator
com.qaprosoft.carina.core.foundation.utils.* -> com.zebrunner.carina.utils.*
com.qaprosoft.carina.core.foundation.report.* -> com.zebrunner.carina.utils.report.*
com.qaprosoft.carina.core.foundation.retry.* -> com.zebrunner.carina.utils.retry.*
com.qaprosoft.carina.core.foundation.commons.SpecialKeywords -> com.zebrunner.carina.utils.commons.SpecialKeywords
com.qaprosoft.carina.core.foundation.listeners.TestnamingService -> com.zebrunner.carina.core.testng.TestNamingService
com.qaprosoft.carina.core.foundation.exception.* -> com.zebrunner.carina.utils.exception.*
com.qaprosoft.carina.core.foundation.performance.* -> com.zebrunner.carina.utils.performance.*
com.qaprosoft.carina.browserupproxy.* -> com.zebrunner.carina.proxy.browserup.*
com.qaprosoft.carina.proxy.SystemProxy -> com.zebrunner.carina.proxy.SystemProxy
com.qaprosoft.amazon.* -> com.zebrunner.carina.amazon.*
com.qaprosoft.azure.* -> com.zebrunner.carina.azure.*
com.qaprosoft.appcenter.* -> com.zebrunner.carina.appcenter.*
com.qaprosoft.apitools.* -> com.zebrunner.carina.api.apitools.*
com.qaprosoft.carina.core.foundation.api.* -> com.zebrunner.carina.api.*
com.qaprosoft.carina.core.foundation.webdriver.* -> com.zebrunner.carina.webdriver.*
com.qaprosoft.carina.core.gui.* -> com.zebrunner.carina.webdriver.gui.*
com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSCache -> com.zebrunner.carina.utils.parser.xls.XLSCache
com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSChildTable -> com.zebrunner.carina.utils.parser.xls.XLSChildTable
com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser -> com.zebrunner.carina.utils.parser.xls.XLSParser
com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSTable -> com.zebrunner.carina.utils.parser.xls.XLSTable
com.qaprosoft.carina.core.foundation.dataprovider.* -> com.zebrunner.carina.dataprovider.*
com.qaprosoft.carina.core.foundation.* -> com.zebrunner.carina.core.*
```

### Separated optional dependencies

These dependencies can be added in addition to the carina-core dependency.

* [carina-dataprovider](https://github.com/zebrunner/carina-dataprovider/releases) see [(maven-central)](https://mvnrepository.com/artifact/com.zebrunner/carina-dataprovider) - to use csv/xls data providers. 
Test classes that use/will use such dataproviders should implement `com.zebrunner.carina.dataprovider.IAbstractDataProvider` interface. 
For more info check [documentation](https://zebrunner.github.io/carina/advanced/dataprovider/). 
Also when adding carina-dataprovider dependency exclude testng dependency as the only source of testng should be carina-core.
* [carina-api](https://github.com/zebrunner/carina-api/releases) see [(maven-central)](https://mvnrepository.com/artifact/com.zebrunner/carina-api) - for API testing.
* [carina-appcenter](https://github.com/zebrunner/carina-appcenter/releases) see [(maven-central)](https://mvnrepository.com/artifact/com.zebrunner/carina-appcenter) - if the application is stored using AppCenter.
* [carina-azure](https://github.com/zebrunner/carina-azure/releases) see [(maven-central)](https://mvnrepository.com/artifact/com.zebrunner/carina-azure) - if the application/files are stored using Azure.
* [carina-aws-s3](https://github.com/zebrunner/carina-aws-s3/releases) see [(maven-central)](https://mvnrepository.com/artifact/com.zebrunner/carina-aws-s3) - if the application/files are stored using Amazon S3.

### Redesigned the encryption process

* The support of crypto key as a file, as well as the actual `crypto_key_path` parameter, have been removed.
* The `crypto_key_value` parameter is added to specify the crypto key (specified in [configuration](https://zebrunner.github.io/carina/configuration/)).
* The presence of a crypto key is optional, as long as there is no encrypted data.

Since a file was used as a key before, you can use the following code to get the key from it and writing it to the 
`crypto_key_value` parameter (for example, in beforeSuite):

```
R.CONFIG.put("crypto_key_value", Files.readString(Path.of(R.CONFIG.get("crypto_key_path")), StandardCharsets.UTF_8));
```
However, this approach is not recommended. It is recommended to put `crypto_key_value` in environment variables.

For more info check [documentation](https://zebrunner.github.io/carina/advanced/security/).

### Changed the process of creating capabilities for a session

In connection with the update of Selenium / Appium, the rules for specifying capabilities have changed. 
Check the [documentation](https://zebrunner.github.io/carina/advanced/driver/#capabilities).

### Screenshots

Screenshot rules redesigned, check the [documentation](https://zebrunner.github.io/carina/advanced/screenshot/).

### Driver event listeners

In connection with the update of Selenium, the use of a `org.openqa.selenium.support.events.WebDriverEventListener` event listener has been removed 
and accordingly removed the wrapper based on the `org.openqa.selenium.support.events.EventFiringWebDriver` class.
Instead, use an event listener based on the `org.openqa.selenium.support.events.WebDriverListener` interface 
(as an example, take a look at Carina Framework [DriverListener](https://github.com/zebrunner/carina-webdriver/blob/master/src/main/java/com/zebrunner/carina/webdriver/listener/DriverListener.java) event listener).


Important: it is strongly not recommended to use methods of drivers by casting to the driver implementations, drivers implement interfaces with appropriate methods,
up to which you need to cast the driver to gain access to the method. For example, if you want to call the method
`executeScript` of the `org.openqa.selenium.remote.RemoteWebDriver` class, you can execute the following code:

```
((JavascriptExecutor)getDriver()).executeScript(...);
```

However, there are methods that cannot be accessed using the interface. In such situation you can use the `castDriver` method of the 
`com.zebrunner.carina.webdriver.listener.DriverListener` class. However, if you are casting to AndroidDriver for example, you must
be sure it's AndroidDriver.

### Project side dependency requirements

* The `net.bytebuddy.byte-buddy` version should be `1.12.10`.
* The `com.zebrunner.agent-core` version should be `1.9.3`.
* When specifying carina-... dependencies, it is desirable that carina-core be the first among them.
* The explicit com.google.guava.guava dependency must be removed from the project, and guava must be pulled from 
carina-webdriver (you can check it using `mvn dependency:tree`) and should match version `31.1-jre`.

### Proxy

Changed [BrowserMob](https://github.com/lightbody/browsermob-proxy) proxy to [BrowserUp proxy](https://github.com/browserup/browserup-proxy), added different proxy modes from Selenium. 
For more info check the [documentation](https://zebrunner.github.io/carina/advanced/proxy/).

### Element search process

* Added support for annotations `FindBys`, `FindAll`, `AndroidFindBy` and so on. However, using features such as CaseInsensitiveXpath annotation, 
L10N in locator, format method and so on is currently not supported with FindBys, FindAny, FindAll and Appium ..Bys, 
..All, ...AllSet, ..ChainSet annotations.
* The `format` method of the `ExtendedWebElement` class now works with localization and the CaseInsensitiveXpath annotation.
