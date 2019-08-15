# Release notes

### 6.1.25 (2019-07-29)
[6.1.25](https://github.com/qaprosoft/carina/releases/tag/6.1.25)

### 6.1.24 (2019-07-14)
[6.1.24](https://github.com/qaprosoft/carina/releases/tag/6.1.24)

### Starting from now release notes will be published only on Github!

### 6.1.22 (2019-06-29)

**Enhancements**

* [#736](https://github.com/qaprosoft/carina/issues/736) Updated <b>@MethodOwner</b> annotation. <b>secondaryOwner</b> parameter no longer exists, instead of this parameter added platform filter where neccessary:<br>
Note: previous single @MethodOwner annotation without platform works as well
```
@MethodOwner(owner = "user1", platform = "android")
@MethodOwner(owner = "user2", platform = "ios")
``` 
* [#749](https://github.com/qaprosoft/carina/issues/749) Added ability to set custom test data directory name. This functionality is available via <b>  ReportContext.setCustomTestDirName("customName") </b> method.
* [#751](https://github.com/qaprosoft/carina/issues/751) provided anonymous access to carina [CI jobs](https://ci.qaprosoft.com/jenkins/job/carina)
* [#752](https://github.com/qaprosoft/carina/issues/752) Added generation of [JavaDoc](https://ci.qaprosoft.com/jenkins/job/carina/job/carina-CENTRAL-RELEASE/javadoc/)
* [#754](https://github.com/qaprosoft/carina/issues/754) Renamed <i>browser_locale</i> property to <i>browser_language</i><br>
<b>Note:</b> make sure to update your project  _config.properties accordingly to be able to override browser language!
* Added release and snapshot build job statuses to [carina github](https://github.com/qaprosoft/carina)
![image](https://user-images.githubusercontent.com/4551455/60375087-3de3bc00-9a10-11e9-828c-7cdcf561a5a3.png)

**Fixes**

* Browser resize moved to DesktopFactory only to minimize erros in mobile web tests. Browser window resolution sets according to <b>capabilites.resolution</b> values otherwise maximizes window using old functionality.

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* zafira-client 4.0.56 -> 4.0.57

**Migration Steps**

* Remove all occurrences of <b>secondaryOwner</b> parameter for <i>@MethodOwner</i> annotation. Use platform filter based on information above instead
* To complete migration replace in your _config.properties all occurrences of <i>browser_locale</i> to <i>browser_language</i>



### 6.1.21 (2019-06-09)

**Enhancements**

* Added full integration with [ZebRunner](https://zebrunner.com/) (Super Fast Test Automation Cloud Solution)
* Cleaned up Carina archetype in accordance with the latest changes (dependencies, removal of obsolete files, etc.)
* Released 6.1.21 archetype and updated "Getting started" document accordingly
* Redesigned integration with S3 in scope of [#703](https://github.com/qaprosoft/carina/issues/703) reusing valid functionality from ZafiraClient and removing obsolete Amazon properties from config.properties
* [#720](https://github.com/qaprosoft/carina/issues/720) Migrate AndroidUtils and IOSUtils to functional interfaces enhancement:
  * platform independent IMobileUtils->isAppRunning() implemented
  * AndroidService->checkCurrentDeviceFocus() deprecated
  * removed already deprecated executeKeyEvent, pressKeyCode, pressBack, swipeInContainer, waitUntilElementNotPresent from IAndroidUtils

**Fixes**

* [#745](https://github.com/qaprosoft/carina/issues/745) Bump up com.fasterxml.jackson.core:jackson-databind to 2.9.9 bug
* [#744](https://github.com/qaprosoft/carina/issues/744) Upgrade jacoc-maven-plugin to 0.8.4 in carina and carina-archetype
* [#740](https://github.com/qaprosoft/carina/issues/740) Unable to take a screenshot on a test failure bug
* [#731](https://github.com/qaprosoft/carina/issues/731) Rerun failures and logic onTestFailure doesn't work for the tests with dependsOnMethods
* [#727](https://github.com/qaprosoft/carina/issues/727) Unable to deploy fresh documentation using upgraded pipeline to v 4.0
* [#703](https://github.com/qaprosoft/carina/issues/703) UI dump report for mobile runs should be published to S3 as an artifact bug
* fixed NPE in type method when null was provided as an argument

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* maven-surefire-plugin 3.0.0-M3
* jacoco-maven-plugin 0.8.2 -> 0.8.4
* jackson-databind 2.8.8 -> 2.8.9



### 6.1.20 (2019-05-17)

**Enhancements**

* integrated with <b>4.0.53</b> Zafira Server and <b>4.0.56</b> Zafira Client!<br>
<b>Important: </b> Please, don't migrate to this core version without upgrading Zafira Reporting Tool.
* [#715](https://github.com/qaprosoft/carina/issues/715) Bump up to 4.0.56 zafira-client with fixes for dynamic tags registration (#701)
* [#461](https://github.com/qaprosoft/carina/issues/461) Added support for web mobile execution on real devices using embedded QPS-HUB: Chrome, Firefox
  * Also coming soon:
    * Samsung Native browser, Edge, Opera, Opera Mini and Yandex mobile browsers
* Added localized date and time settings support for Android 9
* [#722](https://github.com/qaprosoft/carina/issues/722) Deprecated com.qaprosoft.carina.core.foundation.utils.android.Androidutils and com.qaprosoft.carina.core.foundation.utils.ios.IosUtils.<br>
Functional IAndroidUtils and IIosUtils added instead
* [#621](https://github.com/qaprosoft/carina/issues/621) Removed CustomTypePageFactory<br>
<b>Note:</b> Reuse ICustomTypePageFactory functional interface updating imports and removing static calls

**Fixes**

* [#711](https://github.com/qaprosoft/carina/issues/711)  Fixed Maven compiler source and target argument using 1.8 Java for both
* Hide "Timer not stopped for operation: .." to debug level as it is not so important

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* zafira-client 3.4.55 -> 4.0.56



### 6.0.19 (2019-05-01)

**Enhancements**

* integrated with <b>4.0.52</b> Zafira Server!<br>
<b>Important: </b>Please, don't migrate to this core version without upgrading Zafira Reporting Tool.
* Bump up to 3.4.55 zafira-client which support ZAFIRA_ARTIFACTS_USE_PROXY feature for permanent artifacts storing in AWS S3

**Fixes**

* N/A

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* zafira-client 3.4.54 -> 3.4.55



### 6.0.18 (2019-04-15)

**Enhancements**

* integrated with <b>3.4.51</b> Zafira Server!<br>
<b>Important: </b>Please, don't migrate to this core version without upgrading Zafira Reporting Tool.
* Finished with artifacts uploading improvements to S3 including asynchronous uploading
* Add Google spreadsheet data provider
```
@Test(dataProvider = "SingleDataProvider")
@XlsDataSourceParameters( spreadsheetId = "1G....", sheet = "urls", executeValue = "TRUE", dsUid = "TUID", dsArgs = "TUID, ARG1, ARG2")
public void test(String TUID, String ARG1, String ARG2)
```
* added support for mobile web tests execution against QPS-HUB in qps-infra
* Published howto article about @ExtendedFindBy() usage for mobile elements

**Fixes**

* N/A

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* maven-surefire-plugin 2.22.1 -> 3.0.0-M3
* zafira-client 3.3.51 -> 3.4.54



### 6.0.16 (2019-03-15)

**Enhancements**

* integrated with <b>3.3.48</b> Zafira Server!<br>
<b>Important: </b>Please, don't migrate to it's core version without upgrading Zafira Reporting Tool.
* Improved artifacts uploading to amazon S3. Updated existing screenshots capturing with ability to register important screenshots as test artifacts
* introduced new "ignore_ssl=false" property for carina-api module. If enabled api tests ignore handshake exception during tests run

**Fixes**

* Hopefully  [permanent fix](https://github.com/qaprosoft/carina/commit/6916aa54ccf47795f278f6ff088f34ac4590eeb3) delivered for a mess with capturing screenshots over the died driver<br>
Note: in case of any exception in child thread TestNG crash execution of it's logic and do not execute after methods/classes etc. Our DriverListener is a child thread.
* [#673](https://github.com/qaprosoft/carina/issues/673) Fixed extra places with potential NPE

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* zafira-client 3.3.49 -> 3.3.51



### 6.0.12 (2019-02-25)

**Enhancements**

* [#648](https://github.com/qaprosoft/carina/issues/648) Implement findByImage strategy
* Added [instructions](http://qaprosoft.github.io/carina/automation/mobile/) "How to use Find by image strategy".
* Avoid input field clearing for empty control

**Fixes**

* closing BEFORE_CLASS drivers during "onFinish(ITestContext context)"
* Added extra debug logging messages for FtpUtils to simplify failures debugging
* declared ARTIFACTS_EXPIRATION_SECONDS as configuration parameter to see it's value in log

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* jackson-databind 2.8.11.3 -> 2.9.8
* jackson-annotations 2.9.8



### 6.0.11 (2019-02-05)

**Enhancements**

* N/A

**Fixes**

* [#652](https://github.com/qaprosoft/carina/issues/652) web video quality property is missed

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* N/A



### 6.0.10 (2019-01-29)

**Enhancements**

* [#566](https://github.com/qaprosoft/carina/issues/566) Add new parameters for video recording to match appium 1.10 implementation
* Prepared MobileSampleTest for iOS/Android and internal application in carina-demo

**Fixes**

* ExtendedWebElement.scrollTo() refactored using up to date Locatable class

**Known Issues**

* [#637](https://github.com/qaprosoft/carina/issues/637) Abnormal behavior when swiping left or right
More information requested
* [#652](https://github.com/qaprosoft/carina/issues/652) web video quality property is missed

**[DEPENDENCIES UPDATES]**

* N/A



### 6.0.9 (2019-01-22)

**Enhancements**

* [#618](https://github.com/qaprosoft/carina/issues/618) Update appium java client to the latest version (7.0.0)
* [#622](https://github.com/qaprosoft/carina/issues/622) Create Zoom in/out in MobileUtils/IMobileUtils
* [#534](https://github.com/qaprosoft/carina/issues/534), [#563](https://github.com/qaprosoft/carina/issues/563) Improved BrowserMob proxy coverage by unit tests including example for secure traffic sniffer.
Take a [look](https://github.com/qaprosoft/carina/blob/e723fbdc5adce0447d47b5bfbfc1533f17f632ce/carina-proxy/src/test/java/com/qaprosoft/carina/browsermobproxy/BrowserMobTest.java#L96) for example.
Also disabled proxy_set_to_system=false property by default. It should be enabled only if you have to use your internal corporate proxy to have access to internet.
 
* [#436](https://github.com/qaprosoft/carina/issues/436), [#326](https://github.com/qaprosoft/carina/issues/326)  Registering device information for unsuccessful driver start. Available only in scope of https://www.qps-infra.io and https://mobiletesting.farm<br>
<B>Note:</B> Appium should be patched to incorporate custom details into the exception (system-calls.js)

**Fixes**

* [#625](https://github.com/qaprosoft/carina/issues/625) AbstractTest doesn't implement ICustomTypePageFactory
* [#629](https://github.com/qaprosoft/carina/issues/629) Annoying INFO message: Value not resolved by key: PRODUCTION.url
* [#636](https://github.com/qaprosoft/carina/issues/636) operations with relative elements in AbstractUiObject are broken
* [#634](https://github.com/qaprosoft/carina/issues/634) do not restart proxy during driver restart on the same device

**Known Issues**

* [#637](https://github.com/qaprosoft/carina/issues/637) Abnormal behavior when swiping left or right

**[DEPENDENCIES UPDATES]**

* io.appium.java-client 6.1.0 -> 7.0.0



### 6.0.8 (2019-01-15)

**Enhancements**

* [#614](https://github.com/qaprosoft/carina/issues/614) refactor CustomTypePageFactory to interface<br>
<b>Note:</b> CustomTypePageFactory.initPage(...) should be replaced by initPage(...)
also we again return initPage(...) method without driver as argument
```
CustomTypePageFactory.initPage(...); 
// it should be replaced in most cases by
initPage(...);
```
* [#608](https://github.com/qaprosoft/carina/issues/608)  update copyright info using 2019

* [#612](https://github.com/qaprosoft/carina/issues/612) Test execution by filter rules. 
Annotations for future filtering can be set in tests in following way for test method and inside <b>config.properties</b>: 
```
	@TestPriority(Priority.P1)
	@MethodOwner(owner = "owner")
	@TestTag(name = "feature", value = "reg")

	// _config.properties:
	test_run_rules=PRIORITY=>P1&amp;&amp;P2;;OWNER=>owner;;TAGS=>tag1=temp&amp;&amp;feature=reg
	rules logic: test_run_rules={RULE_NAME_ENUM}=>{RULE_VALUE1}&&{RULE_VALUE2};;...

	//Listener should be added in pom.xml or in required suite
	<listener class-name="com.qaprosoft.carina.core.foundation.listeners.FilterTestsListener" /> 
```

**Fixes**

* [#533](https://github.com/qaprosoft/carina/issues/533) Default url parameter is not detected by envLoader
* [#613](https://github.com/qaprosoft/carina/issues/613) openURL functionality refactoring
* Improved hasDependencies logic using short and full qualified class/method names
* Improved negative use-case handlers avoiding loop in making screenshots and generating UiDump
* Added deprecation javadoc for DriverHelper.tapWithCoordinates(double x, double y)
* [#623](https://github.com/qaprosoft/carina/issues/623) LOGGER is not being recognized correctly in 6.0.7

**Known Issues**

* N/A

**[DEPENDENCIES UPDATES]**

* N/A 



### 6.0.6 (2019-01-07)

**Enhancements**

* [#607](https://github.com/qaprosoft/carina/issues/607) refactor DevicePool logic integrating it completely into DriverPool<br>
<b>Note:</b> Due to the removed DevicePool class some code refactoring on project level needed. 
Mostly for devicePool.getDevice() which is not available anymore:
```
DevicePool.getDevice(); 
// it should be replaced in most cases by
getDevice();

// for the use-case when you need it in static way use
IDriverPool.getDefaultDevice();

// Also we have new methods to get device by driver name!
getDevice("default"); // return device assigned to "default" driver
getDevice("custom"); //return device addigned to "custom" driver.
// if no driver discovered in the pool with provided name then nullDevice will be returned
```
* [#610](https://github.com/qaprosoft/carina/issues/610) Declared easy way to control drivers quit on project layer. You can disable core logic with no way to enable it back for current test run. Anyway, Carina will close drivers on suite finish. 
```
// Execute as only you would like to disable automatic drivers quit
CarinaListener.disableDriversCleanup();
```
* [#581](https://github.com/qaprosoft/carina/issues/581) Agreed to avoid drivers keep for depenent groups. In case you need it please disable automatic drivers quit according to the step above and organize quit using your own logic.
* [#599](https://github.com/qaprosoft/carina/issues/599) Upgrade com.fasterxml.jackson.core:jackson-databind to version 2.8.11.3 or later

**Fixes**

* Fixed potential recursive loops in DriverListener during failures detection
* [#605](https://github.com/qaprosoft/carina/issues/605) review and complete IDriverPool TODO's
* [#609](https://github.com/qaprosoft/carina/issues/609) hide "Can't save file to Amazon S3!" error message stack-trace
* [#604](https://github.com/qaprosoft/carina/issues/604) device name for the test is not registered in Zafira
* [#584](https://github.com/qaprosoft/carina/issues/584) DevicePool.getDevice() returns different objects of the same device

**Known Issues**

* N/A 

**[DEPENDENCIES UPDATES]**

* com.fasterxml.jackson.core: jackson-databind 2.8.11.1->2.8.11.3



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

**Enhancements**

* N/A

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
* Moved unit tests into the valid modules to be able to track coverage results in [Sonar](https://ci.qaprosoft.com/sonarqube/dashboard/index?did=2)
* Move some noisy log messages onto the DEBUG level
* added possibility to redefine log level for explicit sub-modules/classes
```
core_log_level=DEBUG
core_log_packages=IDriverPool,ZafiraConfigurator
```
* added support for Opera browser. For now both variants are ok:
    * browser=opera
    * browser=operablink 
* Removed all TestRail updaters from Carina and all required dto classes. Integration with 3rd party testcase management tools will be allowed through the [zafira](https://github.com/qaprosoft/zafira) only!
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

* N/A

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
* Added [documentation](http://qaprosoft.github.io/carina/automation/mobile/) about CustomTypePageFactory usage to generate unified Desktop/Android/iOS test classes.
* Added live VNC streaming support for drivers started in before suite/class/method actions
* Added new public static method DevicePool.isRegistered()
* Updated carina default archetype

**Fixes**

* Updated copyright info
* Workaround applied for appium [issue](https://github.com/appium/appium/issues/10159)
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

* MobileUtils - migrated tap etc operations onto the TouchOptions etc<br>
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

* MobileUtils - migrated tap etc operations onto the TouchOptions etc<br>
Note: if in your code thera direct references onto the TouchActions->tap operations you should update them as well because in 6.0.0 java appium client deprecated methods were removed.
Example: 859d1f0

* Split click operation for separated Web and Mobile actions to minimize negative side-effects

**Fixes**

* minor adjustments in UI operations for invisible elements on browsers

**[DEPENDENCIES UPDATES]**

* io.appium:java-client was updated to official 6.0.0 release
* org.seleniumhq.selenium:selenium-java updated to 3.12.0
