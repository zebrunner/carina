Carina Automation Framework
==================
Carina is Java-based test automation framework that unites all testing layers: Mobile applications (web, native, hybrid), WEB appications, REST services, Databases.

* Carina framework is built on the top of most popular open-source solutions like Selenium, Appium, TestNG which reduces dependability on specific technology stack.

* Carina covers all popular browsers (IE, Safari, Chrome, Firefox) and mobile devices (iOS/Android). Special feature for mobile domain: it reuses test automation code between IOS/Android by 70-80%.

* As far as our framework is build in Java, it is cross-platform. Tests may be easily executed both on Unix or Windows machines all you need is JDK 8 installed.

* Framework supports different types of databases - both relational and nonrelational (MySQL, SQL Server, Oracle, PostgreSQL), providing amazing experience of DAO layer implementation using MyBatis ORM framework.

* API testing is based on Freemarker template engine. It enables great flexibility in generating REST requests and responses dynamically changed by incoming arguments. 

## Сontents
* [Initial setup](#initial-setup)
* [Project structure](Project-structure)
* [Configuration and execution](Configuration-and-execution)
* [Web UI automation](Web-UI-automation)
* [Web services API automation](Web-services-API-automation)
* [Mobile automation](Mobile-automation)
* [Database access setup](DB-access)
* [Data-providers usage](Data-providers)

## Initial setup
* Install and configure JDK 1.8+
* Install and configure [Apache Maven 3.5.2+](http://maven.apache.org/)
* Download and start latest [Selenium standalone server](http://www.seleniumhq.org/download/)
* Download the latest version of [Eclipse](http://www.eclipse.org/downloads/) and install [TestNG plugin](http://testng.org/doc/download.html)

### Generating project
The easiest way to initialize new project is to you Carina archetype, you will get correct project structure along with test samples. 
First clone archetype repo:
```
git clone https://github.com/qaprosoft/carina-archetype.git
```
Navigate to cloned repo and execute:
```
mvn install
```
Now go to folder where you need to generate new project and execute (do not forget to set groupId, artifactId, name, url, version):
```
mvn archetype:generate -DarchetypeGroupId=com.qaprosoft \
                       -DarchetypeArtifactId=carina \
                       -DarchetypeVersion=1.0 \
                       -DgroupId=<your_groupId> \ 
                       -DartifactId=<your_artifactId> \ 
                       -Dname="<you_proj_name>" \
                       -Durl=<your_proj_url> \
                       -Dversion=<your_proj_version>
```
The command should be executed without '\' symbols on one line, leaving single white space between the attributes. If any attribute contains spaces, it should be set in quoted(e.g.: -Dname="Hello World"). In the Maven command listed above, you have to specify 5 attributes while the first 3 should be left unchanged. Let's go through these attributes:
<table> 
	<tr>
		<th>Attribute</th>
		<th>Meaning</th>
		<th>Example</th>
	</tr>
	<tr>
		<td>-DgroupId</td>
		<td>Company domain in reverce order</td>
		<td>com.qaprosoft</td>
	</tr>
	<tr>
		<td>-DartifactId</td>
		<td>Java project name</td>
		<td>carina-qa</td>
	</tr>
	<tr>
		<td>-Dname</td>
		<td>Name with more details</td>
		<td>"Carina Test Automation"</td>
	</tr>
	<tr>
		<td>-Durl</td>
		<td>Company URL</td>
		<td>http://qaprosoft.com</td>
	</tr>
	<tr>
		<td>-Dversion</td>
		<td>Project version</td>
		<td>1.0</td>
	</tr>
</table>

### Import to Eclipse
If generation finishes successfully, you should see a new project folder with a name equal to the artifactId attribute specified during generation, so navigate to that folder (where pom.xml is located) and execute the following Maven task:
```
mvn clean eclipse:eclipse
```
Using this command, Maven should resolve all dependencies, downloading required libraries to your local repository and generating Eclipse classpath. Before importing a new project to Eclipse, you must link your IDE with your Maven repository by executing the following task:
```
mvn -Dworkspace=<path_to_workspace> eclipse:configure-workspace
```
Here you have to specify the absolute path to the Eclipse workspace. After that, restart Eclipse IDE. Now you may import generated projects such as "Existing Java Project" into Eclipse IDE.
Generate eclipse workspace using command:
```
mvn clean eclipse:eclipse
```
Now you are ready to import project into eclipse.

![Eclipse view](https://github.com/qaprosoft/carina-demo/blob/gh-pages/img/001-Initial-setup.png?raw=true)


### Git configuration 
1). **Fork repository** `https://github.com/qaprosoft/carina` to your own user.

2). **Clone your fork to your local machine**:

 `git clone git@github.com:your_fork_url/carina.git`

3). `git remote add origin <your_fork_url>` (can be already added)

4). `git fetch origin`

5). `git remote add upstream git@github.com:qaprosoft/carina.git`

6). `git fetch upstream`

7). `git checkout -b work_local_branch upstream/master`

And then after adding files (`git add` ...) use `git commit` (add description) and then **push**:

    git push origin work_local_branch:work_remote_branch
    
And on [https://github.com/qaprosoft/carina](https://github.com/qaprosoft/carina) you will see possibility to "**Compare & Pull Request**"


License
-----------
Code - [Apache Software License v2.0](http://www.apache.org/licenses/LICENSE-2.0)

Documentation and Site - [Creative Commons Attribution 4.0 International License](http://creativecommons.org/licenses/by/4.0/deed.en_US)
