[![Carina - Configuration](https://raw.githubusercontent.com/qaprosoft/carina/master/docs/img/video.png)](https://youtu.be/MMviWxCS9x4)

### Configuration files
There are multiple properties files located in src/main/resources:

*  **api.properties** - API test endpoints reference
*  **config.properties** - global test configuration
*  **database.properties** - database connection properties
*  **email.properties** - emailable reports config
*  **testdata.properties** - test user credentials 

All properties may be retrieved in test using R class:
```
R.API.get("GetUserMethods")
R.CONFIG.get("browser")
R.DATABASE.get("db.url")
R.EMAIL.get("title")
R.TESTDATA.get("user.email")
```
Default config properties can be obtained by
```
Configuration.get(Parameter.EXTRA_CAPABILITIES)
```

All project configuration properties are located in **_config.properties** file. In the table below we are providing description for most of the parameters:
<table>
	<tr>
		<th>Attribute</th>
		<th>Meaning</th>
		<th>Example</th>
	</tr>
	<tr>
		<td>url</td>
		<td>Base application URL</td>
		<td>http://qaprosoft.com</td>
	</tr>
	<tr>
		<td>browser</td>
		<td>Browser for testing</td>
		<td>chrome / firefox / safari / iexplore</td>
	</tr>
	<tr>
		<td>selenium_host</td>
		<td>Selenium server host</td>
		<td>http://localhost:4444/wd/hub</td>
	</tr>
	<tr>
		<td>app_version</td>
		<td>Application version/build number for reporting</td>
		<td>1.2.5</td>
	</tr>
	<tr>
		<td>locale</td>
		<td>Locale for using L10N feature. Enabled when enable_l10n=true</td>
		<td>en_GB,de_DE,fr_FR</td>
	</tr>
	<tr>
		<td>language</td>
		<td>Language for i18n defature. Enabled when enable_i18n=true</td>
		<td>en_GB,de_DE,fr_FR</td>
	</tr>
	<tr>
		<td>retry_interval</td>
		<td>Timeout interval between calling HTML DOM for the element.<br><b>Note:</b> in ms. For mobile automation specify number from 500-1500 range</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>auto_screenshot</td>
		<td>Global switch for taking screenshots. When disabled only failures will be captured</td>
		<td>Boolean</td>
	</tr>
	<tr>
		<td>report_url</td>
		<td>Direct HTTP link to Jenkins workspace report folder. Automatically specified by CI</td>
		<td>http://localhost:8888/job /my_project/1/eTAF_Report</td>
	</tr>
	<tr>
		<td>max_screen_history</td>
		<td>Max number of reports in history</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>jira_url</td>
		<td>JIRA base URL for direct links with bugs description</td>
		<td>https://jira.carina.com/browse/</td>
	</tr>
	<tr>
		<td>email_list</td>
		<td>Comma-separated list of emails for reports</td>
		<td>u1@gmail.com,u2@gmail.com</td>
	</tr>
	<tr>
		<td>sender_email</td>
		<td>Email account for reports sending. <br><b>Note:</b> Gmail smtp settings are used by default. Update _email.properties to use your own SMTP server</td>
		<td>carina.qareport@qaprosoft.com</td>
	</tr>
	<tr>
		<td>sender_pswd</td>
		<td>Email password for reports sending</td>
		<td>pwd123</td>
	</tr>
	<tr>
		<td>env</td>
		<td>Environment specific configuration. More about this [feature](#environment-specific-configuration)</td>
		<td>STAG, PROD, DEMO</td>
	</tr>
	<tr>
		<td>env_arg_resolver</td>
		<td>This parametr is optional, if it isn't set default value will be used. In most cases <b>default value is enough</b></td>
		<td>java class </td>
	</tr>
		<tr>
		<td>platform</td>
		<td>Platform version for Selenium Grid</td>
		<td>ANDROID,IOS,WINDOWS,MAC,LINUX</td>
	</tr>
		<tr>
		<td>browser_version</td>
		<td>The browser version, or the empty string if unknown for Selenium Grid</td>
		<td>"8.0", "52.1"</td>
	</tr>
		<tr>
		<td>driver_mode</td>
		<td>Rule for defining WebDriver lifecycle.</td>
		<td>method_mode / class_mode / suite_mode</td>
	</tr>
	<tr>
		<td>driver_event_listeners</td>
		<td>Comma-separated list of listeners. Listeners provide more logs from WebDriver and have to be instances of WebDriverEventListener</td>
		<td>com.someCompane.core .EventListener</td>
	</tr>
		<tr>
		<td>max_driver_count</td>
		<td>Max number of drivers per thread</td>
		<td>Integer</td>
	</tr>
		<tr>
		<td>custom_capabilities</td>
		<td>Name of propertie file with custom capabilities(key-value)</td>
		<td>custom.properties</td>
	</tr>
		<tr>
		<td>proxy_host</td>
		<td>The hostname of the server</td>
		<td>host.example.com</td>
	</tr>
		<tr>
		<td>proxy_port</td>
		<td>The port number</td>
		<td>80</td>
	</tr>
		<tr>
		<td>proxy_protocols</td>
		<td>Comma-separated list of internet protocols used to carry connection information from the source requesting the connection to the destination for which the connection was requested.</td>
		<td>http, https, ftp, socks</td>
	</tr>
		<tr>
		<td>browsermob_proxy</td>
		<td>Boolean parameter which enable or disable set up of mobile proxy</td>
		<td>true, false</td>
	</tr>
		<tr>
		<td>browsermob_port</td>
		<td>The port number for mobile browser (make sense only for local debugging)</td>
		<td>Integer</td>
	</tr>
		<tr>
		<td>proxy_set_to_system</td>
		<td>Boolean parameter which enable or disable set up of proxy</td>
		<td>true, false</td>
	</tr>
		<tr>
		<td>failure_email_list</td>
		<td>Comma-separated list of emails for failure reports</td>
		<td>u1@mail.com,u2@mail.com</td>
	</tr>
		<tr>
		<td>track_known_issues</td>
		<td>Boolean parameter. If it is true and some Jira tickets assosiated with test in case of failure Jira info will be added to report</td>
		<td>true,false</td>
	</tr>
	<tr>
		<td>explicit_timeout</td>
		<td>Timeout in seconds to wait for a certain condition to occur before proceeding further in the code</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>auto_download</td>
		<td>Enabled parametr prevent download dialog and download file automatically. Feature currently avaliable for Chrome and FireFox</td>
		<td>false, true</td>
	</tr>
	<tr>
		<td>auto_download_apps</td>
		<td>MIME types / Internet Media Types. Parameter is necessary only for configure auto downloading for FireFox</td>
		<td>application/pdf, list of [values](https://freeformatter.com/mime-types-list.html)</td>
	</tr>
	<tr>
		<td>project_report_directory</td>
		<td>Path to folder where reports will be saved</td>
		<td>./reports/qa</td>
	</tr>
	<tr>
		<td>big_screen_width</td>
		<td>Sreenshots will be resized according this width if there own width is bigger</td>
		<td>500, 1200, Integer</td>
	</tr>
	<tr>
		<td>big_screen_height</td>
		<td>Sreenshots will be resized according this height if there own height is bigger</td>
		<td>500, 1200, Integer</td>
	</tr>
		<tr>
		<td>small_screen_width</td>
		<td>Thumbnails width</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>big_screen_height</td>
		<td>Thumbnails height</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>big_screen_height</td>
		<td>Thumbnails height</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>init_retry_count</td>
		<td>Number of attempts to create driver.  Default value 0 means that it would be only 1 attempt</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>init_retry_interval</td>
		<td>Interval is seconds between attempts to create driver</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>retry_count</td>
		<td>Number of test-retrying in case of failure.  Default value 0 means that test would be performed only once</td>
		<td>Integer</td>
	</tr>
		<tr>
		<td>enable_l10n</td>
		<td>Enable L10N feature</td>
		<td>false, true</td>
	</tr>
			<tr>
		<td>l10n_encoding</td>
		<td>Charset for l10n feature</td>
		<td>ISO-8859-5, ISO-8859-6, UTF-8</td>
	</tr>
	<tr>
		<td>enable_i18n</td>
		<td>Enable i18n feature</td>
		<td>false, true</td>
	</tr>
		<tr>
		<td>thread_count</td>
		<td>Default number of threads to use when running tests in parallel.</td>
		<td>fInteger</td>
	</tr>
		<tr>
		<td>data_provider_thread_count</td>
		<td>Default number of threads to use for data providers when running tests in parallel.</td>
		<td>Integer</td>
	</tr>
		<tr>
		<td>core_log_level</td>
		<td>Level for Carina logging</td>
		<td>ALL, DEBUG, ERROR, WARN, FATAL, INFO, OFF, TRACE </td>
	</tr>
		<tr>
		<td>log_all_json</td>
		<td>API response will be logged in JSON format</td>
		<td>true, false</td>
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
		<td>crypto_key_path</td>
		<td>Path to file with crypto key</td>
		<td>./src/main/resources/crypto.key</td>
	</tr>
		<tr>
		<td>suite_name</td>
		<td>Suite name for report and TestRail. If this parameter is NULL will be get from TestNG xml (parameter suite name) or _email.properties (title)</td>
		<td>Advanced Acceptance</td>
	</tr>
	<tr>
		<td>jira_updater</td>
		<td>Class contains logic to update Jira. <b>Note</b> Custom updater have to implement DefaultJiraUpdater, by default methods do nothing</td>
		<td>Custom class</td>
	</tr>
	<tr>
		<td>jira_url</td>
		<td>Url to Jira</td>
		<td>https://yourclass.atlassian.net</td>
	</tr>
		<tr>
		<td>jira_user</td>
		<td>Jira user email</td>
		<td>admin@yourcompany.com</td>
	</tr>
		<tr>
		<td>jira_password</td>
		<td>Jira user password</td>
		<td>admin123456</td>
	</tr>
		<tr>
		<td>jira_suite_id</td>
		<td>Jira suit id (if you have one)</td>
		<td>Integer</td>
	</tr>
	<tr>
		<td>jira_project</td>
		<td>Jira project id</td>
		<td>Integer</td>
	</tr>
		<tr>
		<td>jira_create_new_ticket</td>
		<td>If feature is enabled after test failure new jira ticket will be created</td>
		<td>true, false</td>
	</tr>
	<tr>
		<td>optimize_video_recording</td>
		<td>Enable video recording only for the final retry attempt</td>
		<td>true, false</td>
	</tr>
	<tr>
		<td>testrail_url</td>
		<td>Url to TestRail</td>
		<td>https:/yourcompany.testrail.net</td>
	</tr>
	<tr>
		<td>testrail_user</td>
		<td>TestRail user email</td>
		<td>admin@yourcompany.com</td>
	</tr>
		<tr>
		<td>testrail_updater</td>
		<td>Class contains logic to update TestRail. <b>Note</b> Custom updater have to implement EmptyTestRailUpdater, by default methods do nothing</td>
		<td>Custom class</td>
	</tr>
		<tr>
		<td>testrail_milestone</td>
		<td>Milestone to set on TestRail for run</td>
		<td>some-milestone</td>
	</tr>
		<tr>
		<td>testrail_assignee</td>
		<td>User asserneed for the suit</td>
		<td>asignee_user@yuorcompany.com</td>
	</tr>
		<tr>
		<td>s3_bucket_name</td>
		<td>Bucket name on S3 Amazon from which you suppose to download artifacts</td>
		<td>some bucket</td>
	</tr>
		<tr>
		<td>access_key_id</td>
		<td>Acces key id for Amamzon S3. More info [here](#https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys)</td>
		<td>gkhcvdgvceUYF67897hbjsbdc</td>
	</tr>
		<tr>
		<td>secret_key</td>
		<td>Secret key for Amamzon S3. More info [here](#https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys)</td>
		<td>gkhcvdgvceUYF67897hbjsbdc</td>
	</tr>
		<tr>
		<td>s3_local_storage</td>
		<td>Directory for downloading artefacts</td>
		<td>./s3</td>
	</tr>
		<tr>
		<td>s3_save_screenshots</td>
		<td>If enabled screenshots would be saved on S3 Amamzon</td>
		<td>false, true</td>
	</tr>
		<tr>
		<td>hockeyapp_token</td>
		<td>Token for authentication in Hockey App</td>
		<td>gkhcvdgvceUYF67897hbjsbdc</td>
	</tr>
		<tr>
		<td>hockeyapp_local_storage</td>
		<td>Directory for Hockey App artefacts</td>
		<td>./hockeyapp</td>
	</tr>
		<tr>
		<td>add_new_localization</td>
		<td>Should be set to 'true' if you want to create new localization files for required Locale.Otherwise there will be just localization checking</td>
		<td>false, true</td>
	</tr>
		<tr>
		<td>add_new_localization_encoding</td>
		<td>Encoding for new lokalization</td>
		<td>utf-16, utf-32</td>
	</tr>
		<tr>
		<td>add_new_localization_path</td>
		<td>Path where created localization properties should be saved. If null - they will be added to artifactory folder in report</td>
		<td>utf-16, utf-32</td>
	</tr>
		<tr>
		<td>add_new_localization_property_name</td>
		<td>Path where created localization properties should be saved. If null - they will be added to artifactory folder in report</td>
		<td>Basic template for property name.</td>
	</tr>
		<tr>
		<td>cucumber_tests</td>
		<td>If parametrs contains true Cucumber tests will be started</td>
		<td>true, false</td>
	</tr>
		<tr>
		<td>cucumber_tests_app_version</td>
		<td>Version of app using in Cucumber tests</td>
		<td>2.013</td>
	</tr>
		<tr>
		<td>cucumber_tests_name</td>
		<td>Cucucmber tests name</td>
		<td>cucumber tests</td>
	</tr>
		<tr>
		<td>cucumber_tests_results_image_resize</td>
		<td>Percent of Scaling from default image. 100 - same size</td>
		<td>Integer</td>
	</tr>
		<tr>
		<td>cucumber_report_subfolder</td>
		<td>Subfolder on Jenkins (jenkins_url/{someNumbersFr EveryRun}/ artifacts/CucumberReport/ {cucumber_report_subfolder})</td>
		<td>cucumber-reports</td>
	</tr>
		<tr>
		<td>cucumber_user_js_in_report</td>
		<td>Enabled parameter help to get more beautiful reports</td>
		<td>true, false</td>
	</tr>
		<tr>
		<td>tls_keysecure_location</td>
		<td>Path to directory with tls secure keys</td>
		<td>./tls/keysecure</td>
	</tr>
		<tr>
		<td>health_check_class</td>
		<td>Class to execute helth checks</td>
		<td>Custom class</td>
	</tr>
		<tr>
		<td>health_check_methods</td>
		<td>Comma-separate list of methods of health_check_class to execute preliminary</td>
		<td>doThis, doThat</td>
	</tr>
</table>
Most of the properties may be read in the following way:
```
Configuration.get(Parameter.URL) // returns string value
Configuration.getBoolean(Parameter.AUTO_SCREENSHOT) // returns boolean value
Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH) // returns integer value
Configuration.getDouble(Parameter.BROWSER_VERSION) // returns double value
```

### Environment specific configuration
In some cases it is required to support multiple environments for testing. Let's assume we have STAG and PROD environments which have different application URLs. In this case we need to specify the following properties in _config.properties:
```
env=PROD
STAG.url=http://stag-app-server.com
PROD.url=http://prod-app-server.com
```

And get env-specific argument in test the following way:
```
Configuration.getEnvArg("url")
```
As a result you switch between environments just changing env argument in _config.properties file.

### [Zafira](https://github.com/qaprosoft/zafira) configuration
[**zafira.properties**](https://github.com/qaprosoft/carina-demo/blob/master/src/main/resources/zafira.properties) is used for Zafira QA reporting integration, here you should specify some values for proper integration:<table>
	<tr>
		<th>Attribute</th>
		<th>Meaning</th>
		<th>Example</th>
	</tr>
	<tr>
		<td>zafira_enabled</td>
		<td>Root switch</td>
		<td>true/false</td>
	</tr>
	<tr>
		<td>zafira_service_url</td>
		<td>Webservice URL</td>
		<td>http://localhost:8080/zafira-ws</td>
	</tr>
	<tr>
		<td>zafira_project</td>
		<td>Project name (created in Zafira)</td>
		<td>empty or any created</td>
	</tr>
	<tr>
		<td>zafira_rerun_failures</td>
		<td>Rerun only failures</td>
		<td>true/false</td>
	</tr>
	<tr>
		<td>zafira_report_emails</td>
		<td>List of emails for report</td>
		<td>user1@qps.com,user2@qps.com</td>
	</tr>
	<tr>
		<td>zafira_configurator</td>
		<td>Configurator class (use default)</td>
		<td>com.qaprosoft.carina.core.foundation.report.ZafiraConfigurator</td>
	</tr>	
</table>
