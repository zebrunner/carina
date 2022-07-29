Screenshots generation is organized automatically for selenium tests via [IScreenshotRule.java](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/screenshot/IScreenshotRule.java) implementation(s).
You can implement and register any custom rules based on your requirements.

Default [AutoScreenshotRule.java](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/screenshot/AutoScreenshotRule.java) depends on 2 configuration parameters:
```
auto_screenshot=true
allow_fullsize_screenshot=false
```

Example of the extended screenshot rule can be found [here](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/screenshot/DebugLevelScreenshotRule.java)

When `auto_screenshot` enabled Carina capture screenshots after every important actions: click, type, page opening and of cause after driver and test failures.
Via `allow_fullsize_screenshot` you can enable full size screenshoting for driver and test failures. Make sure to test its enabling in advance on your webapp. 
Huge pages concatenation might significantly slow down your tests execution. 

Default timeout for capturing visible screen is `EXPLICIT_TIMEOUT/3` and for fullsize screenshoting `EXPLICIT_TIMEOUT/2`

To disable completely automatic screenshots capturing clear all rules:
```
Screenshot.clearRules()
```

To capture screenshot explcitly from test code use:
```
Screenshot.capture(getDriver(), "my page screenshot");
Screenshot.capture(getDriver(), "Captureg full size screenshot", true);
```

###FAQ
**How can I find all places where automatic screenshot capturing is performed?**

You can take a look into the [DriverListener](https://github.com/zebrunner/carina/blob/1c9b50202e9254545600488e13f326eaa564e034/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/listener/DriverListener.java#L276) 
and [CarinaListener](https://github.com/zebrunner/carina/blob/1c9b50202e9254545600488e13f326eaa564e034/carina-core/src/main/java/com/qaprosoft/carina/core/foundation/listeners/CarinaListener.java#L958) to find call hierarchy.

