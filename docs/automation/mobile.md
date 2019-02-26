Carina framework provides the useful and elegant way of Mobile (Android and iOS) Test Automation. The best practices have a lot in common with web automation, so it’s highly recommended to look through [Web automation article](http://qaprosoft.github.io/carina/automation/web/).

### Mobile special requirements:
To run mobile tests [Appium](http://appium.io/) is used instead of Selenium. There are 2 versions of Appium: desktop and console and both are good for Carina. <b>Appium has to be running every time before the test run.</b>

![Alt text](../img/appium_design.png "Appium")
	
### Android special requirements:	
1. Android SDK(part of [Android Studio](https://developer.android.com/studio/)) is an important component for work. Pay attention that after installing Android Studio sometimes (depends on version) you need to additionally install ADB and aapt (for mac only). 
2. Edit your PATH variable and add ANDROID_HOME (path to “sdk” folder) to PATH. 
Hint: sometimes (especially on Mac) you need to add paths to important folders inside sdk, such as “platform-tools” (here is ADB located), “tools” and “build-tools”(here is aapt located).
3. .apk file - installation file of a program that’s being tested is required, the same for both - real device and emulator.

### iOS special requirements:
1. [Xcode](https://developer.apple.com/xcode/) is vital component for work, but unfortunately, it’s Mac-used only. It’s impossible to do iOS automation on Windows.
2. Installation file of a program that’s being tested is required. For real device it’s .ipa file and for simulator it’s .app file. .app file should be provided by developers and has special signatures to work correctly, but automation on simulator is free. On the other hand, the license is needed to work with the real device.

### Mobile config properties
We could provide any Appium capabilty in **config.properties** file using capabilities.name=value format. In the table below we are providing description for the most popular mobile capabilities:

<table>
	<tr>
		<th>Attribute</th>
		<th>Meaning</th>
                <th>Default value</th>
		<th>Example</th>
	</tr>
        <tr>
		<td>capabilities.deviceName</td>
		<td>Device name for report</td>
                <td>n/a</td>
		<td>Sumsung_Galaxy_J5</td>
	</tr>
        <tr>
		<td>capabilities.deviceType</td>
		<td>The only custom carina capability to detmine type of device</td>
                <td>n/a</td>
		<td>phone/tablet/tv...</td>
	</tr>
        <tr>
		<td>capabilities.platformName</td>
		<td>Name of mobile platform</td>
                <td>n/a</td>
		<td>Android/iOS/AndroidTV/tvOS</td>
	</tr>
        <tr>
		<td>capabilities.platformVersion</td>
		<td>Version of mobile platform</td>
                <td>n/a</td>
		<td>6.0.1</td>
	</tr>
        <tr>
		<td>capabilities.automationName</td>
		<td>Name of programm using for automation (for Android 7+ use uiautomator2 instead of Appium)</td>
                <td>n/a</td>
		<td>Appium/uiautomator2/XCUITest</td>
	</tr>
        <tr>
		<td>capabilities.app</td>
		<td>Path to application (apk/app/ipa) which is tested, Can be provided as a pattern from AWS S3 storage with automatic downloading</td>
                <td>n/a</td>
		<td>D:/application.apk, s3://qaprosoft.com/android/myapk.*-release.apk</td>
	</tr>
        <tr>
		<td>capabilities.newCommandTimeout</td>
		<td>New implicit timeout in seconds to wait for element for mobile automation</td>
                <td>n/a</td>
		<td>180</td>
	</tr>
        <tr>
		<td>capabilities.udid</td>
		<td>Unique Device ID</td>
                <td>n/a</td>
		<td>759b543c</td>
	</tr>
        <tr>
		<td>capabilities.device</td>
		<td>Specifies a particular mobile device for the test environment on Browserstack</td>
                <td>n/a</td>
		<td>Galaxy Note 8, iPhone X</td>
	</tr>
	 <tr>
		<td>capabilities.os_version</td>
		<td>Version of OS for Browserstack</td>
                <td>n/a</td>
		<td>ios, android</td>
	</tr>
	<tr>
		<td>capabilities.remoteURL</td>
		<td>Remote URL for using Selenium Grid</td>
                <td>n/a</td>
		<td> 'http://localhost:4444/wd/hub'</td>
	</tr>
		<tr>
		<td>capabilities.appActivity</td>
		<td>Activity name for the Android activity you want to launch from your package.</td>
                <td>n/a</td>
		<td>com.amazon.mShop.HomeActivity</td>
	</tr>
		<tr>
		<td>capabilities.appPackage</td>
		<td>Java package of the Android app you want to run</td>
                <td>n/a</td>
		<td>in.amazon.mShop.android.shopping</td>
	</tr>
	<tr>
		<td>capabilities.noSign</td>
		<td>Skip checking and signing of app with debug keys, will work only with UiAutomator and not with selendroid, default false</td>
                <td>n/a</td>
		<td>true, false</td>
	</tr>
	<tr>
		<td>capabilities.autoGrantPermissions</td>
		<td>Have Appium automatically determine which permissions your app requires and grant them to the app on install. Defaults to false</td>
                <td>n/a</td>
		<td>true, false</td>
	</tr>
	<tr>
		<td>capabilities.skipUnlock</td>
		<td>Skip the device unlock process. Defaults to false</td>
                <td>n/a</td>
		<td>true, false</td>
	</tr>
</table>

<b>Important:</b>
* though it’s not necessary to run Selenium, seleniumum_host must be defined.
* file _config.properties should be located in src/main/resources

### Example for Android of _config.properties:
```
#=====================================================#
#================= Configuration v2 ==================#
#=====================================================#

selenium_host=http://localhost:4723/wd/hub
extra_selenium_driver_host=http://localhost:4444/wd/hub

#============ Android Local Mobile ===================#

##for Android 7+ use uiautomator2 instead of Appium
capabilities.automationName=uiautomator2
capabilities.appPackage=
capabilities.deviceName=Nexus_6
capabilities.udid=emulator-5554
capabilities.noSign=true
capabilities.deviceType=phone
capabilities.app=/Users/{user}/qaprosoft/mmf/test-app.apk
capabilities.platformName=ANDROID
capabilities.autoGrantPermissions=true
#=====================================================#
```

<b>Note:</b>
To get device’s unique id (UDID) command “adb devices” is used. This command provides a list of devices attached with their UDIDs. For real device it will look like “759b543c” (can be shorter or longer) and for emulator it will be like  “emulator-5554”.

### Example for iOS of _config.properties:
```
#=====================================================#
#================= Configuration v2 ==================#
#=====================================================#

selenium_host=http://localhost:4723/wd/hub
extra_selenium_driver_host=http://localhost:4444/wd/hub

#======== Local Run for iOS Mobile ===============#
capabilities.app=/Users/{user}/qaprosoft/mmf/TestApp-iphonesimulator.app
capabilities.platformName=iOS
capabilities.deviceName=iPhone X
capabilities.platform=iOS
capabilities.deviceType=phone
capabilities.platformVersion=11.4
capabilities.automationName=XCUITest
capabilities.udid=D85FF1CD-D95F-4B78-A007-77A11EBD3ABB
capabilities.newCommandTimeout=1800
#=====================================================#
```

<b>Note:</b>
To get device’s unique id (UDID) for emulator command “xcrun simctl list” is useful. It will show the list of simulators with their names, iOS versions, udids and statuses (shutdown or booted). 
To get UDID from the real device you have to plug your phone into a computer and copy it out of iTunes:
1. Launch iTunes and connect your iPhone.
2. In the right panel, locate the information about your iPhone, including its name, capacity, software version, serial number, and phone number.
3. Reveal the Identifier by clicking on Serial Number.

### Implementation of Page Objects:
The main idea is the same as in [web-testing](http://qaprosoft.github.io/carina/automation/web/#implementation-of-page-objects) except that css isn’t used in Mobile. 
ExtendedWebElement(Carina’s implementation of WebElement) is used instead of WebElement and it has methods similar to those that WebElement has but more reliable and convenient.
<b>Important</b>:
 * Page should extends com.qaprosoft.carina.core.gui.AbstractPage
 * Use com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement instead of Selenium WebElement
 * Locate Page Object classes in src/main/java package

### How to find locators for Android application
To obtain locators of web elements from Android app different programs are used, such as latest Appium itself and convenient Android SDK tool: uiautomatorviewer. It’s located in $ANDROID_HOME/tools/bin and could be launched using command line  (./uiautomatorviewer).
Example:
```
 @FindBy(xpath = "//*[@resource-id='name_input']")
 private ExtendedWebElement input;
```


### How to find locators for iOS application
To obtain locators of web elements from iOS app different programs are used, such as GUI Appium itself and convenient [Macaca App Inspector](https://macacajs.github.io/app-inspector/).
To faster work @Predicate is used. With predicates complicate “xpaths” can’t be used, but “id”, “name”, “className” and etc. search are sufficient. 
Example:
```
@FindBy(xpath = "name = 'DONE'")
@Predicate
protected ExtendedWebElement doneButton;
```
Another possibility to find the element is to use @ClassShain annotation.
Example:
```
@FindBy(xpath = "**/XCUIElementTypeStaticText[`name=='Developer'`]")
@ClassChain
protected ExtendedWebElement developerText;
```

### Implementation of tests
Carina framework uses TestNG for test organization. In general, test represents manipulation with Page Objects and additional validations of UI events. Here is sample test implementation:

```
public class SampleTest extends AbstractTest {
	
	String name = "My name";
	String carName = "Mercedes";
	

    @Test()
    public void sendName() {
    	FirstPage  firstPage = new FirstPage(getDriver());
    	GoogleTestPage googleTestPage = new GoogleTestPage(getDriver());
    	MyWayOfHelloPage myWayOfHelloPage = new MyWayOfHelloPage(getDriver());
    			firstPage.clickOnGooleButton();
    			googleTestPage.setName(name);
    			googleTestPage.clickOnSpinner();
    			googleTestPage.selectCar(carName);
    			googleTestPage.clickOnSendYourNameButton();
    			Assert.assertTrue(myWayOfHelloPage.isTextElementPresent(name), “Assert message” );
    			Assert.assertTrue(myWayOfHelloPage.isTextElementPresent(carName.toLowerCase()), “Assert message” );
    			
    }

}
```

<b>Important:</b>
* Test class should extend com.qaprosoft.carina.core.foundation.AbstractTest
* Test method should start with org.testng.annotations.Test annotation
* Use getDriver() method to get driver instance in test
* Locate tests in src/test/java source folder

### How to use CustomTypePageFactory
Carina provides technique to combine Desktop/iOS/Android tests into the single test class/method. For all platforms you should use [Page Object Design Pattern](https://www.seleniumhq.org/docs/06_test_design_considerations.jsp#page-object-design-pattern) but in a bit improved way.
Each page has abstract declaration and different implementations if needed (by default 3 ones should be enough: Desktop, iOS/Android):

 * Common abstract page in common package with common methods and elements;
 * Desktop page in desktop package with desktop methods and elements;
 * iOS page in ios package with iOS methods and elements;
 * Android page in android package with Android methods and elements.

Child pages should extends BasePage implementing all abstract methods. Annotation @DeviceType would provide information about device type and parent (common) page.

**Examples:**

**Common (Base) Page**
```
public abstract class HomePageBase extends AbstractPage {

    public HomePageBase(WebDriver driver) {
        super(driver);
    }

    public abstract PhoneFinderPageBase openPhoneFinder();

    public abstract ComparePageBase openComparePage();
}
```

**Android Page**
```
@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {

    @FindBy(xpath = "//android.widget.TextView[@resource-id='itemTitle' and @text='Phone Finder']")
    protected ExtendedWebElement phoneFinderTextView;

    @FindBy(xpath = "//android.widget.TextView[@resource-id='itemTitle' and @text='compare']")
    protected ExtendedWebElement compareTextView;

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Override
    public PhoneFinderPageBase openPhoneFinder() {
        phoneFinderTextView.click();
        return CustomTypePageFactory.initPage(getDriver(), PhoneFinderPageBase.class);
    }

    @Override
    public ComparePageBase openComparePage() {
        compareTextView.click();
        return CustomTypePageFactory.initPage(getDriver(), ComparePageBase.class);
    }
```

**iOS Page**
```
@DeviceType(pageType = Type.IOS_PHONE, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {

    @FindBy(xpath = "name = 'Phone Finder'")
    @Predicate
    private ExtendedWebElement phoneFinderTextView;

    @FindBy(xpath = "name = 'Compare'")
    @Predicate
    private ExtendedWebElement compareTextView;

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Override
    public PhoneFinderPageBase openPhoneFinder() {
        phoneFinderTextView.click();
        return CustomTypePageFactory.initPage(getDriver(), PhoneFinderPageBase.class);
    }

    @Override
    public ComparePageBase openComparePage() {
        compareTextView.click();
        return CustomTypePageFactory.initPage(getDriver(), ComparePageBase.class);
    }
```

Inside each tests Carina operates with abstract base page using CustomTypePageFactory and substitute it by real implementation based on desired capabilities in _config.properties etc.

**Example:**
```
@Test
    public void comparePhonesTest() {
        HomePageBase homePage = CustomTypePageFactory.initPage(getDriver(), HomePageBase.class);
        ComparePageBase phoneFinderPage = homePage.openCompare();
        ...
    }
```

If there are differences in application according to OS version just implement pages for different versions and include version parameter in @DeviceTypeae for each page

**Example:**

For Android 8 (either 8.0 or 8.1)
```
@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, version = “8”, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {
```

Or for specific version
```
@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, version = “8.1”, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {
```

### How to use Find by image strategy

Find by image strategy is based on [appium implementation](https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/image-comparison.md). Be sure you have openCV libraries installed to [your system](https://github.com/justadudewhohacks/opencv4nodejs).
Find by image logic is covered by ```@ExtendedFindBy``` annotation. 

**Example:**
```
@ExtendedFindBy(image = "images/singUp6.png")
private ExtendedWebElement signUpBtn;
```
Available list of actions with image elements and related driver settings are listed [here](http://appium.io/docs/en/advanced-concepts/image-elements/).

Basically all you need is to create image template of the element in .png format and place it to your project. We suggest to use ```src/main/resources/``` folder to store images. 
Be sure your image size less then real screen size. Real iOS screen sizes are listed [here](https://developer.apple.com/library/archive/documentation/DeviceInformation/Reference/iOSDeviceCompatibility/Displays/Displays.html) in 'UIKit Size (Points)' column. 


