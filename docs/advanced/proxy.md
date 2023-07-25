Selenium provides the ability to set various proxy settings for the browser session.
This possibility is implemented and supplemented in Carina Framework through the usage of various configurations.

First, the type of proxying is controlled by the configuration parameter
`proxy_type`. The value of this parameter can be `ZEBRUNNER`, `DIRECT`, `MANUAL`, `PAC`, `AUTODETECT`, 
`SYSTEM`, `UNSPECIFIED`. Let's consider them in more detail.

### DIRECT proxy mode
Specifies that the session will not use a proxy.

Set by changing the value of the `proxy_type` parameter in the configuration to `DIRECT`.

An example of using a proxy can be viewed [here](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/src/test/java/com/zebrunner/carina/demo/ProxySampleTest.java#L93).

###  PAC proxy mode

A Proxy Auto-Configuration (PAC) file is a JavaScript function that determines whether 
web browser requests (HTTP, HTTPS, and FTP) go directly to the destination or are 
forwarded to a web proxy server. More information about the structure of the pac file can be found [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/Proxy_servers_and_tunneling/Proxy_Auto-Configuration_PAC_file).

Set by changing the value of the `proxy_type` parameter in the configuration to `PAC`.

This mode depends on the following configuration parameters:

`proxy_autoconfig_url` - specifies the URL to be used for proxy auto-configuration.
Expected format is `http://hostname.com:1234/pacfile`. However, the value of this parameter
may be local path with pac file.

`proxy_pac_local` - specifies, how the value provided in the `proxy_autoconfig_url` parameter.
If the parameter value is `true` then it is assumed
that `proxy_autoconfig_url` contains the path to the file on the local machine. It will be sent
browser to set up a proxy. If the value of the parameter is `false`, then it is assumed that
that `proxy_autoconfig_url` contains the URL to the pac file.

An example of using a proxy can be viewed [here](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/src/test/java/com/zebrunner/carina/demo/ProxySampleTest.java#L112).

###  AUTODETECT proxy mode

Specifies whether to autodetect proxy settings. Presumably with [WPAD](https://en.wikipedia.org/wiki/Web_Proxy_Auto-Discovery_Protocol).

Set by changing the value of the `proxy_type` parameter in the configuration to `AUTODETECT`.

An example of using a proxy can be viewed [here](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/src/test/java/com/zebrunner/carina/demo/ProxySampleTest.java#L152).

###  SYSTEM proxy mode

Use system proxy settings. Default Mode on Linux.

Set by changing the value of the `proxy_type` parameter in the configuration to `SYSTEM`.

An example of using a proxy can be viewed [here](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/src/test/java/com/zebrunner/carina/demo/ProxySampleTest.java#L171).

###  UNSPECIFIED proxy mode [NOT RECOMMENDED TO USE]

The mode set in the Selenium proxy by default and indicates that they should
use the following settings (for Windows - DIRECT mode, for linux - SYSTEM).

Set by changing the value of the `proxy_type` parameter in the configuration to `UNSPECIFIED`.

It is not recommended to use, if you want to specify that the proxy object is not added to
session, then use `UNUSED` mode. Only added because Selenium provides
the ability to explicitly specify the `UNSPECIFIED` proxy type.

###  MANUAL proxy mode

Proxy mode, in which the host and port of the proxy are explicitly specified.

Set by changing the value of the `proxy_type` parameter in the configuration to `MANUAL`.

Depends on the following configuration parameters:

`proxy_host` - contains a proxy host, for example `127.0.0.1`  
`proxy_port` - contains a proxy port, for example `8080`  
`proxy_protocols` - contains a list of protocol types to which should be applied
the above options. May contain a set of the following values: `http`, `https`, `ftp`, `socks`.
Values are separated by a comma, such as `http,https,ftp`.  
`no_proxy` - contains comma-separated addresses to which the proxy should not be applied.

If this mode applies to the entire application, you can use the following setting:

`proxy_set_to_system` - if `true`, then sets proxy values to system properties, i.e.
the proxy will be applied to all http/https/ftp requests from the application as well (depends on the values listed in `proxy_protocols`).
However, this approach is not thread-safe and is therefore not recommended for use in all other modes. Disabled by default.

An example of using a proxy can be viewed [here](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/src/test/java/com/zebrunner/carina/demo/ProxySampleTest.java#L69).

### ZEBRUNNER proxy mode

Proxy mode, designed to work with [Zebrunner Selenium Grid](https://zebrunner.com/selenium-grid).

Set by changing the value of the `proxy_type` parameter in the configuration to `ZEBRUNNER`.

By simply enabling this type of proxy, incoming/outgoing browser requests will be proxied.
However, it is also possible to specify additional parameters for the proxy. The class `com.zebrunner.carina.webdriver.proxy.ZebrunnerProxyBuilder`
is used for this.

This builder is used in the test before the driver is created. Usage example:

```java
   @Test
    public void proxyBuilderTest() {
        R.CONFIG.put(WebDriverConfiguration.Parameter.PROXY_TYPE.getKey(), "ZEBRUNNER", true);
        // adding a body modification condition (replace text 'Phone finder' with 'MODIFIED PHONE FINDER')
        ZebrunnerProxyBuilder.getInstance()
                .addBodyModify("Phone finder", "MODIFIED PHONE FINDER")
                .build(true);

        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");
        Assert.assertEquals(homePage.getPhoneFinderButton().getText(), "MODIFIED PHONE FINDER",
                "'Phone Finder' text should be modified in response.");
    }
```
