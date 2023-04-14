There is an enhanced L10N utility to support localized (L10N)  test automation testing. It can load localized resources on the fly and verify texts if needed.
> We recommend to use existing localized resources and don't generate them by automation team!

## Prerequisites

Localized resources should be located in [**src/main/resources/L10N**](https://github.com/zebrunner/carina-demo/tree/master/src/main/resources/L10N) folder.
Each localized resource file has unique keys with translated values.
> Verify that there is a must have file without any postfix like [locale.properties](https://github.com/zebrunner/carina-demo/blob/master/src/main/resources/L10N/locale.properties). This file will be considered as a default localization.

## Implementation

Define parameters in [_config.properties](https://github.com/zebrunner/carina-demo/blob/master/src/main/resources/_config.properties).
```
#Localization language
locale=de_DE

#Optionally you could operate browser locale as well by using
browser_language=en_US

#Enables auto verification for elements, that are marked with @Localized (by default it's false)
localization_testing=true

#Encoding for a new localization (by default UTF-8)
localization_encoding=UTF-8
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
Add to the project resources that corresponds to `@Localized` elements:
```
#!The key from locale file should have Page name + name of variable that needs to be compared.
WikipediaLocalePage.createAccountElem=fiók létrehozása
WikipediaLocalePage.contribElem=közreműködések

#!Elements from List should have the same name of a variable + current element's number
WikipediaLocalePage.pageLinks0=Kezdőlap
WikipediaLocalePage.pageLinks1=Tartalom
WikipediaLocalePage.pageLinks2=Kiemelt szócikkek
WikipediaLocalePage.pageLinks3=Friss változtatások
WikipediaLocalePage.pageLinks4=Lap találomra
WikipediaLocalePage.pageLinks5=Tudakozó
```
For elements that are operated in test and marked with `@Localized` annotation 
Carina will automatically compare text from the page with text from your locale_xx_XX.properties file.
Every mismatch will be collected in `L10N` class. 
>To assert collected assertions use `L10N.assertAll()` method.

If you want to do it manually use [`L10N.getText(key)`](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/src/test/java/com/zebrunner/carina/demo/WebLocalizationSample.java#L99)
to get expected translations from resources:
```
String welcomeText = wikipediaLocalePage.getWelcomeText();
String expectedWelcomeText = L10N.getText("welcomeText");
Assert.assertEquals(welcomeText, expectedWelcomeText.trim(), "Wikipedia welcome text was not the expected.");
```
## Resources generation
To generate resources with Carina, you need to enable `localization_testing` parameter.For elements that are need localization, you need to mark them with `@Localized` and operate with them.
In test call `L10N.flush()` to create new locale file in your project directory.
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

    L10N.flush();
    L10N.assertAll(); // not necessary for resources generation
}
```
## Finding elements with a help of locales

Declare elements with [L10N](https://github.com/zebrunner/carina-demo/blob/318b5235b3d100c9f9419dcb274f1e4c25700cf0/src/main/java/com/zebrunner/carina/demo/gui/pages/desktop/WikipediaLocalePage.java#L57) 
prefix where needed.
Use key after the ":" sign in @FindBy annotations which will be replaced by actual localized translations.

```
@FindBy(xpath = "//*[text()='{L10N:welcomeText}'")
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
## Creation of multi-language tests
If one or a group of tests checks several language versions of the site, then you need to do the following:
1. [Optional] if you need to change the language version of the site, you can overwrite the locale parameter:
```
# overwrites locale value with de_DE for this test only
R.CONFIG.put("locale", "de_DE", true);
```
2. Call the setLocale function and pass the locale to it:
```
# overwrites the default locale in the file to 'de_DE'
L10N.setLocale("de_DE")
```
3. Call the load function to download the localized resource of the current locale:
```
L10N.load();
```
4. When you need to get a list of locale errors, call assertAll:
```
L10N.assertAll();
```