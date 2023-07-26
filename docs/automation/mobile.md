Carina framework provides a useful and elegant way of Mobile (Android and iOS) Test Automation. The best practices have a lot in common with web automation, so it's highly recommended to look through [Web automation article](http://zebrunner.github.io/carina/automation/web/).

### Mobile special requirements:
To run mobile tests, [Appium](http://appium.io/) is used instead of Selenium. There are 2 versions of Appium: desktop and console ones, and both are good for Carina. <b>Appium must be running every time before the test run.</b>

![Alt text](../img/appium_design.png "Appium")
	
### Android special requirements:	
1. Android SDK (part of [Android Studio](https://developer.android.com/studio/)) is an important component for work. Pay attention that after installing Android Studio you sometimes (depends on a version) need to additionally install ADB (Mac only). 
2. Edit your PATH variable and add ANDROID_HOME (path to "sdk" folder) to PATH. 
  > Sometimes (mostly on Mac) need to add paths to important folders inside sdk, such as "platform-tools" (ADB is located here), "tools" and "build-tools".
3. apk file - installation file of a program that's being tested is required, the same for both - a real device and an emulator.

### iOS special requirements:
1. [Xcode](https://developer.apple.com/xcode/) is a vital component for work so iOS testing is available only on Mac OS.
2. Installation file of a program that's being tested is required. For a real device it's ipa file, and for a simulator it is app file. App file should be provided by developers and has special signatures to work correctly.

### Mobile config properties
We can provide any Appium capability in the **_config.properties** file using `capabilities.name=value` format. In the table below we are providing the description of the most popular mobile capabilities:

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
</table>

Actual list of Appium capabilities can be found [here](https://appium.io/docs/en/2.0/guides/caps/#appium-capabilities).

### Example for Android of _config.properties:
```properties
selenium_url=http://localhost:4723/wd/hub
#============ Android Local Mobile ===================#
capabilities.platformName=ANDROID
capabilities.deviceName=Nexus_6
capabilities.app=https://qaprosoft.s3-us-west-2.amazonaws.com/carinademoexample.apk
capabilities.noSign=true
capabilities.autoGrantPermissions=true
#=====================================================#
```

### Example for iOS of _config.properties:
```properties
selenium_url=http://localhost:4723/wd/hub
#======== Local Run for iOS Mobile ===============#
capabilities.platformName=iOS
capabilities.deviceName=iPhone X
capabilities.app=https://qaprosoft.s3-us-west-2.amazonaws.com/carinademoexample.zip
#=====================================================#
```

### Implementation of Page Objects:
The main idea is the same as in [web-testing](http://zebrunner.github.io/carina/automation/web/#implementation-of-page-objects). 

### How to find locators for Android application
To obtain the locators of elements from an Android app different programs are used such as Appium itself or convenient Android SDK tool: `uiautomatorviewer`.
Example:
```java
 @FindBy(xpath = "//*[@resource-id='name_input']")
 private ExtendedWebElement input;
```


### How to find locators for iOS application
To obtain the locators of elements from an iOS app different programs are used such as Appium itself or convenient [Macaca App Inspector](https://macacajs.github.io/app-inspector/).
To speed up element detection @Predicate annotation can be used used. Complicate "xpath" can't be used with predicates. 
Example:
```java
@FindBy(xpath = "name = 'DONE'")
@Predicate
protected ExtendedWebElement doneButton;
```
Another possibility to find the element is to use @ClassChain annotation.
Example:
```java
@FindBy(xpath = "**/XCUIElementTypeStaticText[`name=='Developer'`]")
@ClassChain
protected ExtendedWebElement developerText;
```

Starting from Carina version 6.0.12, it's recommended to use @ExtendedFindBy() annotation.
Example:
```java
@ExtendedFindBy(iosClassChain = "**/XCUIElementTypeStaticText[`name=='Developer'`]")
protected ExtendedWebElement developerText;
```
or 
```java
@ExtendedFindBy(iosPredicate = "name = 'DONE'")
protected ExtendedWebElement developerText;
```

### Implementation of tests
Carina framework uses TestNG for test organization. In general, test represents a manipulation with Page Objects and additional validations of UI events. Here is sample test implementation:

```java
public class SampleTest implements IAbstractTest {

    String name = "My name";
    String carName = "Mercedes";

    @Test()
    public void sendName() {
    	FirstPage firstPage = new FirstPage(getDriver());
        firstPage.clickOnGooleButton();

        GoogleTestPage googleTestPage = new GoogleTestPage(getDriver());
        googleTestPage.setName(name);
        googleTestPage.clickOnSpinner();
        googleTestPage.selectCar(carName);
        googleTestPage.clickOnSendYourNameButton();
        
        MyWayOfHelloPage myWayOfHelloPage = new MyWayOfHelloPage(getDriver());
        Assert.assertTrue(myWayOfHelloPage.isTextElementPresent(name), "Assert message" );
        Assert.assertTrue(myWayOfHelloPage.isTextElementPresent(carName.toLowerCase()), "Assert message" );
    }
}
```

<b>Important:</b>
* Test class should implement com.zebrunner.carina.core.IAbstractTest
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

Children pages should extend BasePage implementing all abstract methods. Annotation @DeviceType will provide the information about the device type and the parent (common) page.

**Examples:**

**Common (Base) Page**
```java
public abstract class HomePageBase extends AbstractPage {

    public HomePageBase(WebDriver driver) {
        super(driver);
    }

    public abstract PhoneFinderPageBase openPhoneFinder();

    public abstract ComparePageBase openComparePage();
}
```

**Android Page**
```java
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
```java
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
}
```

Inside every test, Carina operates with an abstract base page using CustomTypePageFactory and substitutes it by the real implementation based on the desired capabilities in _config.properties etc.

**Example:**
```java
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
```java
@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, version = "8", parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {
    ...
}
```

Or for a specific version
```java
@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, version = "8.1", parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {
    ...
}
```

### How to use Find by Image strategy

Find by image strategy is based on [appium implementation](https://github.com/appium/appium/blob/master/packages/images-plugin/docs/image-comparison.md). Be sure you have OpenCV libraries installed to [your system](https://github.com/justadudewhohacks/opencv4nodejs).
Find by image logic is covered by ```@ExtendedFindBy``` annotation. 

**Example:**
```java
@ExtendedFindBy(image = "images/singUp6.png")
private ExtendedWebElement signUpBtn;
```
The list of actions with image elements and related driver settings is available [here](http://appium.io/docs/en/advanced-concepts/image-elements/).

Basically, all you need is to create an image template of the element in .png format and place it to your project. We suggest using ```src/main/resources/``` folder to store images. 
Be sure your image size is less than the real screen size. Real iOS screen sizes are listed [here](https://developer.apple.com/library/archive/documentation/DeviceInformation/Reference/iOSDeviceCompatibility/Displays/Displays.html) in 'UIKit Size (Points)' column. You can also find the ultimate guide to iPhone resolutions [here](https://www.paintcodeapp.com/news/ultimate-guide-to-iphone-resolutions).

### How to change context of application

In carina-demo there is an example of a [tool](https://github.com/zebrunner/carina-demo/blob/master/src/main/java/com/zebrunner/carina/demo/utils/MobileContextUtils.java),
that can change context of application. Just add needed context as a field in [View](https://github.com/zebrunner/carina-demo/blob/master/src/main/java/com/zebrunner/carina/demo/utils/MobileContextUtils.java#L51) enum.
```java
// for example
NATIVE("NATIVE_APP"),
WEB1("WEBVIEW_chromeapp"),
WEB2("WEBVIEW_opera");
```

Then change context in your test/page class where needed.
```java
public void testWebView() {
    WelcomePageBase welcomePage = initPage(getDriver(), WelcomePageBase.class);
    LoginPageBase loginPage = welcomePage.clickNextBtn();
    loginPage.login();
    WebViewPageBase webViewPageBase = initPage(getDriver(), WebViewPageBase.class);
    
    MobileContextUtils contextHelper = new MobileContextUtils();
    contextHelper.switchMobileContext(View.WEB);
    
    ContactUsPageBase contactUsPage = webViewPageBase.goToContactUsPage();
    contactUsPage.typeName("John Doe");
    contactUsPage.typeEmail("some@email.com");
    contactUsPage.typeQuestion("This is a message");
    contactUsPage.submit();
    
    Assert.assertTrue(contactUsPage.isErrorMessagePresent() || contactUsPage.isRecaptchaPresent(),
        "Error message or captcha was not displayed");
}
```

### What if the mobile app link points to Amazon / Azure/ AppCenter / other

When the driver starts, a link to the application is taken from the capabilities. The type of link determines how to process it 
and get the final (for example, pre-assign) link which will be used when starting the driver instead of the original.
If you provide a link to an application located on Amazon, then add the dependency [com.zebrunner.carina-aws-s3](https://github.com/zebrunner/carina-aws-s3/releases), 
Azure - [com.zebrunner.carina-azure](https://github.com/zebrunner/carina-azure/releases), AppCenter - [com.zebrunner.carina-appcenter](https://github.com/zebrunner/carina-appcenter/releases).

You can also create your own implementation of getting the final link to the mobile application:

1. Create a class that implements the `com.zebrunner.carina.commons.artifact.IArtifactManager` interface (from `com.zebrunner.carina-commons` dependency).
The `getDirectLink` method just transforms the link to the final.
2. Create a class that implements the `com.zebrunner.carina.commons.artifact.IArtifactManagerFactory` interface and annotate it
by `com.zebrunner.carina.commons.artifact.ArtifactManagerFactory`. So this class will be discovered by Carina Framework at runtime.

If Carina Framework does not find a suitable artifact manager to generate final link, then the link will be passed to the driver as is.

## FAQ

**Where can I find mobile-specific methods?**

* [IMobileUtils](https://github.com/zebrunner/carina-webdriver/blob/master/src/main/java/com/zebrunner/carina/utils/mobile/IMobileUtils.java) -
contains methods for interacting with both IOS and Android devices
* [IAndroidUtils](https://github.com/zebrunner/carina-webdriver/blob/master/src/main/java/com/zebrunner/carina/utils/android/IAndroidUtils.java),
[AndroidService](https://github.com/zebrunner/carina-webdriver/blob/master/src/main/java/com/zebrunner/carina/utils/android/AndroidService.java) -
contains methods for interacting with Android devices only
* [IOSUtils](https://github.com/zebrunner/carina-webdriver/blob/master/src/main/java/com/zebrunner/carina/utils/ios/IOSUtils.java) -
contains methods for interacting with IOS devices only
