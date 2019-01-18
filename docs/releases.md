# Release notes

### 6.0.5 (2019-01-04)

**Enhancements**

* [#565](https://github.com/qaprosoft/carina/issues/565) carina-api: api.validateResponseAgainstJSONSchema should support schema draft 7
* Set of improvements into the IDriverPool to make its modification threads safety
* Start global migration of MobileUtils to IMobileUtils interface 
* Register device information in global driversPool for each driver session
* Moved drivers quit and devices disconnect onto the java shutdown hook to kill sessions even for aborted tests/jobs
* Saved information about integrated with STF device in Device object with automatic disconnect on finish
* Do not show encrypted text in logs (aka user credentials etc)

**Fixes**

* [#590](https://github.com/qaprosoft/carina/issues/594) org.openqa.selenium.remote.RemoteWebDriver cannot be cast to io.appium.java_client.MobileDriver
* [#590](https://github.com/qaprosoft/carina/issues/590) Device type is defined incorrectly in several cases for iOS/Android tests
* [#588](https://github.com/qaprosoft/carina/issues/588) we still can generate ConcurrentModificationException during driver quit operation
* [#574](https://github.com/qaprosoft/carina/issues/574) annoying INFO message in log for specific use-cases
* [#459](https://github.com/qaprosoft/carina/issues/459) Enormous amount of logs appears in console on attempt to use element.clickIfPresent(3)
* Disabled screenshots capturing for three more driver failures: 
   * was terminated due to CLIENT_STOPPED_SESSION
   * Session ID is null. Using WebDriver after calling quit()
   * was terminated due to BROWSER_TIMEOUT

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* N/A

### 6.0.4 (2018-12-27)

**Fixes**

* [#553](https://github.com/qaprosoft/carina/issues/553) 6.0: Rebuild failures doesn't work
* [#582](https://github.com/qaprosoft/carina/issues/582) Hardcoded parameters in carina archetype (-Dname and -Durl)

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* zafira-client 3.3.47->3.3.49


### 6.0.3 (2018-12-21)

**Enhancements**

* [#570](https://github.com/qaprosoft/carina/issues/570) Updated iDriverPool returning back automatic driver quit onto AfterMethod phase as it was in 5.3.x core. New approach was rejected due to the discovered regressions 
* [#567](https://github.com/qaprosoft/carina/issues/567) Added new browsermob_host parameter to override automatically detected IP address for embedded proxy. Updated manual accordingly http://qaprosoft.github.io/carina/proxy
* [#568](https://github.com/qaprosoft/carina/issues/568) Returned Chrome browser maximize to the default method removing old workaround with hardcoded dimension
* [576](https://github.com/qaprosoft/carina/issues/576) Removed sensitive information from logs during onType and onAttachFile operations
* [#574](https://github.com/qaprosoft/carina/issues/574) Removed invalid log4j appender and hide noisy log messages on startup
* Adjust custom_capabilities usage allowing not only define it globally  but generate for each driver session
```
  // start new driver with generated capabilities based on capabilities inside 
  // Samsung_Galaxy_S8.properties property file
   WebDriver drv = getDriver("name", new CapabilitiesLoader().getCapabilities("browserstack/android/Samsung_Galaxy_S8.properties"))
```
* Removed obsolete extra_capabilities configuration parameter 
* Declared [Spring Plugins](https://mvnrepository.com/repos/springio-plugins-release) repository in pom.xml to download from scratch monte-repack (javax.media.jai) dependency

**Known Issues**

* [#553](https://github.com/qaprosoft/carina/issues/553) In the integration with Zafira environment rerun failures is still broke 

**[DEPENDENCIES UPDATES]**

* N/A

### 6.0.2 (2018-12-04)

**Enhancements**

* Starting from 6.0.x carina-core became a listener with unified CarinaListener class:
    * most of the AbstractTest core methods moved to CarinaListener
    * incorporated HealthCheckListener as part of CarinaListener
    * splited initialization logic into static block and ISuiteListener->onStart(suite) method
    * incorporated DriverPool cleanup on shutdown hook
    * **Note:** some changes are incompatible in comparison with 5.x.x versions and need manual updates according to the **Migration Steps** 
* Migrated to the latest 6.14.3 TestNG version with fully supported retry execution feature
* Updated build numbering structure according all qaprosoft projects:
    * **6**.0.0 - core generation
    * 6.**0**.0 - service pack release
    * 6.0.**0** - cross release build number which starts from 0 in 6th generation (in 5.x.x.x release latest build number is 5.3.3.**129**)
* Finalized integration rules for Zafira integration:
    * Add ZafiraListener in global pom.xml to inject it for all CI runs
    * Add ZafiraListener into each TestNG suite to be able to run locally with Zafira integration
    * Note: carina archetype updated accordingly
* Refactored DriverPool class and deliver it as functional IDriverPool interface
    * It allowed to remove driver_mode property and calculate driver lifecycle automatically based on rules below:
         * All drivers started during @BeforeSuite phase are saved across all suite run
         * All drivers started during @BeforeClass phase are saved across current test class run
         * All drivers started during @BeforeMethod phase or inside method are saved only for current method and closed automatically
         * To be able to keep "method mode" drivers just use dependsOnMethods property in @Test annotation.
```
Example:
    @Test()
    public void test1() {
        getDriver(); //start new driver
        ...
    }
    @Test(dependsOnMethods="test1")
    public void test2() {
        getDriver(); //get existing driver started in test1 as test2 depends on test1
        ...
    }
    @Test()
    public void test3() {
        getDriver(); //start new driver as no dependencies detected
        ...
    }
```
* Moved unit tests into the valid modules to be able to track coverage results in Sonar: https://ci.qaprosoft.com/sonarqube/dashboard/index?did=2
* Move some noisy log messages onto the DEBUG level
* added possibility to redefine log level for explicit sub-modules/classes
```
core_log_level=DEBUG
core_log_packages=IDriverPool,ZafiraConfigurator
```
* added support for Opera browser. For now both variants are ok:
    * browser=opera
    * browser=operablink 
* Removed all TestRail updaters from Carina and all required dto classes. Integration with 3rd party testcase management tools will be allowed through the https://github.com/qaprosoft/zafira only!
* Removed SMTP integration settings from Carina. All notification methods will be concentrated in Zafira Reporting Tool. Carina could only run and generate local report in ./reports/qa folder.
* Removed completely JUnit libraries from classpath to avoid invalid asserts import inside the test classes on projects level
* Removed cucumber integration as not used feature
* Removed all deprecated methods in DriverHelper
* Removed deprecated constructors in ExtendedWebElement class

**Fixes**

* Improved driver restart logic fixing [#364](https://github.com/qaprosoft/carina/issues/364) and [#552](https://github.com/qaprosoft/carina/issues/552).
* removed soapui.log4j.config property 
* reused new getConstructorOrMethod() function for getting methods instances
* [#558](https://github.com/qaprosoft/carina/issues/558), [#546](https://github.com/qaprosoft/carina/issues/546), [#542](https://github.com/qaprosoft/carina/issues/542), [#532](https://github.com/qaprosoft/carina/issues/532), [#519](https://github.com/qaprosoft/carina/issues/519), [#512](https://github.com/qaprosoft/carina/issues/512), [#505](https://github.com/qaprosoft/carina/issues/505), [#484](https://github.com/qaprosoft/carina/issues/484), [#483](https://github.com/qaprosoft/carina/issues/483), [#480](https://github.com/qaprosoft/carina/issues/480), [#479](https://github.com/qaprosoft/carina/issues/479), [#375](https://github.com/qaprosoft/carina/issues/375), [#343](https://github.com/qaprosoft/carina/issues/343)

**[DEPENDENCIES UPDATES]**

* TestNG 6.8.8->6.14.3
* zafira-client 3.3.37->3.3.68-SNAPSHOT (due to the changed TestNG)
* selenium-server 3.14.0->3.141.59
* selenium-java 3.14.0->3.141.59
* jackson-databind.version 2.8.9->2.8.11.1
* org.apache.pdfbox 1.8.7->1.8.16
* org.testng 6.8.8->6.14.3


**Migration Steps**

* Visit [Migration Steps](http://qaprosoft.github.io/carina/migration/) for details.

### 5.3.3.129 (2018-11-21)

**Enhancements**

* Workaround Appium issue and enabled iOS Apps video recording
* Introduced new TestRail and QTest cases annotations
* Enabled secure (https) traffic sniffering via embedded proxy by default
* Removed dependencies conflicts to make browsermobproxy workable for secure content 
* Published documentation about [proxy usage](http://qaprosoft.github.io/carina/proxy/)

**Fixes**

* Removed workaround for https://github.com/SeleniumHQ/selenium/issues/5299 as not required anymore. 
   * Note: tested on Chrome 69 and selenium-standalone 3.11.0-3.141.5
* [#525](https://github.com/qaprosoft/carina/issues/525) Incorrect video count in test info

**[DEPENDENCIES UPDATES]**

* exclude out-of-date 1.38 org.bouncycastle dependency



### 5.3.2.127 (2018-11-07)

**Enhancements**

* Added possibility to start localized Chrome and Firefox browsers using "browser_locale" property
* Updated carina archetype content
* Updated [snapshots qaprosoft](ttps://ci.qaprosoft.com/nexus/content/repositories) repositories to use https protocol.
* Switched to latest ZafiraClient (3.3.47) with improvements to the AWS S3 screenshots uploading. We can provide expiresIn in seconds for each uploaded image

**Fixes**

* Only "priority" tag name keep in reserved system names pool

**[DEPENDENCIES UPDATES]**

* com.qaprosoft.zafira-client updated to 3.3.47


### 5.3.1.125 (2018-10-16)

**Enhancements**

* Incorporated Carina pipeline build process into the common qps-pipeline library with such possibilities opened for everyone:
  * Automatic snapshot build deployment based on PullRequest sources when "build-snapshot" label is assigned to the PR or "build-snapshot" is present in PR title
  * Configured automatic PR checker static code analysis using Sonar PR checker
  * Configured full static code analysis and snapshot build generation after merge to master
* @TestTag and @TestPriority annotations developed to be able to assign P0-P6 priorities to any test method and any custom tag like "feature" etc
* Switched to latest ZafiraClient (3.3.46) with custom tags registration funcitonality

**Fixes**

N/A

**[DEPENDENCIES UPDATES]**

* maven-surefire-plugin 2.12.4->2.22.1
* maven-compiler-plugin 3.1->3.8.0
* maven-javadoc-plugin 2.3 -> 3.0.1
* maven-assembly-plugin 2.4.1 -> 3.1.0
* maven-source-plugin 2.4 -> 3.0.1
* com.qaprosoft.zafira-client updated to 3.3.46



### 5.3.0.124 (2018-10-08)

**Enhancements**

* Refactored List<ExtendedWebElement> and AbstractUI objects to speedup objects manipulations
* Switched to latest ZafiraClient (3.3.44) with screenshots publishing into AWS S3
* Implemented async screenshot images publishing to AWS S3
* Added optimize_video_recording parameter. If true video will be captured only for the final retry and as result failed only.
* Handled application crash on iOS with proper exception as only it happens
* Improved logging messages and format to sync with new gallery and log viewer
* Removed obsolete Parameter(s)
  * ci_url
  * ci_build
  * keep_all_screenshots
* Removed obsolete com.qaprosoft.carina.core.foundation.webdriver.appium package
* Removed obsolete start/stop recording methods for Device
* Pushed to log WebDriver url when any exception is captured
* Be able to crypto aws access keys: http://qaprosoft.github.io/carina/security/
  
**Fixes**

* Found a root cause of the TestNG main thread crashes and delivered fixes (children DriverListener threads can't raise unchecked RuntimeException)
* Fixed huge regression defect with STF integration (added explicit adb disconnect to correctly stop device usage)
* Added explcit io.netty for carina-proxy module
* Fixed most of the serious Sonar complains
* Handled StaleElementReferenceException which appeared in 1.9.0 Appium
* Fix @CaseInsensitiveXPath annotation usage for more complicated xpath values

**[DEPENDENCIES UPDATES]**

* com.qaprosoft.zafira-client updated to 3.3.44



### 5.2.5.119 (2018-08-23)

**Enhancements**

* Disabled live screenshots appender into the Zafira Reporting Tool due to the performance degradation of elasticsearch
* Improved custom capabilities transfer between carina and custom mobile selenium-hub
* Added documentationabout CustomTypePageFactory usage to generate unified Desktop/Android/iOS test classes: http://qaprosoft.github.io/carina/automation/mobile/
* Added live VNC streaming support for drivers started in before suite/class/method actions
* Added new public static method DevicePool.isRegistered()
* Updated carina default archetype

**Fixes**

* Updated copyright info
* Workaround applied for appium issue: https://github.com/appium/appium/issues/10159
* Hide some stacktrace messages during browser maximize as warning (mostly for the executing web tests on mobile devices/browsers)
* Cleanup in DesktopFactory removing obsolete functionality

**[DEPENDENCIES UPDATES]**

* com.qaprosoft.zafira-client updated to 3.0.43


### 5.2.4.111 (2018-07-24)

**Enhancements**

* Published new article for API and Mobile automation approaches:
http://qaprosoft.github.io/carina/automation/api/
http://qaprosoft.github.io/carina/automation/mobile/

* Improved actual browser_version identification and it's registration in Zafira Reporting Tool

* Introduced new DisableCacheLookup annotation to ExtendedWebElement to disable automatic caching and use object as locator proxy only

* Introduced @CaseInsensitiveXPath annotation (mostly for Android where different frameworks can recognize locator in different way)

* Split click operation for separated Web and Mobile actions to minimize negative side-effects

* MobileUtils - migrated tap etc operations onto the TouchOptions etc
Note: if in your code thera direct references onto the TouchActions->tap operations you should update them as well because in 6.0.0 java appium client deprecated methods were removed.
Example: https://github.com/qaprosoft/carina/commit/859d1f0d284462733e2c2ddf005bad3f48b41711

* Introduced AndroidUtils.pressKeyboardKey(...) method

* Enabled logcat extractor for Android devices. Copying system device logs for each tests to capture crashes much easier

* Finalized with UI Dump generation utility (generating screenshot and metadata information on Android devices to be able to analyze xpath values in offline mode using uiautomator viewer)

* Improved integration with qps-pipeline to support demo screenshots publishing to CI for aborted testruns as well

**Fixes**
* minor adjustments in UI operations for invisible elements on browsers
* fixed integration with ZafiraClient to specify default expiration for test artifacts (30 days)
* Resolve issues with default Timer operations (error messages instead of RuntimeException)
* Handled annoying cast exception "java.lang.ClassCastException: com.google.common.collect.Maps$TransformedEntriesMap cannot be cast to java.lang.String" which is produced mistakenly by Appium instead of StaleElementException 
* Fixed potential recursive callss in DriverListener->onException method
* Fixed vnc pattern usage for live video streaming (Web and Android mobile)
* Fixed UIX schema files generation for Android mobile pages
* Fixed IE/Edge capabilities generation for use-case with BrowserStack
* Added thread name and id into the thread log appender messaging

**[DEPENDENCIES UPDATES]**

* io.appium:java-client was updated to official 6.1.0 release
* org.seleniumhq.selenium:selenium-java updated to 3.12.0
* com.qaprosoft.zafira-client updated to 3.0.40



### 5.2.4.97 (2018-06-02)

**Enhancements**

* Published new article for API automation approach:
http://qaprosoft.github.io/carina/automation/api/

* MobileUtils - migrated tap etc operations onto the TouchOptions etc
Note: if in your code thera direct references onto the TouchActions->tap operations you should update them as well because in 6.0.0 java appium client deprecated methods were removed.
Example: 859d1f0

* Split click operation for separated Web and Mobile actions to minimize negative side-effects

**Fixes**

* minor adjustments in UI operations for invisible elements on browsers

**[DEPENDENCIES UPDATES]**

* io.appium:java-client was updated to official 6.0.0 release
* org.seleniumhq.selenium:selenium-java updated to 3.12.0
