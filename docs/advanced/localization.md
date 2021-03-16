# Localization usage

There is enhanced L10N utility to support Test Automation localization (L10N) testing. It might load localized resources on the fly and translate texts when needed.
> We recommend to reuse existing localized resources and don't generate them by automation team!

## Prerequisites

Localized resources are located in [**src/main/resources/L10N**](https://github.com/qaprosoft/carina-demo/tree/master/src/main/resources/L10N) folder.
Each localized resource file has unique keys with translated values.
> Verify that for every resource there is a file without any postfix for example [locale.properties](https://github.com/qaprosoft/carina-demo/blob/master/src/main/resources/L10N/locale.properties)

## Implementation

Declare page elements with L10N [prefix](https://github.com/qaprosoft/carina-demo/blob/64b63927e8c3a1a76d5e567e28f837be82797d56/src/main/java/com/qaprosoft/carina/demo/gui/pages/localizationSample/WikipediaLocalePage.java#L26).
Use key after the ":" sign in @FindBy annotations which will be replaced by actual localized translations.

```
@FindBy(xpath = "//*[text()='{L10N:HomePage.welcomeText}'")
private ExtendedWebElement welcomeText;

@FindBy(linkText = "{L10N:discussionElem}")
private ExtendedWebElement discussionBtn;
```

Use [`L10N.getText(key)`](https://github.com/qaprosoft/carina-demo/blob/64b63927e8c3a1a76d5e567e28f837be82797d56/src/test/java/com/qaprosoft/carina/demo/WebLocalizationSample.java#L53) to get expected translations for assertions:

```
String welcomeText = wikipediaLocalePage.getWelcomeText();
String expectedWelcomeText = L10N.getText("HomePage.welcomeText");
Assert.assertEquals(welcomeText, expectedWelcomeText.trim(), "Wikipedia welcome text was not the expected.");
```

## Execution

Update `locale` parameter in configuration and launch test.
```
locale=de_DE
#Optionally you could operate by browser locale as well using
browser_language=de_DE
```

At runtime actual translations will be used to locate elements
```
xpath = "//*[text()='{L10N:HomePage.welcomeText}'"
# actual value at run-time:
xpath = "//*[text()='Willkommen bei Wikipedia'"
```
