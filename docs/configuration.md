There are multiple properties files located in src/main/resources:

*  **_api.properties** - API test endpoints reference
*  **_config.properties** - global test configuration
*  **_database.properties** - database connection properties
*  **_email.properties** - emailable reports config
*  **_testdata.properties** - test user credentials 

All the properties may be retrieved in a test using R class:
```
R.API.get("GetUserMethods")
R.CONFIG.get("browser")
R.DATABASE.get("db.url")
R.EMAIL.get("title")
R.TESTDATA.get("user.email")
```
The default config properties can be obtained by
```
Configuration.get(Parameter.BROWSER)
```

All the project configuration properties are located in a **_config.properties** file. In the table below, please see the description of most of the parameters:
<table>
	<tr>
		<th>Attribute</th>
		<th>Meaning</th>
		<th>Example</th>
	</tr>
	<tr>
		<td>env</td>
		<td>Environment specific configuration [feature](#environment-specific-configuration)</td>
		<td>STAG, PROD, DEMO</td>
	</tr>
	<tr>
		<td>selenium_url</td>
		<td>Selenium/Appium server URL</td>
		<td>http://localhost:4444/wd/hub</td>
	</tr>
	<tr>
		<td>app_version</td>
		<td>Application version/build number for reporting</td>
		<td>1.2.5</td>
	</tr>
	<tr>
		<td>url</td>
		<td>Base application URL</td>
		<td>https://zebrunner.com</td>
	</tr>
	<tr>
		<td>browser</td>
		<td>Browser for testing</td>
		<td>chrome, firefox, MicrosoftEdge, safari</td>
	</tr>
	<tr>
		<td>headless</td>
		<td>Run tests in headless browser mode. **Default: false**</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>browser_language</td>
		<td>Browser language. **Default: NULL** to use default browser language.</td>
		<td>es, fr</td>
	</tr>
	<tr>
		<td>locale</td>
		<td>Locale for using by [L10N](https://zebrunner.github.io/carina/advanced/localization/) feature</td>
		<td>en_GB, de_DE, fr_FR</td>
	</tr>
	<tr>
		<td>localization_testing</td>
		<td>Enables auto verification for elements that are marked with `@Localized` annotations</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>localization_encoding</td>
		<td>Encoding for generation of new/missed localization resources</td>
		<td>UTF-8</td>
	</tr>
	<tr>
		<td>retry_count</td>
		<td>Number of test-retryings in case of failure. **Default: 0** means that a test will be performed only once</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>thread_count</td>
		<td>Number of threads to use when running tests in parallel. **Default: -1** to use value from TestNG suite xml.</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>data_provider_thread_count</td>
		<td>Number of threads to use for data providers when running tests in parallel. **Default: -1** to use value from TestNG suite xml.</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>core_log_level</td>
		<td>Level for Carina logging. **Default: INFO**</td>
		<td>ALL, DEBUG, ERROR, WARN, FATAL, INFO, OFF, TRACE</td>
	</tr>
	<tr>
		<td>test_run_rules</td>
		<td>Executing rules logic: test_run_rules={RULE_NAME_ENUM}=>{RULE_VALUE1}&&{RULE_VALUE2};;...</td>
		<td>test_run_rules=PRIORITY=>P1&amp;&amp;P2&&P4;;OWNER=>owner;;TAGS=>tag1=temp||!!feature=reg</td>
	</tr>
	<tr>
		<td>test_naming_pattern</td>
		<td>The pattern by which the name of the test method will be formed.</td>
		<td>{tuid} {test_name} - {method_name}</td>
	</tr>
	<tr>
		<td>retry_interval</td>
		<td>Timeout interval in **ms** between calling HTML DOM for the element. **Default: 100**. For mobile automation specify in between 500-1000</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>auto_screenshot</td>
		<td>Global switch for taking screenshots. When disabled, screenshots will be captured only after failures. **Default: true**. </td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>allow_fullsize_screenshot</td>
		<td>Global switch for allowing full size screenshots on failures. **Default: false**</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>max_screen_history</td>
		<td>Max number of reports artifacts saved in history. **Default: 10**</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>driver_event_listeners</td>
		<td>Comma-separated list of extra driver listeners. Listeners provide extra custom actions for WebDriver and have to be the instances of WebDriverEventListener</td>
		<td>com.some_company.core.EventListener</td>
	</tr>
	<tr>
		<td>max_driver_count</td>
		<td>Max number of [drivers](https://zebrunner.github.io/carina/advanced/driver/#initialization) per thread. **Default: 3**</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>init_retry_count</td>
		<td>Number of extra attempts to create a driver. **Default: 0** means that there will be no extra attempts.</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>init_retry_interval</td>
		<td>Interval in seconds between the attempts to create a driver. **Default: 1**</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>forcibly_disable_driver_quit</td>
		<td>If enabled, turns off webdriver quit based on [initizalization phase](https://zebrunner.github.io/carina/advanced/driver/#quit). **Default: false**</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>custom_capabilities</td>
		<td>Path to the properties file with custom key-value [capabilities](https://zebrunner.github.io/carina/advanced/driver/#capabilities)</td>
		<td>browserstack/win/win_10_Edge.properties</td>
	</tr>
	<tr>
		<td>explicit_timeout</td>
		<td>Timeout is seconds to wait for a certain condition to occur before proceeding further in the code</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>read_timeout</td>
		<td>Timeout is seconds to read response from Selenium/Appium. **Default: 600**</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>page_opening_strategy</td>
		<td>Determines how carina detects whether the expected [page](https://zebrunner.github.io/carina/automation/web/#page-opening-strategy) is opened</td>
		<td>BY_ELEMENT, BY_URL, BY_URL_AND_ELEMENT</td>
	</tr>
	<tr>
		<td>page_recursive_reflection</td>
		<td>Determines if pages should be searched in dependencies. **Default: false**</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>element_loading_strategy</td>
		<td>Determines how carina detects appearing of [web elements](https://zebrunner.github.io/carina/automation/web/#element-loading-strategy) on page</td>
		<td>BY_PRESENCE, BY_VISIBILITY, BY_PRESENCE_OR_VISIBILITY</td>
	</tr>
	<tr>
		<td>auto_download</td>
		<td>The enabled parameter prevents downloading dialog and downloading a file automatically into the test artifact folder. The feature is supported for Chrome and Firefox. **Default: false**</td>
		<td>false, true</td>
	</tr>
	<tr>
		<td>custom_artifacts_folder</td>
		<td>Custom unified path for auto-downloaded artifacts for all tests. **Default: NULL** to download into the unique test artifacts location.</td>
		<td>String</td>
	</tr>
	<tr>
		<td>auto_download_apps</td>
		<td>MIME types / Internet Media Types. The parameter is needed only to configure auto-downloading for FireFox. List of [values](https://freeformatter.com/mime-types-list.html)</td>
		<td>application/pdf</td>
	</tr>
	<tr>
		<td>log_all_json</td>
		<td>API response will be logged in JSON format. **Default: true**</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>ignore_ssl</td>
		<td>API requests/responses to ignore SSL errors. **Default: false**</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>project_report_directory</td>
		<td>Path to a folder where the testing report will be saved</td>
		<td>./reports</td>
	</tr>
	<tr>
		<td>proxy_host</td>
		<td>Hostname of the [proxy](https://zebrunner.github.io/carina/advanced/proxy/) server</td>
		<td>host.example.com</td>
	</tr>
		<tr>
		<td>proxy_port</td>
		<td>Port number</td>
		<td>80</td>
	</tr>
	<tr>
		<td>proxy_protocols</td>
		<td>Comma-separated list of internet protocols used to carry the connection information from the source requesting the connection to the destination for which the connection was requested.</td>
		<td>http,https,ftp,socks</td>
	</tr>
	<tr>
		<td>browserup_proxy</td>
		<td>Boolean parameter which enables or disables the automatic BrowserUp proxy launch</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>browserup_port</td>
		<td>Port number for BrowserUp proxy (if nothing or 0 specified, then any free port will be reused)</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>browserup_ports_range</td>
		<td>Range of ports that will be used for starting of browserup proxy. The first available port from the range will be used. If all ports are used, then a test will wait for the first freed port.</td>
		<td>8001:8003</td>
	</tr>
	<tr>
		<td>proxy_set_to_system</td>
		<td>Boolean parameter which enables or disables the setup of a proxy</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>no_proxy</td>
		<td>Excluded hostname(s) for communication via proxy. Available only when proxy_host and proxy_port are declared!</td>
		<td>localhost.example.com</td>
	</tr>
	<tr>
		<td>date_format</td>
		<td>Date format for DateUtils.class</td>
		<td>HH:mm:ss dd/MM/yyyy, HH:mm MM/dd/yyyy</td>
	</tr>
	<tr>
		<td>time_format</td>
		<td>Date format for DateUtils.class</td>
		<td>HH:mm:ss.SSS, HH:mm a zzz</td>
	</tr>
	<tr>
		<td>crypto_key_value</td>
		<td>crypto key</td>
		<td>OIujpEmIVZ0C9kOkXniFRw==</td>
	</tr>
	<tr>
		<td>tls_keysecure_location</td>
		<td>Path to a directory with tls secure keys</td>
		<td>./tls/keysecure</td>
	</tr>
    <tr>
		<td>db.url</td>
		<td>Database url</td>
		<td>jdbc:mysql://localhost/test</td>
	</tr>
    <tr>
		<td>db.username</td>
		<td>Database username</td>
		<td>username</td>
	</tr>
    <tr>
		<td>db.password</td>
		<td>Database password</td>
		<td>password</td>
	</tr>
</table>
Most of the properties may be read in the following way:

```
Configuration.get(Parameter.URL) // returns string value
Configuration.getBoolean(Parameter.AUTO_SCREENSHOT) // returns boolean value
Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) //return int value
Configuration.getDouble(Parameter.MAX_DRIVER_COUNT) // returns double value
```

### Environment specific configuration
In some cases, it is required to support multiple environments for testing. Let's assume we have STAG and PROD environments which have different 
application URLs. In this case, we need to specify the following properties in **_config.properties**:

```
env=PROD
STAG.url=http://stag-app-server.com
PROD.url=http://prod-app-server.com
```

And get an env-specific argument in the test in the following way:

```
Configuration.getEnvArg("url")
Configuration.getEnvArg(Configuration.Parameter.URL)
```

As a result, you switch between the environments just changing the env argument in the **_config.properties** file.

In some cases, it is necessary to store multiple parameter sets for the same env (for example, if multiple databases are used).
For this, the concept of an alias is added. The parameter with env and alias will be stored in the following way:

```
STAG.mongo.db.url=mongodb://stag.example.com:27017
STAG.mysql.db.url=jdbc:mysql://localhost/stag_test
PROD.mongo.db.url=mongodb://prod.example.com:27017
PROD.mysql.db.url=jdbc:mysql://localhost/prod_db
```

Get an env-alias-specific argument in the test in the following way:

```
Configuration.getEnvArg(Configuration.Parameter.DB_URL, "mongo") // mongodb://prod.example.com:27017
Configuration.getEnvArg(Configuration.Parameter.DB_URL, "mysql") // jdbc:mysql://localhost/prod_db
```

### Tests execution filter configuration
The `test_run_rules` parameter is responsible for filtering tests.
There are 3 filter types:<br>
1) **PRIORITY** - enum field (from P0 to P6)<br>
2) **OWNER** - the test owner<br>
3) **TAGS** - custom label<br>

Example of how to attach labels in code:
```
@Test
@TestPriority(Priority.P3)
@MethodOwner(owner = "Josh")
@MethodOwner(owner = "Jake")
@TestTag(name = "feature", value = "web")
@TestTag(name = "type", value = "regression")
public void t4(){
	...
	some code
	...
}
```

`test_run_rules` parameter parse logic:

1) A simple filter:
```
test_run_rules=OWNER=>Josh
#Where OWNER is tag, and "=>" split's tag and rule part.
#Because of the "Josh" rule, test will be executed if it has Josh as owner
```
2) With negative logic:
```
test_run_rules=OWNER=>!!Josh
#Test will be executed if it hasn't got Josh as owner
```
3) With boolean logic:
```
#Use || or && to create more difficult rules
#where || == OR; && == AND.

test_run_rules=OWNER=>Josh||Jake
#Test will be executed if it has at least Josh or Jake as owner.

test_run_rules=OWNER=>Josh&&Jake
#Test will be executed if it has at least Jish and Jake as owner

test_run_rules=OWNER=>Josh&&Jake||Peter
#Expression will be processed in sequential priority, like
#test_run_rules=OWNER=>((Josh&&Jake)||Peter)
#So test will be executed if it has at least (Josh and Jake) or (Peter) as owner
```
4) To add more tags to the rule, use ";;", for example:
```
#;; works as && (AND) but for tags

test_run_rules=PRIORITY=>!!P1;;OWNER=>Josh&&!!Jake;;TAGS=>feature=web&&!!type=smoke||feature=android

#Test will be executed if it has
#1) no @TestPriority(Priority.P1)
#AND
#2) @MethodOwner(owner = "Josh") without @MethodOwner(owner = "Jake")
#AND
#3) (@TestTag(name = "feature", value = "web") without @TestTag(name = "type", value = "smoke"))
	 	or @TestTag(name = "feature", value = "android")	 	

#In other words, tests will be executed only with Priority that differs from P1, with Josh as owner if there is no Jake 
#and if they are not for smoke web or if they are for android.
```

### Changing the pattern of forming the name of the test method
The name of the test method is formed based on the pattern provided in the `test_naming_pattern` parameter. 
The pattern can be formed from the following parts:<br>
1) `{test_name}` - test name  (content of `name` attribute of `<test>` tag in xml)<br>
2) `{tuid}` - TUID, see [doc](https://zebrunner.github.io/carina/advanced/dataprovider/#adding-test-unique-identifier-tuid-to-the-test-name-using-java-data-provider)<br>
3) `{method_name}` - test method name<br>
4) `{method_priority}` - test method priority (number)<br>
5) `{method_thread_pool_size}` - the number of threads to be used when invoking the method on parallel<br>
6) `{group_names}` - the groups this method belongs to, possibly added to the groups declared on the class<br>
7) `{method_description}` - description of test method<br>
8) `{test_class}` - simple name of test class this method belongs to<br>

Default pattern: `test_naming_pattern={tuid} {test_name} - {method_name}`

###FAQ
**Where is a recommended place to declare configuration parameters?**

Declare default parameters in `_config.properties`. For multi-maven projects, you can use extra underscore symbol to override default settings on new layer `__config.properties`, `___config.properties`, etc.

**How to override params from the code?**

Put method might be used to override parameters globally or for a current test only
```
R.CONFIG.put("selenium_url", "http://host1:4444/wd/hub"); //override selenium_url globally for the rest of tests
R.CONFIG.put("selenium_url", "http://host2:4444/wd/hub", true); // override selenium_url for current test only
R.DATABASE.put("db.driver", "org.postgresql.Driver") //override db.driver in_database.properties globally
```

**Crypted values are returned in encrypted format. How can I decrypt them?**

Use `R.CONFIG.getDecrypted(String key)` method to read decrypted value. 

> You should have valid crypto key to be able to decrypt values. For details, visit [Security](https://zebrunner.github.io/carina/advanced/security/)

**Can I override configuration parameters from CI?**

Provide updated values via System Properties to override a value, for example:
```
mvn -Denv=PROD ...
```
