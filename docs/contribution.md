There are multiple ways to contribute to Carina. See below for everything you can do and the processes to follow for each contribution method.
Your contribution is governed by our [Code of Conduct](https://github.com/zebrunner/carina/blob/master/CODE_OF_CONDUCT.md).

### Make changes to the Carina code or docs

Please use the unified code formatter [Java code formatter for Eclipse](https://github.com/zebrunner/carina/blob/master/carina_formatter.xml); in IntelliJ IDEA, install and configure [Eclipse Code Formatter](https://plugins.jetbrains.com/plugin/6546-eclipse-code-formatter).

Fork the project, make a change, and send a pull request. For every Pull Request, an automatic snapshot build is generated and [Sonar](https://ci.zebrunner.com/sonarqube/dashboard?id=com.zebrunner%3Acarina-core) quality checks are performed.
The exact build number can be found among the check details:
![Alt text](https://github.com/zebrunner/carina/raw/master/docs/img/pr-checker.png "Pull Request Checker")


Update your project [pom.xml](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/pom.xml#L16), make sure to have a [snapshot](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/pom.xml#L28) repository enabled and test your changes. Add test results/comments into the Pull Request if possible.

After reviewing and merging, we generate a consolidated release candidate build increasing the build number, for example, after release `1.0.0` all the merges come into `1.0.0-SNAPSHOT`. We strongly recommend that you do one more testing round using this build number.

The release candidate build number can also be found in the latest SHA1 commit details:
![Alt text](https://github.com/zebrunner/carina/raw/master/docs/img/snapshot-build.png "Release Candidate")


### Help out on our community

We can always use help on our forums at
[**Carina Support**](https://t.me/qps_carina)! Hop on over and see if there
are any questions that you can answer.

### Submit bug reports or feature requests

Just use the GitHub issue tracker to submit your bug reports and feature requests. Please follow the [issue](https://github.com/zebrunner/carina/blob/master/.github/ISSUE_TEMPLATE/bug-report.md) or [feature](https://github.com/zebrunner/carina/blob/master/.github/ISSUE_TEMPLATE/feature_request.md) templates.
