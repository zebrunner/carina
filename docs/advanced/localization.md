# Localization usage

There is an enhanced L10N utility to support localized (L10N)  test automation testing. It can load localized resources on the fly and verify texts if needed.
> We recommend to use existing localized resources and don't generate them by automation team!

## Prerequisites

Localized resources are located in [**src/main/resources/L10N**](https://github.com/qaprosoft/carina-demo/tree/master/src/main/resources/L10N) folder.
Each localized resource file has unique keys with translated values.
> Verify that there is a file without any postfix like [locale.properties](https://github.com/qaprosoft/carina-demo/blob/master/src/main/resources/L10N/locale.properties). This file will be considered as a default localization.

## Implementation

Define parameters in [_config.properties](https://github.com/qaprosoft/carina-demo/blob/master/src/main/resources/_config.properties).
```
#Localization language
locale=de_DE

#Optionally you could operate by browser locale as well using
browser_language=en_US

#Enables auto verification for elements, that are marked with @Localized (by default it's false)
locale_auto_verification=false

#Encoding for a new localization (by default utf-8)
locale_encoding=UTF-8
```

Then declare page elements with `@Localized` annotation. For example:
```
@Localized
@FindBy(id = "pt-createaccount")
private ExtendedWebElement createAccountElem;

@Localized
@FindBy(id = "pt-anoncontribs")
private ExtendedWebElement contribElem;

@Localized
@FindBy(xpath = "//nav[@id='p-navigation']/descendant::ul[@class='vector-menu-content-list']/*")
private List<ExtendedWebElement> pageLinks;
```

If locale auto verification is on, on first operation with element carina will compare text from page with text from your locale_xx_XX.properties file.
__The key from locale file should have the same name as the name of variable that needs to be compared__. Resources example for elements, described higher:
```
createAccountElem=fiók létrehozása
contribElem=közreműködések
pageLinks0=Kezdőlap
pageLinks1=Tartalom
pageLinks2=Kiemelt szócikkek
pageLinks3=Friss változtatások
pageLinks4=Lap találomra
pageLinks5=Tudakozó
```
Carina will collect every text mismatch until L10N.assertAll() method is called. If at the end of the suite there still left some unassorted inconsistencies, they will be showed at the end of the logs.

If you want to do it manually use [`L10N.getText(key)`](https://github.com/qaprosoft/carina-demo/blob/64b63927e8c3a1a76d5e567e28f837be82797d56/src/test/java/com/qaprosoft/carina/demo/WebLocalizationSample.java#L53)
to get expected translations for assertions:
```
String welcomeText = wikipediaLocalePage.getWelcomeText();
String expectedWelcomeText = L10N.getText("HomePage.welcomeText");
Assert.assertEquals(welcomeText, expectedWelcomeText.trim(), "Wikipedia welcome text was not the expected.");
```
## Resources generation
To generate resources by carina, you need to enable locale auto verification parameter. Then operate with every element, that should have localization.
In the end call **L10N.saveLocalization()**. In [src/main/resources/L10N](https://github.com/qaprosoft/carina-demo/tree/master/src/main/resources/L10N)
folder will be generated new file with resources, that were collected while performing verification.
Example:
```
public void testAddNewLanguages() {
    WikipediaHomePage wikipediaHomePage = new WikipediaHomePage(getDriver());
    wikipediaHomePage.open();

    WikipediaLocalePage wikipediaLocalePage = wikipediaHomePage.goToWikipediaLocalePage(getDriver());

    wikipediaLocalePage.hoverWelcomeText();
    wikipediaLocalePage.hoverContribElem();
    wikipediaLocalePage.hoverCreateAccountElem();

    wikipediaLocalePage.hoverHeaders();

    wikipediaLocalePage.clickDiscussionBtn();

    L10N.saveLocalization();
    L10N.assertAll(); // not necessary for resources generation
}
```
## Elements searching with locales

Declare elemetns with a help of [L10N](https://github.com/qaprosoft/carina-demo/blob/64b63927e8c3a1a76d5e567e28f837be82797d56/src/main/java/com/qaprosoft/carina/demo/gui/pages/localizationSample/WikipediaLocalePage.java#L26) prefix.
Use key after the ":" sign in @FindBy annotations which will be replaced by actual localized translations.

```
@FindBy(xpath = "//*[text()='{L10N:HomePage.welcomeText}'")
private ExtendedWebElement welcomeText;

@FindBy(linkText = "{L10N:discussionElem}")
private ExtendedWebElement discussionBtn;
```
At runtime actual translations will be used to locate elements
```
xpath = "//*[text()='{L10N:HomePage.welcomeText}'"
# actual value at run-time:
xpath = "//*[text()='Willkommen bei Wikipedia'"
```
