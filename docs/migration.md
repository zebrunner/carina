### Migration steps from 5.3.3.xx to 6.0.x carina

Due to the architectural changes, code cleanup, etc., 6.0 carina-core generation is not compatible with 5.3 anymore.

* Visit [Release Notes](http://qaprosoft.github.io/carina/releases/) for details.

1) Listener Updates

   * AnnotationTransformer is a part of CarinaListener, so it should be removed from all TestNG suites and pom.xml files:
```
    // remove below lines from each TestNG suites and pom.xml files
    <listeners>
        <listener
                class-name="com.qaprosoft.carina.core.foundation.retry.AnnotationTransformer"/>
    </listeners>
```

   * HealthCheckListener is a part of CarinaListener, so it should be removed from TestNG suites and pom.xml files:
```
    // remove below lines from each TestNG suites and pom.xml files
    <listeners>
        <listener class-name="com.qaprosoft.carina.core.foundation.listeners.HealthCheckListener"/>
    </listeners>
```

2) Apache Maven plugin upgrade

   * all plugins have been upgraded on a core level, and we recommend to bump up the below mentioned plugins to the newest versions on project levels as well:
      * maven-compiler-plugin 3.2 -> 3.8.0
      * maven-surefire-plugin 2.18.1 -> 2.22.1

3) DriverPool has been refactored and is delivered as IDriverPool interface with default functionality

   * No changes on a test class levels if they extend AbstractTest

   * For every service layer which used static DriverPool functionality we should:
```
   // add "implements IDriverPool" for each service class
   public class TestService implements IDriverPool {
   ...
   //WebDriver drv = DriverPool.getDriver();
   // change to 
   WebDriver drv = getDriver();
```

4) Cucumber functionality has been completely removed in 6.0, so just remove all dependent classes if any. Inform [carina-support](mailto:carina-support@qaprosoft.com) if you want to restore cucumber module in Carina.

5) All TestRail updaters, interfaces, etc. have been removed. Carina can't push data to 3rd party Reporting Tools at runtime. 
   Carina can register only TestRail related tags in Zafira. Integration has been moved to the qps-pipeline level which is much easier to develop, maintain and support.

6) DriverHelper (AbstractPage) - removal of deprecated methods

   * click, type, hover, select, etc.
```
   ExtendedWebElement element;
   String value = "value";
   type(element, value); -> element.type(value);

   click(element); -> element.click();

   hover(element); -> element.hover();

   isElementPresent(element); -> element.isPresent(); or element.isElementPresent();

   format(element, "dynamic attr"); -> element.format("dynamic attr");
```
   Note: all the methods using a new OOP format have been replaced

   * pressEnter() has been removed
```
   pressEnter(fileNameLabel);
   // change to
   fileNameLabel.sendKeys(Keys.RETURN);
```

   * waitForElementToBeClickable(...) has been removed
```
   waitForElementToBeClickable(element, LONG_TIMEOUT);
   // change to
   element.isClickable(LONG_TIMEOUT);
```

7) CustomTypePageFactory->initPage method has an obligatory driver as the 1st argument. 

   * transferring the driver as an argument should resolve invalid calls on service layer when we received "driver already quit..." too often
```
   return CustomTypePageFactory.initPage(PlayerPageBase.class);
   // change to
   return CustomTypePageFactory.initPage(getDriver(), PlayerPageBase.class);
```

8) Communicate via [carina-support](mailto:carina-support@qaprosoft.com) to get migration support for free...
