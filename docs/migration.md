### Migration steps from 5.3.3.xx to 6.0.x carina

Due to the architectual changes, code cleanup etc 6.0 carina-core generation is not compatible with 5.3 anymore.

* Visit [Release Notes](http://qaprosoft.github.io/carina/releases/) for details.

1) Listeners Updates

   * AnnotationTransformer is part of CarinaListener so it should be removed from all TestNG suites and pom.xml files:
```
    // remove below lines from each TestNG suites and pom.xml files
    <listeners>
        <listener
                class-name="com.qaprosoft.carina.core.foundation.retry.AnnotationTransformer"/>
    </listeners>
```

   * HealthCheckListener is part of carinaListener so it should be removed from TestNG suites and pom.xml files:
```
    // remove below lines from each TestNG suites and pom.xml files
    <listeners>
        <listener class-name="com.qaprosoft.carina.core.foundation.listeners.HealthCheckListener"/>
    </listeners>
```

2) Apache Maven plugins upgrade

   * all plugins where upgraded on core level and we recommend to bump up below plugins to the newest versions on project leveles as well:
      * maven-compiler-plugin 3.2 -> 3.8.0
      * maven-surefire-plugin 2.18.1 -> 2.22.1

3) DriverPool was refactored and is delivered as IDriverPool intefrace with default functionality

   * No changes on test classes level if they extends AbstractTest

   * For each services layer which used static DriverPool functionality we should:
```
   // add "implements IDriverPool" for each service class
   public class TestService implements IDriverPool {
   ...
   //WebDriver drv = DriverPool.getDriver();
   // change to 
   WebDriver drv = getDriver();
```

4) Cucumber functionality was completely removed in 6.0 so just remove all dependent classes if any. Inform [carina-support](mailto:carina-support@qaprosoft.com) if you want to restore cucumber module in Carina.

5) All TestRail updaters, interfaces etc were removed. Carina can't push at runtime data to 3rd party Reporting Tools. 
   Carina can register only TestRail related tags in Zafira. Integration was moved to the qps-pipeline level with much more easier to develop, maintain and support

6) DriverHelper (AbstractPage) - deprecated methods removal

   * click, type, hoved, select etc
```
   ExtendedWebElement element;
   String value = "value";
   type(element, value); -> element.type(value);

   click(element); -> element.click();

   hover(element); -> element.hover();

   isElementPresent(element); -> element.isPresent(); or element.isElementPresent();

   format(element, "dynamic attr"); -> element.format("dynamic attr");
```
   Note: replaced all methods using new OOP format

   * pressEnter() was removed
```
   pressEnter(fileNameLabel);
   // change to
   fileNameLabel.sendKeys(Keys.RETURN);
```

   * waitForElementToBeClickable(...) was removed
```
   waitForElementToBeClickable(element, LONG_TIMEOUT);
   // change to
   element.isClickable(LONG_TIMEOUT);
```

7) CustomTypePageFactory->initPage method has obligatory driver as 1st argument. 

   * transferring driver as argument should resolve invalid calls on services layer when we got too often "driver already quit..." 
```
   return CustomTypePageFactory.initPage(PlayerPageBase.class);
   // change to
   return CustomTypePageFactory.initPage(getDriver(), PlayerPageBase.class);
```

8) Communicate via [carina-support](mailto:carina-support@qaprosoft.com) to get migration support for free...