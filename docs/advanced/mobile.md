### Run one test on android and ios in parallel

To run tests on multiple devices, including different platforms, you can use a data provider. For example, you can create a data provider like:

```
@DataProvider(parallel = true, name = "capabilitiesDataProvider")
public static Object[][] capabilitiesDataProvider() {
    return new Object[][] {
        { "Samsung Galaxy M52 5G", "Android", "https://qaprosoft.s3-us-west-2.amazonaws.com/carinademoexample.apk" },
        { "iPhone 8 Plus", "iOS", "https://qaprosoft.s3-us-west-2.amazonaws.com/carinademoexample.zip" }
    };
}
```

and the test that this data provider uses:

```
@Test(dataProvider = "capabilitiesDataProvider")
public void testLoginUser(String deviceName, String platformName, String app) {
    R.CONFIG.put("capabilities.deviceName", deviceName, true);
    R.CONFIG.put("capabilities.platformName", platformName, true);
    R.CONFIG.put("capabilities.app", app, true);
        
    WelcomePageBase welcomePage = initPage(getDriver(), WelcomePageBase.class);
    Assert.assertTrue(welcomePage.isPageOpened(), "Welcome page isn't opened");
    ...
}
```

Thus, you can throw up any capability, capabilities will also be taken from the config file.

In the TestNG xml suite file, do not forget to specify the number of parallel streams for the data provider `data-provider-thread-count`, 
for example:

```
<suite name="Carina Demo Tests - Mobile Sample (Android)" verbose="1" parallel="methods" data-provider-thread-count="5">

```

### Recommended capability to minimize INSTALL_FAILED_INSUFFICIENT_STORAGE exception

This exception may appear due to multiple app reinstalls on the Android device.
You need to add `remoteAppsCacheLimit` capability with value `0` for all Android testing to disable caching mechanism.
This capability was delivered as part of original CE [codeline](https://github.com/zebrunner/pipeline-ce/commit/b8732cafa30c93d7d9d56e73da73e491b1f22b2c).

Explanation and original google feedback about `INSTALL_FAILED_INSUFFICIENT_STORAGE` exception might be found [here](https://issuetracker.google.com/issues/170867658?pli=1).

### Run tests on tvOS, AndroidTV

#### Run tests on tvOS

To run tests for [tvOS](https://developer.apple.com/tvos/), set the capability value of `platformName` to `tvOS` in `_config.properties`, for example:

```
capabilities.platformName=tvOS #here
capabilities.platformVersion=12.2
capabilities.deviceName=Apple TV
capabilities.deviceType=tv # carina custom capability
 ..
}
```

TvOS provides remote controller based actions. Appium provides Buttons actions via mobile: pressButton. These are menu, up/down/left/right, home, 
playpause and select. You can emulate these actions like this:

```
JavascriptExecutor driver = (JavascriptExecutor)getDriver();
Map<String, String> commandHomeAttributes = ImmutableMap.of("name", "Home"));
driver.executeScript("mobile: pressButton", commandHomeAttributes);
Map<String, String> commandUpAttributes = ImmutableMap.of("name", "Home"));
driver.executeScript("mobile: pressButton", commandUpAttributes);
```

Also, to emulate transitions between elements, you can use a simple click on the element you are looking for.

For more info visit [tvOS appium support](https://github.com/appium/appium-xcuitest-driver/blob/master/docs/ios-tvos.md).

In the implementation of tests, the idea is the same as in [mobile-testing](https://zebrunner.github.io/carina/automation/mobile/).

#### Run tests on AndroidTV

To get started, enable [developer options](https://developer.android.com/studio/debug/dev-options). To connect to a device, you need to know at 
least the Device Name, Platform Name, and Platform Version.
You specify this data as a capabilities in `_config.properties`, for example:

```
capabilities.platformName=Android
capabilities.platformVersion=11
capabilities.deviceName=Redmi TV
capabilities.deviceType=tv # carina custom capability
...
```

When testing, you will simulate pressing buttons from the remote control. To do this, you can use the **pressKeyboardKey** method from
**IAndroidUtils** with key event parameters **AndroidKey.DPAD_UP**, **AndroidKey.DPAD_DOWN**, **AndroidKey.DPAD_LEFT**, **AndroidKey.DPAD_RIGHT**,
**AndroidKey.DPAD_CENTER** and other. For more info see [TV UI events](https://developer.android.com/training/tv/start/controllers#tv-ui-events).

In the implementation of tests, the idea is the same as in [mobile-testing](https://zebrunner.github.io/carina/automation/mobile/).
