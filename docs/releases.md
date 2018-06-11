# Release notes


## 5.2.4.97 (2018-06-02)
**Enhancements**

* Published new article for API automation approach:
http://qaprosoft.github.io/carina/automation/api/

* MobileUtils - migrated tap etc operations onto the TouchOptions etc
Note: if in your code thera direct references onto the TouchActions->tap operations you should update them as well because in 6.0.0 java appium client deprecated methods were removed.
Example: 859d1f0

* Split click operation for separated Web and Mobile actions to minimize negative side-effects

**Fixes**

* minor adjustments in UI operations for invisible elements on browsers

**[DEPENDENCIES UPDATES]**
* io.appium:java-client was updated to official 6.0.0 release
* org.seleniumhq.selenium:selenium-java updated to 3.12.0
