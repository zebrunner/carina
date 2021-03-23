#Windows

To do Windows application automation, you need to:

* Download windows [driver](https://github.com/Microsoft/WinAppDriver/releases)
* Enable developers mode for [Windows](https://docs.microsoft.com/en-us/windows/apps/get-started/enable-your-device-for-development)
* Start `Appium` or `WinAppDriver`
* Set carina's configuration

```
#path to your Appium/WinAppDriver server
selenium_host=http://localhost:4723/wd/hub

#Should be set to specify windows automation session
capabilities.platformName=Windows
capabilities.automationName=Windows

#path to application you want to test	
capabilities.app=C:/Program Files (x86)/Microsoft/Skype for Desktop/Skype.exe
```

That's all. To get access to the driver use getDriver(). Write and run tests as usual. If you want to pass specific parameters to WindowsDriver refer to Appium [documentation](https://github.com/appium/appium-windows-driver#windowsdriver-specific-capabilities).