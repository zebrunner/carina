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
		<th>Description</th>
	</tr>
	<tr>
		<td><a href="https://github.com/zebrunner/carina-api">Carina API</a></td>
		<td>For API testing, based on RestAssured library. <b>Optional</b></td>
	</tr>
    <tr>
		<td><a href="https://github.com/zebrunner/carina-aws-s3">Carina AWS S3</a></td>
		<td>A set of utilities for working with Amazon S3. <b>Optional</b></td>
	</tr>
    <tr>
		<td><a href="https://github.com/zebrunner/carina-azure">Carina Azure</a></td>
		<td>A set of utilities for working with Azure. <b>Optional</b></td>
	</tr>
    <tr>
		<td><a href="https://github.com/zebrunner/carina-appcenter">Carina AppCenter</a></td>
		<td>A set of utilities for working with AppCenter. <b>Optional</b></td>
	</tr>
    <tr>
		<td><a href="https://github.com/zebrunner/carina-dataprovider">Carina DataProvider</a></td>
		<td>Provides the ability to use xls/csv as data sources. <b>Optional</b></td>
	</tr>
    <tr>
		<td><a href="https://github.com/zebrunner/carina-webdriver">Carina WebDriver</a></td>
		<td>Contains logic for creating sessions. <b>Part of Carina Core</b></td>
	</tr>
    <tr>
		<td><a href="https://github.com/zebrunner/carina-utils">Carina Utils</a></td>
		<td>Provides a set of tools for all components of Carina Framework. <b>Part of Carina Core</b></td>
	</tr>
    <tr>
		<td><a href="https://github.com/zebrunner/carina-commons">Carina Commons</a></td>
		<td>Contains interfaces for pluggable dependencies (e.g. carina-azure). <b>Part of Carina Core</b></td>
	</tr>
    <tr>
		<td><a href="https://github.com/zebrunner/carina-crypto">Carina Crypto</a></td>
        <td>Contains utilities for encryption/decryption. <b>Part of Carina Core</b></td>
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
