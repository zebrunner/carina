# Contributing to Carina

There are a lot of different ways to contribute to Carina. See below for everything you can do and the processes to follow for each contribution method.
Your contribution is governed by our [Code of Conduct](CODE_OF_CONDUCT.md).

### Make changes to the Carina code or docs

Please use unified code formattor [Java code formatter for Eclipse](carina_formatter.xml), in IntelliJ IDEA install and configure [Eclipse Code Formatter](https://plugins.jetbrains.com/plugin/6546-eclipse-code-formatter).

Fork the project, make a change, and send a pull request. For every Pull Request automatic snapshot build is generated and [Sonar](https://ci.zebrunner.com/sonarqube/dashboard?id=com.qaprosoft%3Acarina) quality checks performed.
Exact build number can be found among check details:
![Alt text](https://github.com/zebrunner/carina/raw/master/docs/img/pr-checker.png "Pull Request Checker")


Update your project [pom.xml](https://github.com/zebrunner/carina-demo/blob/ea08927c722d5138a003cdb1f04b03363d89aeb7/pom.xml#L16), make sure to have [snapshot](https://github.com/zebrunner/carina-demo/blob/d23dd865567e8bafbdd3c925fa89374ae712b6bd/pom.xml#L26) repository enabled and test your changes. Add test results/comments into the Pull Request if possible.

After review and merge we generate consolidated release candidate build increasing build number, for example after `7.2.14` release all merges vome into the `7.2.15-SNAPSHOT`. We strongly recommend to do one more testing round using this build number.

Release candidate build number also can be found on latest SHA1 commit details:
![Alt text](https://github.com/zebrunner/carina/raw/master/docs/img/snapshot-build.png "Release Candidate")


You will probably also want to have a look at this more in-depth [**Carina Overview**](https://zebrunner.github.io/carina/) of the project,
how it is architected, how to use it etc.

### Help out on our community

We can always use help on our forums at
[**Carina Support**](https://t.me/qps_carina)! Hop on over and see if there
are any questions that you can answer.

### Submit bug reports or feature requests

Just use the GitHub issue tracker to submit your bug reports and feature requests. Please follow the [issue](.github/ISSUE_TEMPLATE/bug-report.md) or [feature](.github/ISSUE_TEMPLATE/feature_request.md) templates.
