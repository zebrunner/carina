# Localization usage

There are enhanced utilities for adapting a product's translation to a specific country or region in Carina, i.e. we fully support Test Automation localization (L10N) testing.
To reuse this feature effectively you have to discover localized resources and push them into the test project.
We strongly recommend to find resources and don't generate by automation team!

## Prerequisites

* Localized resources are located in [**src/main/resources/L10N**](https://github.com/qaprosoft/carina-demo/tree/master/src/main/resources/L10N) folder of your test project.
  Each localized resource file has key value pairs. Where key is unique key and value is valid translation:

  ```
  HomePage.welcomeText=Welcome to Wikipedia,
  discussionElem=Discussion
  ```

    > Verify that for every resource there is a file without any postfix otherwise Java can't load them!
      For example [locale.properties](https://github.com/qaprosoft/carina-demo/blob/master/src/main/resources/L10N/locale.properties)

## Implementation

* Develop page(s) using [localized key](https://github.com/qaprosoft/carina-demo/blob/64b63927e8c3a1a76d5e567e28f837be82797d56/src/main/java/com/qaprosoft/carina/demo/gui/pages/localizationSample/WikipediaLocalePage.java#L41) usage.
  > Reuse `{L10N:localizedKey}` syntax to declare ExtendedWebElement.

  ```
  @FindBy(xpath = "//*[text()='{L10N:HomePage.welcomeText}'")
  private ExtendedWebElement welcomeText;

  @FindBy(linkText = "{L10N:discussionElem}")
  private ExtendedWebElement discussionBtn;
  ```

* Use [`L10N.getText(key)`](https://github.com/qaprosoft/carina-demo/blob/64b63927e8c3a1a76d5e567e28f837be82797d56/src/test/java/com/qaprosoft/carina/demo/WebLocalizationSample.java#L53) to get expected translations for assertions:
  ```
  String welcomeText = wikipediaLocalePage.getWelcomeText();
  String expectedWelcomeText = L10N.getText("HomePage.welcomeText");
  Assert.assertEquals(welcomeText, expectedWelcomeText.trim(), "Wikipedia welcome text was not the expected.");
  ```

## Execution

* Make sure to provide valid `locale` parameter in configuration and launch test.
  ```
  locale=de_DE
  #Optionally you could operate by browser locale as well using
  browser_language=de_DE
  ```

* At runtime actual translations will be used to locate elements
  ```
  xpath = "//*[text()='{L10N:HomePage.welcomeText}'"
  ->
  xpath = "//*[text()='Willkommen bei Wikipedia'"
  ```

* Any mismatch in actual and transalated text will be identified automatically. 