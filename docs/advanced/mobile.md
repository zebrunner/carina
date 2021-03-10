Carina framework provides a useful and elegant way of Mobile (Android and iOS) Test Automation. The best practices have a lot in common with web automation, so it’s highly recommended to look through [Web automation article](http://qaprosoft.github.io/carina/automation/web/).

### Mobile special requirements:
To run mobile tests, [Appium](http://appium.io/) is used instead of Selenium. There are 2 versions of Appium: desktop and console ones, and both are good for Carina. <b>Appium must be running every time before the test run.</b>

![Alt text](../img/appium_design.png "Appium")
	
### Android special requirements:	
1. Android SDK (part of [Android Studio](https://developer.android.com/studio/)) is an important component for work. Pay attention that after installing Android Studio you sometimes (depends on a version) need to additionally install ADB and aapt (for Mac only). 
2. Edit your PATH variable and add ANDROID_HOME (path to “sdk” folder) to PATH. 
Hint: you sometimes (especially on Mac) need to add paths to important folders inside sdk, such as “platform-tools” (ADB is located here), “tools” and “build-tools” (aapt is located here).
3. .apk file - installation file of a program that’s being tested is required, the same for both - a real device and an emulator.

### iOS special requirements:
1. [Xcode](https://developer.apple.com/xcode/) is a vital component for work, but unfortunately, it’s Mac-used only. It’s impossible to do iOS automation on Windows.
2. Installation file of a program that’s being tested is required. For a real device it’s .ipa file, and for a simulator it’s .app file. .app file should be provided by developers and has special signatures to work correctly, but automation on the simulator is free. On the other hand, the license is needed to work with the real device.

### Mobile config properties
We can provide any Appium capability in the **config.properties** file using capabilities.name=value format. In the table below we are providing the description of the most popular mobile capabilities:

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
		<td>The only custom Carina capability to determine the type of a device</td>
                <td>n/a</td>
		<td>phone/tablet/tv...</td>
	</tr>
        <tr>
		<td>capabilities.platformName</td>
		<td>Name of the mobile platform</td>
                <td>n/a</td>
		<td>Android/iOS/AndroidTV/tvOS</td>
	</tr>
        <tr>
		<td>capabilities.platformVersion</td>
		<td>Version of the mobile platform</td>
                <td>n/a</td>
		<td>6.0.1</td>
	</tr>
        <tr>
		<td>capabilities.automationName</td>
		<td>Name of the program used for automation (for Android 7+ use uiautomator2 instead of Appium)</td>
                <td>n/a</td>
		<td>Appium/uiautomator2/XCUITest</td>
	</tr>
        <tr>
		<td>capabilities.app</td>
		<td>Path to the application (apk/app/ipa) which is tested, can be provided as a pattern from AWS S3 storage with automatic downloading</td>
                <td>n/a</td>
		<td>D:/application.apk, s3://qaprosoft.com/android/myapk.*-release.apk</td>
	</tr>
        <tr>
		<td>capabilities.newCommandTimeout</td>
		<td>New implicit timeout in seconds to wait for the element for mobile automation</td>
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
		<td>Skips checking and signing of the app with debug keys, will work only with UiAutomator and not with selendroid, defaults to false</td>
                <td>n/a</td>
		<td>true, false</td>
	</tr>
	<tr>
		<td>capabilities.autoGrantPermissions</td>
		<td>Has Appium automatically determine which permissions your app requires and grants them to the app on install. Defaults to false</td>
                <td>n/a</td>
		<td>true, false</td>
	</tr>
	<tr>
		<td>capabilities.skipUnlock</td>
		<td>Skips the device unlock process. Defaults to false</td>
                <td>n/a</td>
		<td>true, false</td>
	</tr>
</table>

<b>Important:</b>
* though it’s not necessary to run Selenium, seleniumum_host must be defined.
* The file _config.properties should be located in src/main/resources

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
To get the device’s unique id (UDID), the command “adb devices” is used. This command provides a list of devices attached with their UDIDs. For a real device, it will look like “759b543c” (can be shorter or longer), and for an emulator, it will be like “emulator-5554”.

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
To get the device’s unique id (UDID) for an emulator, the command “xcrun simctl list” is used. It will show the list of simulators with their names, iOS versions, udids and statuses (shutdown or booted). 
To get UDID from the real device, you need to plug your phone into a computer and copy it out of iTunes:
1. Launch iTunes and connect your iPhone.
2. In the right panel, locate the information about your iPhone, including its name, capacity, software version, serial number, and phone number.
3. Reveal the Identifier by clicking on the Serial Number.

### Implementation of Page Objects:
The main idea is the same as in [web-testing](http://qaprosoft.github.io/carina/automation/web/#implementation-of-page-objects) except that CSS isn’t used in Mobile. 
ExtendedWebElement (Carina’s implementation of WebElement) is used instead of WebElement, and it has methods similar to those that WebElement has, but more reliable and convenient ones.
<b>Important</b>:
 * Page should extend com.qaprosoft.carina.core.gui.AbstractPage
 * Use com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement instead of Selenium WebElement
 * Locate Page Object classes in src/main/java package

### How to find locators for Android application
To obtain the locators of web elements from an Android app, different programs are used, such as the latest Appium itself and a convenient Android SDK tool: uiautomatorviewer. It’s located in $ANDROID_HOME/tools/bin and can be launched using the command line  (./uiautomatorviewer).
Example:
```
 @FindBy(xpath = "//*[@resource-id='name_input']")
 private ExtendedWebElement input;
```


### How to find locators for iOS application
To obtain the locators of web elements from an iOS app, different programs are used, such as GUI Appium itself and a convenient [Macaca App Inspector](https://macacajs.github.io/app-inspector/).
To speed up the work, @Predicate is used. Complicate “xpaths” can’t be used with predicates, but “id”, “name”, “className” and etc. search is sufficient. 
Example:
```
@FindBy(xpath = "name = 'DONE'")
@Predicate
protected ExtendedWebElement doneButton;
```
Another possibility to find the element is to use @ClassChain annotation.
Example:
```
@FindBy(xpath = "**/XCUIElementTypeStaticText[`name=='Developer'`]")
@ClassChain
protected ExtendedWebElement developerText;
```

Starting from Carina version 6.0.12, it's recommended to use @ExtendedFindBy() annotation.
Example:
```
@ExtendedFindBy(iosClassChain = "**/XCUIElementTypeStaticText[`name=='Developer'`]")
protected ExtendedWebElement developerText;
```
or 
```
@ExtendedFindBy(iosPredicate = "name = 'DONE'")
protected ExtendedWebElement developerText;
```

### Implementation of tests
Carina framework uses TestNG for test organization. In general, test represents a manipulation with Page Objects and additional validations of UI events. Here is sample test implementation:

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
Carina provides the technique to combine Desktop/iOS/Android tests into the single test class/method. For all platforms you should use [Page Object Design Pattern](https://www.seleniumhq.org/docs/06_test_design_considerations.jsp#page-object-design-pattern), but in a bit improved way.
Every page has an abstract declaration and different implementations if needed (by default, 3 ones should be enough: Desktop, iOS/Android):

 * Common abstract page in a common package with common methods and elements;
 * Desktop page in a desktop package with desktop methods and elements;
 * iOS page in ios package with iOS methods and elements;
 * Android page in android package with Android methods and elements.

Child pages should extend BasePage implementing all abstract methods. Annotation @DeviceType will provide the information about the device type and the parent (common) page.

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

Inside every test, Carina operates with an abstract base page using CustomTypePageFactory and substitutes it by the real implementation based on the desired capabilities in _config.properties etc.

**Example:**
```
@Test
    public void comparePhonesTest() {
        HomePageBase homePage = CustomTypePageFactory.initPage(getDriver(), HomePageBase.class);
        ComparePageBase phoneFinderPage = homePage.openCompare();
        ...
    }
```

If there are differences in application according to OS version, just implement the pages for different versions and include the version parameter in @DeviceType for every page.

**Example:**

For Android 8 (either 8.0 or 8.1)
```
@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, version = “8”, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {
```

Or for a specific version
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
The list of actions with image elements and related driver settings is available [here](http://appium.io/docs/en/advanced-concepts/image-elements/).

Basically, all you need is to create an image template of the element in .png format and place it to your project. We suggest using ```src/main/resources/``` folder to store images. 
Be sure your image size is less than the real screen size. Real iOS screen sizes are listed [here](https://developer.apple.com/library/archive/documentation/DeviceInformation/Reference/iOSDeviceCompatibility/Displays/Displays.html) in 'UIKit Size (Points)' column. You can also find the ultimate guide to iPhone resolutions [here](https://www.paintcodeapp.com/news/ultimate-guide-to-iphone-resolutions).


