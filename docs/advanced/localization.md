## L10N

Carina framework supports multi-localized testing.
To use this feature we need to:

1. Set properties in _config.properties.
```
locale=de_DE 	         
#localization language

browser_language=en_US   
#Determines browser interface language. It won't affect locale parameter.

enable_l10n=true         
#enable/disable localzation testin
```
2. Add locale file into **src->main->resources->L10N**. Locale file samples could be found [here](https://github.com/qaprosoft/carina-demo/tree/master/src/main/resources/L10N). Ask developers to provide locale files and place them into folder mentioned above. If there are no locale files, check out **L10Nparser** section.
3. find WebElements with a help of a key we described in locale_ file.
   >Syntax is '{L10N:key}'

   Example:
```java
@FindBy(xpath = "//*[@id='{L10N:HomePage.welcomeTextId}'")
private ExtendedWebElement welcomeText;

@FindBy(linkText = "{L10N:discussionElem}")
private ExtendedWebElement discussionBtn;
```
4. Launch tests

##L10Nparser

If there is a need to create locale files by your own Carina can generate them with a help of L10Nparser.class.

1. Set parameters in _config.properties:
```
#true: Carina will create locale file with missing key-value pairs
#false: Carina will check if locale key-value pairs present and print which aren't
add_new_localization=true/false          

#path to new locale File
add_new_localization_path=./src/main/resources/L10N 

add_new_localization_encoding=UTF-8

#prefix to new locale file
add_new_localization_property_name=new_locale_ 
```
2. Find elements by not using locale text, for example by id:
```java
@FindBy(id = "pt-anontalk")
private ExtendedWebElement discussionElem;
 
@FindBy(id = "pt-anoncontribs")
private ExtendedWebElement contribElem;
 
@FindBy(id = "pt-createaccount") 
private ExtendedWebElement createAccountElem;
```
3. Create method in your page.class. Place in _localizationCheckList_ webElements that are currently on the page and which locale pair you want to create.
```java
public boolean checkMultipleLocalization() {
        ExtendedWebElement[] localizationCheckList = {discussionElem, createAccountElem, contribElem};
        
        return L10Nparser.checkMultipleLocalization(localizationCheckList);
}
```
4. Create test as below where checkMultipleLocalization() method is called when according page is opened.
```java
public void testAddNewLanguages() {

    WikipediaHomePage wikipediaHomePage = new WikipediaHomePage(getDriver());
    wikipediaHomePage.open();

    String expectedWelcomeText = L10N.getText("HomePage.welcomeText");
    String welcomeText = wikipediaLocalePage.getWelcomeText();

    SoftAssert sa = new SoftAssert();
    sa.assertEquals(welcomeText, expectedWelcomeText.trim(), "Wikipedia welcome text was not the expected.");

    WikipediaLocalePage wikipediaLocalePage = wikipediaHomePage.goToWikipediaLocalePage(getDriver());

    // To set correct locale for creating new localization text.
    // Can be changed dynamically during test execution.
    L10Nparser.setActualLocale(Configuration.get(Configuration.Parameter.LOCALE));
    Locale actualLocale = L10Nparser.getActualLocale();
    LOGGER.info(actualLocale.toString());
    
    sa.assertTrue(wikipediaLocalePage.checkMultipleLocalization(), "Localization error: " + L10Nparser.getAssertErrorMsg());

    L10Nparser.saveLocalization();
    sa.assertAll();
}
```
5. Copy all required values in your existing **locale_xx_XX.properties** file from **new_locale_xx_XX** file.
6. Now elements could be accessed by locale text.
```java
@FindBy(id = "pt-anontalk")
private ExtendedWebElement discussionElem;

@FindBy(linkText = "{L10N:discussionElem}")
private ExtendedWebElement discussionBtn;

public String getDiscussionText(){
    if (discussionBtn.isPresent()) {
        return welcomeText.getText();
    }
    return "";
}
```
