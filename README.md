![Alt text](https://github.com/zebrunner/carina/raw/master/docs/img/carina.png "Carina Logo")

Carina is a Java-based test automation framework that unites all testing layers: Mobile applications (web, native, hybrid), WEB applications, Windows applications, REST services, Databases.

<B>[TRY DEMO PROJECT NOW](https://github.com/zebrunner/carina-demo)</B>

* Carina framework is built on top of most popular open-source solutions like Selenium, Appium, TestNG allowing to reduce dependence on specific technology stack.

* Carina supports all popular browsers (IE, Safari, Chrome, Firefox) and mobile devices (iOS/Android). Special feature for mobile domain: it reuses test automation code between IOS/Android up to 70-80%.

* As far as our framework is built in Java, it is cross-platform. Tests may be easily executed both on Unix or Windows OS. All you need is JDK 11 installed.

* Framework supports different types of databases - both relational and nonrelational (MySQL, SQL Server, Oracle, PostgreSQL), providing amazing experience of DAO layer implementation using MyBatis ORM framework.

* API testing is based on Freemarker template engine. It enables great flexibility in generating REST requests and responses are dynamically changed by incoming arguments. 

![Alt text](https://github.com/zebrunner/carina/raw/master/docs/img/carina_overview.png "Carina Overview")


The Carina Framework ecosystem consists of the following modules:
<table>
	<tr>
		<th>Project Name</th>
		<th>Optional</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>[Carina API]()</td>
		<td>true</td>
		<td>For API testing, based on RestAssured</td>
	</tr>
    <tr>
		<td>[Carina AWS S3]()</td>
		<td>true</td>
		<td>Designed to work with Amazon S3. Add it if you need access to 
utilities to interact with S3 or to generate a link to a mobile application on the fly.</td>
	</tr>
    <tr>
		<td>[Carina Azure]()</td>
		<td>true</td>
		<td></td>
	</tr>
    <tr>
		<td>[Carina AppCenter]()</td>
		<td>true</td>
		<td></td>
	</tr>
    <tr>
		<td>[Carina DataProvider]()</td>
		<td>true</td>
		<td></td>
	</tr>
    <tr>
		<td>[Carina WebDriver]()</td>
		<td>false</td>
		<td></td>
	</tr>
    <tr>
		<td>[Carina Utils]()</td>
		<td>false</td>
		<td></td>
	</tr>
    <tr>
		<td>[Carina Commons]()</td>
		<td>false</td>
		<td></td>
	</tr>
    <tr>
		<td>[Carina Proxy]()</td>
		<td>false</td>
		<td></td>
	</tr>
    <tr>
		<td>[Carina Crypto]()</td>
		<td>false</td>
		<td></td>
	</tr>
</table>

## Sponsor
<p align="center">
  <a href="https://zebrunner.com/"><img alt="Zebrunner" src="https://github.com/zebrunner/zebrunner/raw/master/docs/img/zebrunner_intro.png"></a>
</p>

## Documentation and free support
* [User Guide](http://zebrunner.github.io/carina)
* [Demo Project](https://github.com/zebrunner/carina-demo)
* [Telegram Channel](https://t.me/qps_carina)

## Code formatter
We offer to use our configured [**Java code formatter for Eclipse**](https://github.com/zebrunner/carina/blob/master/carina_formatter.xml). To use the same formatter in IntelliJ IDEA you should install and configure [**Eclipse Code Formatter**](https://plugins.jetbrains.com/plugin/6546-eclipse-code-formatter).

## License
Code - [Apache Software License v2.0](http://www.apache.org/licenses/LICENSE-2.0)

Documentation and Site - [Creative Commons Attribution 4.0 International License](http://creativecommons.org/licenses/by/4.0/deed.en_US)
