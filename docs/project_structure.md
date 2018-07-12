[![Carina - Project structure](https://raw.githubusercontent.com/qaprosoft/carina/master/docs/img/video.png)](https://youtu.be/I1u07JspECE)

Carina test project is structured as a standard Maven project:
```
carina-demo
|-- pom.xml
|-- src/test/java        
|-- src/test/resources
    |-- api
    |-- testng_suites
    |-- xls
|-- src/main/java
|-- src/main/resources
    |-- l18n
```

* **src/test/java** - contains test classes organized using TestNG annotations

![src/test/java](img/002-Project-structure.png)

* **src/test/resources** - contains TestNG xml files, API templates and XLS data providers

![src/test/resources](img/003-Project-structure.png)

* **src/main/java** - contains page object classes, API domains and additional utilities

![src/main/java](img/004-Project-structure.png)

* **src/main/resources** - contains l18n bundles, configuration properties files and MyBatis profiles if needed

![src/main/resources](img/005-Project-structure.png)
