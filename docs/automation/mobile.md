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
		<td>capabilities.os_versione</td>
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
		<td>Skip the device unlock process. Defaults to false</td>
                <td>n/a</td>
		<td>true, false</td>
	</tr>
</table>

### For Android:
```
#=============== Android Mobile ======================#
capabilities.deviceName=Samsung_Galaxy_J5
capabilities.app=s3://qaprosoft.com/android/myapk.*-release.apk
capabilities.skipUnlock=true
capabilities.noSign=true
capabilities.automationName=uiautomator2
capabilities.newCommandTimeout=180
capabilities.platformName=ANDROID
capabilities.autoGrantPermissions=true
#=====================================================#
```

### For iOS:
```
#=================== iOS Mobile ======================#
capabilities.autoAcceptAlerts=true
capabilities.app=/opt/apk/my-apk.app
capabilities.automationName=XCUITest
capabilities.newCommandTimeout=180
capabilities.platformName=IOS
#=====================================================#
```
