# Localization usage

## Prerequisites

There are enhanced utilities for adapting a product's translation to a specific country or region in Carina, i.e. we fully support Test Automation localization (L10N).
To reuse this feature effectively you have to discover localized resources and push them into the test project, for [example](https://github.com/qaprosoft/carina-demo/tree/master/src/main/resources/L10N)
> Every localized application obligatory has set of resources. We strongly recommend to find them and don't generate by automation team!

* Collect and put localized resources into the **src->main->resources->L10N** folder/package. 
  Each localized resource file has key value pairs. Where key is unique key and value is valid translation
  ```
  HomePage.welcomeText=Welcome to Wikipedia,
  discussionElem=Discussion
  ```

* Verify that for every resource there is a file without any locale postfix with default translations (en_US), for example [locale.properties](https://github.com/qaprosoft/carina-demo/blob/master/src/main/resources/L10N/locale.properties)
  This file will be loaded into the L10N as a default locale resource.

## Elements declaration

* Implement page(s) using localized elements usage, for [example](https://github.com/qaprosoft/carina-demo/blob/64b63927e8c3a1a76d5e567e28f837be82797d56/src/main/java/com/qaprosoft/carina/demo/gui/pages/localizationSample/WikipediaLocalePage.java#L41)

* Reuse `{L10N:localizedKey}` syntax to declare ExtendedWebElement. That's allow to operate with elements by localized text at runtime based on `locale` parameter
  ```
  @FindBy(xpath = "//*[text()='{L10N:HomePage.welcomeText}'")
  private ExtendedWebElement welcomeText;

  @FindBy(linkText = "{L10N:discussionElem}")
  private ExtendedWebElement discussionBtn;
  ```

  During the test actual translations will be used to locate element.

## Execution

* Make sure to provide valid `locale` parameter in configuration before start.
  ```
  locale=de_DE
  #Optionally you could operate by browser locale as well using
  browser_language=de_DE
  ```