Before running any web or mobile test you have to start Selenium/Appium locally or use a remote Selenium Grid. 
We recommend to use scalable [Zebrunner Engine](https://zebrunner.com/) grid.

### Running tests

There are several options to execute a test: you may run test suite from Eclipse IDE or initiate test execution from the console using Maven Surefire plugin built into Carina framework.

To run test suite from Eclipse IDE, just select the required TestNG xml file: Right click > Run As > TestNG suite

![Execution from Eclipse IDE](img/006-Configuration-and-execution.png)

To run the same test suite from the console, navigate to the test project root (where pom.xml is located) and execute the following command:

```
mvn clean -Dsuite=api test
```

> Overriden configuration parameters migth be provided as Java arguments:
```
mvn clean -Denv=STAG -Dbrowser=firefox -Dsuite=web test
```
