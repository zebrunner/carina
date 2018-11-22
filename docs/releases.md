Release notes

# (Pre-release) 6.0.188-SNAPSHOT
==================

## Enhancements

## 6.0-beta12
* finalized integration rules with Zafira (we support declaration of ZafiraListener in global pom.xml and inside each testng suite xml)
* adjusted default carina archetype based on new Zafira integration rules
* moved unit tests into the valid modules to be able to track coverage results in Sonar: https://ci.qaprosoft.com/sonarqube/overview?id=2
* hide several methods inside IDriverPool implementation

## 6.0-beta11
* refactored onStart/onFinish actions using ISuiteListener and static initialization block
* Simplified integration with Zafira and successfully resolved registration issues in multi-threading mode
* temporary enabled debug logging for ZafiraConfigurator component only
## [DEPENDENCIES UPDATES]
zafira-client 3.3.37->3.3.68-SNAPSHOT (based on TestNG 6.14.3)

## 6.0-beta10
* Removed Empty/Default/Custom testRail updater from Carina and all required dto classes. Integration with 3rd party testcase management tools will be allowed through the https://github.com/qaprosoft/zafira only!
* Ported changes from 5.3.3 (mostly browsermobproxy fixes)

## 6.0-beta9
* Wrapped onTestStart(previously @BeforeSuite) into synchronized action.
* Allowed single onTestStart execution only

## 6.0-beta8
* ported iOS video recording fixes from 5.3.x

## 6.0-beta7
* added ability to start localized browsers (Chrome and Firefox)
* keep static IDriverPool.getDefaultDriver() to keep all utils services classes.
* fixed browsermob proxy issues

## 6.0-beta6
* added support for Opera browser. For now both variants are ok:
    * browser=opera
    * browser=operablink 

## 6.0-beta5
* removed driver_mode property. To keep drivers between methods you should use dependsOnMethods property in @Test annotation.
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

## 6.0-beta4
* return to default build numbering structure we use in all qaprosoft projects:
   * <b>6</b>.0.0 - core generation
   * 6.<b>0</b>.0 - service pack release
   * 6.0.<b>0</b> - cross release build number which starts from 0 in 6th generation (in 5.x.x.x release latest build number is 5.3.1.<b>125</b> for today)
* refactored DriverPool class and make it as functional IDriverPool interface with default implementation

## 6.0-beta3
* Moved most of the AbstractTest functionality into CarinaListener class
* Incorporated all HealthCheckListener  logic into the single CarinaListener
* Finally moved to the latest TestNG version with fully supported retry execution feature
* Migrated to the latest selenium version
* Removed smtp integration settings from Carina. All notification methods will be concentrated in Zafira Reporting Tool
* Removed completely junit libraries from classpath to avoid invalid asserts import inside the test classes on projects level
* Removed cucumber integration as not used feature
* Removed all deprecated methods in DriverHelper
* Removed deprecated constructors in ExtendedWebElement class
## Fixes
* removed soapui.log4j.config property 
* reused new getConstructorOrMethod() function for getting methods instances
## [DEPENDENCIES UPDATES]
6.8.8->6.14.3
due to the security alerts bumped up:
jackson-databind.version 2.8.9->2.8.11.1
org.apache.pdfbox 1.8.7->1.8.16
org.testng 6.8.8->6.14.3
org.seleniumhq.selenium 3.12.0->3.14.0


#5.3.3.129 (2018-11-21)
==================

