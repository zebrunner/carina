There are a several options to execute the test: you may run test suite from Eclipse IDE or initiate test execution from the console using Maven Surefire plugin built into Carina framework. Before running tests make sure you have downloaded Selenium standalone server jar file and started it by the following command:
```
java -jar selenium-server-standalone-3.6.0.jar
```

To run the test suite from Eclipse IDE, just select the required TestNG xml file: Right click > Run As > TestNG suite

![Execution from Eclipse IDE](img/006-Configuration-and-execution.png)


To run the same test suite from the console, navigate to the test project root (where pom.xml is located) and execute the following command:

```
mvn clean -Dsuite=api test
```
