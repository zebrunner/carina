Migration guide from [1.1.5](https://github.com/zebrunner/carina/releases/tag/1.1.5) to [1.2.6](https://github.com/zebrunner/carina/releases/tag/1.2.6).

### Configuration

Removed `src/main/java/com/zebrunner/carina/utils/Configuration.java` class. Configuration parameters managed by [com.zebrunner.carina.utils.config.Configuration](https://github.com/zebrunner/carina-utils/blob/develop/src/main/java/com/zebrunner/carina/utils/config/Configuration.java) class and it's inheritors ([WebDriverConfiguration](https://github.com/zebrunner/carina-webdriver/blob/master/src/main/java/com/zebrunner/carina/webdriver/config/WebDriverConfiguration.java) and so on).

This class provides a `get` method. This method provides the following features:

1. Get `Optional` value. If you think that parameter should exists, use [getRequired](https://github.com/zebrunner/carina-utils/blob/f05791c528af6f317fd7a7889ec2359ffc60c1fc/src/main/java/com/zebrunner/carina/utils/config/Configuration.java#L156) method instead (throws exception if there are no value).
2. Cast to the specified type. Supported types: `String`, `Integer`, `Long`, `Double`, `Boolean`, `Short`, `Byte`.
3. By default, this method tries to find environment (`<env>.<parameter_name>`) value. If there are no value, it tries to find global (`<parameter_name>`) value. If you want to search only `global` or `environment` value, specify [StandardConfigurationOption.ENVIRONMENT](https://github.com/zebrunner/carina-utils/blob/b27b4f3921bcb67e1cb5f4a60b5d394af844f649/src/main/java/com/zebrunner/carina/utils/config/StandardConfigurationOption.java#L7) or [StandardConfigurationOption.GLOBAL](https://github.com/zebrunner/carina-utils/blob/b27b4f3921bcb67e1cb5f4a60b5d394af844f649/src/main/java/com/zebrunner/carina/utils/config/StandardConfigurationOption.java#L8C10-L8C10) parameter.
4. If you want to decrypt value, specify [StandardConfigurationOption.DECRYPT](https://github.com/zebrunner/carina-utils/blob/b27b4f3921bcb67e1cb5f4a60b5d394af844f649/src/main/java/com/zebrunner/carina/utils/config/StandardConfigurationOption.java#L9C5-L9C5) parameter.

You can also organize project-specific configuration class. Just inherit from [Configuration](https://github.com/zebrunner/carina-utils/blob/develop/src/main/java/com/zebrunner/carina/utils/config/Configuration.java) class, implement your parameters and override `toString` method. Any existing configuration class is suitable as a sample.

### Interaction with the test folders

[ReportContext](https://github.com/zebrunner/carina-utils/blob/master/src/main/java/com/zebrunner/carina/utils/report/ReportContext.java) class refactored for using `java nio`. It provides more user-friendly interface.

Example (get path to the screenshot file from test directory):

```java
Path path = ReportContext.getTestDirectory()
        .resolve("screenshot.png");
```

### Inherit ExtendedWebElement

Added opportunity to inherit ExtendedWebElement and override it's logic. Inheritor should contains `public <className>(WebDriver driver, SearchContext searchContext)` constructor.

All default methods contains in `src/main/java/com/zebrunner/carina/webdriver/helper/IExtendedWebElementHelper.java` interface (format, presence of the element and so on). Use methods provided by this interface if you want for example `format` your custom `ExtendedWebElement`.

### Project-side dependencies

```
com.zebrunner.carina-core           1.2.6
com.zebrunner.carina-dataprovider   1.2.4
com.zebrunner.carina-api            1.2.4
com.zebrunner.carina-aws-s3         1.2.5
com.zebrunner.carina-azure          1.2.5
com.zebrunner.carina-appcenter      1.2.6
net.bytebuddy.byte-buddy            1.14.5
```

### Other

Removed `MongoConnector` class and `mongo-java-driver` dependency.

Use `com.zebrunner.carina.utils.encryptor.EncryptorUtils` class if you want to encrypt/decrypt data.

`Screenshot` -> `capture` methods return `Optional<Path>` (path to the screenshot) instead of `Optional<String>` (name of the screenshot).
Also removed unnecessary screenshot rule methods.

Removed all non-w3c capabilities. Non-w3c capabilities will fail session startup.

Removed `email.properties` support.