## Enhancements
* Workaround Appium issue and enabled iOS Apps video recording
* Introduced new TestRail and QTest cases annotations
* Enabled secure (https) traffic sniffering via embedded proxy by default
* Removed dependencies conflicts to make browsermobproxy workable for secure content 
* Published documentation about [proxy usage](http://qaprosoft.github.io/carina/proxy/)

## Fixes
* Removed workaround for https://github.com/SeleniumHQ/selenium/issues/5299 as not required anymore. 
   * Note: tested on Chrome 69 and selenium-standalone 3.11.0-3.141.5
* [#525](https://github.com/qaprosoft/carina/issues/525) Incorrect video count in test info

## [DEPENDENCIES UPDATES]
* exclude out-of-date 1.38 org.bouncycastle dependency


#5.3.2.127 (2018-11-07)
==================

## Enhancements
* Added possibility to start localized Chrome and Firefox browsers using "browser_locale" property
* Updated carina archetype content
* Updated [snapshots qaprosoft](ttps://ci.qaprosoft.com/nexus/content/repositories) repositories to use https protocol.
* Switched to latest ZafiraClient (3.3.47) with improvements to the AWS S3 screenshots uploading. We can provide expiresIn in seconds for each uploaded image

## Fixes
* Only "priority" tag name keep in reserved system names pool

## [DEPENDENCIES UPDATES]
* com.qaprosoft.zafira-client updated to 3.3.47

#5.3.1.125 (2018-10-16)
==================

## Enhancements
* Incorporated Carina pipeline build process into the common qps-pipeline library with such possibilities opened for everyone:
  * Automatic snapshot build deployment based on PullRequest sources when "build-snapshot" label is assigned to the PR or "build-snapshot" is present in PR title
  * Configured automatic PR checker static code analysis using Sonar PR checker
  * Configured full static code analysis and snapshot build generation after merge to master
* @TestTag and @TestPriority annotations developed to be able to assign P0-P6 priorities to any test method and any custom tag like "feature" etc
* Switched to latest ZafiraClient (3.3.46) with custom tags registration funcitonality

## Fixes
N/A

## [DEPENDENCIES UPDATES]
* maven-surefire-plugin 2.12.4->2.22.1
* maven-compiler-plugin 3.1->3.8.0
* maven-javadoc-plugin 2.3 -> 3.0.1
* maven-assembly-plugin 2.4.1 -> 3.1.0
* maven-source-plugin 2.4 -> 3.0.1
* com.qaprosoft.zafira-client updated to 3.3.46


#5.3.0.124 (2018-10-08)
==================

## Enhancements
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
## Fixes
* Found a root cause of the TestNG main thread crashes and delivered fixes (children DriverListener threads can't raise unchecked RuntimeException)
* Fixed huge regression defect with STF integration (added explicit adb disconnect to correctly stop device usage)
* Added explcit io.netty for carina-proxy module
* Fixed most of the serious Sonar complains
* Handled StaleElementReferenceException which appeared in 1.9.0 Appium
* Fix @CaseInsensitiveXPath annotation usage for more complicated xpath values
## [DEPENDENCIES UPDATES]
* com.qaprosoft.zafira-client updated to 3.3.44


#5.2.5.119 (2018-08-23)
==================

## Enhancements
* Disabled live screenshots appender into the Zafira Reporting Tool due to the performance degradation of elasticsearch
* Improved custom capabilities transfer between carina and custom mobile selenium-hub
* Added documentationabout CustomTypePageFactory usage to generate unified Desktop/Android/iOS test classes: http://qaprosoft.github.io/carina/automation/mobile/
* Added live VNC streaming support for drivers started in before suite/class/method actions
* Added new public static method DevicePool.isRegistered()
* Updated carina default archetype 

## Fixes
* Updated copyright info
* Workaround applied for appium issue: https://github.com/appium/appium/issues/10159
* Hide some stacktrace messages during browser maximize as warning (mostly for the executing web tests on mobile devices/browsers)
* Cleanup in DesktopFactory removing obsolete functionality

## [DEPENDENCIES UPDATES]
* com.qaprosoft.zafira-client updated to 3.0.43

# 5.2.4.111 (2018-07-24)
==================

## Enhancements
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

## Fixes
* minor adjustments in UI operations for invisible elements on browsers
* fixed integration with ZafiraClient to specify default expiration for test artifacts (30 days)
* Resolve issues with default Timer operations (error messages instead of RuntimeException)
* Handled annoying cast exception "java.lang.ClassCastException: com.google.common.collect.Maps$TransformedEntriesMap cannot be cast to java.lang.String" which is produced mistakenly by Appium instead of StaleElementException 
* Fixed potential recursive callss in DriverListener->onException method
* Fixed vnc pattern usage for live video streaming (Web and Android mobile)
* Fixed UIX schema files generation for Android mobile pages
* Fixed IE/Edge capabilities generation for use-case with BrowserStack
* Added thread name and id into the thread log appender messaging

## [DEPENDENCIES UPDATES]
* io.appium:java-client was updated to official 6.1.0 release
* org.seleniumhq.selenium:selenium-java updated to 3.12.0
* com.qaprosoft.zafira-client updated to 3.0.40


# 5.2.4.97 (2018-06-02)

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
