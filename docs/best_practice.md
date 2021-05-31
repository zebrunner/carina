### Try to init pages and drivers in places where they are used

```
@Test(){
public void someTest(){
   //Page declared and initialized right before using it
   HomePage homePage = new HomePage(getDriver());
   homePage.open();
   FooterMenu footerMenu = homePage.getFooterMenu();
   CompareModelsPage comparePage = footerMenu.openComparePage();
   List<ModelSpecs> specs = comparePage.compareModels("Samsung Galaxy J3", "Samsung Galaxy J5", "Samsung Galaxy J7 Pro");
}
```
### Try to make independent tests
```
//But if there is no way to do so, make sure you use @Test(dependsOnMethods=XXX) tag
HomePage homePage = null;
CompareModelsPage comparePage = null;

@BeforeSuite
public void startDriver() {
   homePage = new HomePage(getDriver());
}

@Test
public void testOpenPage() {
   homePage.open();
   Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");
}

@Test(dependsOnMethods="testOpenPage") //for dependent tests Carina keeps driver sessions by default
public void testOpenCompare() {
   FooterMenu footerMenu = homePage.getFooterMenu();
   Assert.assertTrue(footerMenu.isUIObjectPresent(2), "Footer menu wasn't found!");
}
```
### Hide all work with elements in page classes
```
In test:
public void someTest() {
   WikipediaHomePage wikipediaHomePage = new WikipediaHomePage(getDriver());
   wikipediaHomePage.open();
   WikipediaLocalePage wikipediaLocalePage = wikipediaHomePage.goToWikipediaLocalePage(getDriver());
}

In page class:
@FindBy(xpath = "//div[@id='js-lang-lists']//a")
private List<ExtendedWebElement> langList;

public WikipediaLocalePage goToWikipediaLocalePage(WebDriver driver) {
   openLangList();
   if (!langList.isEmpty()) {
      for (ExtendedWebElement languageBtn : langList) {
         String localeStr = Configuration.get(Configuration.Parameter.LOCALE);
         Locale locale = parseLocale(localeStr);
         if (languageBtn.getAttribute("lang").equals(locale.getLanguage())) {
           languageBtn.click();
           return new WikipediaLocalePage(driver);
         }
      }
   }
   throw new RuntimeException("No language ref was found");
}

public void openLangList() {
   langListBtn.clickIfPresent();
}
```

### Use Carina's features
```
//driver.openUrl() instead of selenium's driver.get()
//page.waitUntil() instead of wait.until()
//element.isPresent() for elements check
//page.getPageSource() instead of driver.getPageSource()
//etc
```
### Try to use SoftAssert overt Assert
```
public void testModelSpecs() {
  HomePage homePage = new HomePage(getDriver());
  homePage.open();
  BrandModelsPage productsPage = homePage.selectBrand("Samsung");
  // Select phone model
  ModelInfoPage productInfoPage = productsPage.selectModel("Galaxy A52 5G");
  
  // Verify phone specifications
  SoftAssert softAssert = new SoftAssert();
  softAssert.assertEquals(productInfoPage.readDisplay(), "6.5\"", "Invalid display info!");
  softAssert.assertEquals(productInfoPage.readCamera(), "64MP", "Invalid camera info!");
  softAssert.assertEquals(productInfoPage.readRam(), "6/8GB RAM", "Invalid ram info!");
  softAssert.assertEquals(productInfoPage.readBattery(), "4500mAh", "Invalid battery info!");
  softAssert.assertAll();
}
```
### Try to pass params through _config.properties, not in code
```
Will work both
1) put in _config.properties :
   selenium_url=http://localhost:4444/wd/hub
2) pass it right in test:
public void testCompareModels() {
   R.CONFIG.put("selenium_host", "http://localhost:4444/wd/hub");
   HomePage homePage = new HomePage(getDriver());
   homePage.open();
   ...
}
But recommended to use 1) variant for initialization of all parameters
```