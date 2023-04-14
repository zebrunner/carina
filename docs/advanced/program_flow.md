Under the hood, Carina uses TestNG framework, so the first class to initialize is `RemoteTestNGStarter` class. Program lifecycle logic can be observed at `TestNG` [run()](https://github.com/cbeust/testng/blob/c394d371224b7d1aa3872c34c1f7818e2b9335f9/testng-core/src/main/java/org/testng/TestNG.java#L1058) method.

The initializing turn comes to Carina when [CarinaListenerChain](https://github.com/zebrunner/carina/blob/master/src/main/java/com/zebrunner/carina/core/listeners/CarinaListenerChain.java) object is created.
It extends [ListenerChain](http://javadox.com/com.nordstrom.tools/testng-foundation/1.10.0/com/nordstrom/automation/testng/package-summary.html)
which will create, sort and attach [IAbstractTest](https://github.com/zebrunner/carina/blob/master/src/main/java/com/zebrunner/carina/core/IAbstractTest.java) listeners `CarinaListener.class, TestRunListener.class, FilterTestsListener.class`. This whole sequence is described in `TestRunner` [init()](https://github.com/cbeust/testng/blob/c394d371224b7d1aa3872c34c1f7818e2b9335f9/testng-core/src/main/java/org/testng/TestRunner.java#L229) method.

These listeners are being attached and created when transform(IListenersAnnotation annotation, Class testClass) method is called.

* [FilterTestsListener](https://github.com/zebrunner/carina/blob/master/src/main/java/com/zebrunner/carina/core/listeners/FilterTestsListener.java) which is resposible for tests execution [rules]( https://zebrunner.github.io/carina/configuration/#tests-execution-filter-configuration)

* `TestRunListener` which is implemented in the Zebrunner agent. 

* [CarinaListener](https://github.com/zebrunner/carina/blob/master/src/main/java/com/zebrunner/carina/core/listeners/CarinaListener.java) which is the main Carina TestNG listener.

Because CarinaListener object is created, the class static field is initialized in it. There are several important steps inside:

* R.reinit(). This method loads default values for all parameters from [carina-core](https://github.com/zebrunner/carina/blob/master/src/main/resources), then overrides them with user's configurations (_api.properties, _config.properties, _testdata.properties, _email.properties, _report.properties, _database.properties).
* Configure log4j2x properties 
* Initialize L10N feature.

Then standard listeners according to their implementations in TestNG:

![Report link](../img/debug_entry_point1.png)

Then overridden TestNG methods

* `FilterTestsListener->onStart(ISuite suite)`

* `TestRunListener->onStart(ISuite suite)`

* `CarinaListener->onStart(ISuite suite)`

* Now your test class is considered initialized and `onStart(ITestContext testContext)` method is called.

* `onBeforeClass(ITestClass testClass)`. Complete the steps described in `@BeforeClass` annotation from your test.

* `onTestStart(ITestResult result)` provides described data to test.

Next, `TestRunner.class` runs code described in your test class. The following route depends on what you are doing in your test:

**UI (web, mobile)**

```
@Test @MethodOwner()
public void webTest() {
   HomePage chromeHomePage = new HomePage(getDriver());
   chromeHomePage.open();
   Assert.assertTrue(chromeHomePage.isPageOpened(), "Chrome home page is not opened!"); 
} 
```

Debug entry point at : [AbstractPage](https://github.com/zebrunner/carina-webdriver/blob/master/src/main/java/com/zebrunner/carina/webdriver/gui/AbstractPage.java) constructor and [IDriverPool](https://github.com/zebrunner/carina-webdriver/blob/master/src/main/java/com/zebrunner/carina/webdriver/IDriverPool.java) getDriver() method.
   
**API**

```
@Test()
public void testCreateUser() throws Exception {
   PostUserMethod api = new PostUserMethod();
   api.expectResponseStatus(HttpResponseStatusType.CREATED_201);
   api.callAPI();
   api.validateResponse();
}
```

Debug entry point at : [AbstractApiMethodV2](https://github.com/zebrunner/carina-api/blob/master/src/main/java/com/zebrunner/carina/api/AbstractApiMethodV2.java) constructor.

**Database**

```
@Test
public void createUser() {

   User USER = new User() {{
                     setUsername("bmarley");
                     setFirstName("Bob");
                     setLastName("Marley");
                     setStatus(Status.ACTIVE);
   }};

   try (SqlSession session = ConnectionFactory.getSqlSessionFactory().openSession(true)) {
     UserMapper userMapper = session.getMapper(UserMapper.class);
     userMapper.create(USER);
     checkUser(userMapper.findById(USER.getId()));
   }
}
```  

These are user's classes, samples at carina-demo: [UserMapper](https://github.com/zebrunner/carina-demo/blob/master/src/main/java/com/zebrunner/carina/demo/db/mappers/UserMapper.java), [ConnectionFactory](https://github.com/zebrunner/carina-demo/blob/master/src/main/java/com/zebrunner/carina/demo/utils/ConnectionFactory.java), [User](https://github.com/zebrunner/carina-demo/blob/master/src/main/java/com/zebrunner/carina/demo/db/models/User.java).

### FAQ

**Dependent vs. independent tests. Which approach is better?**

Try to develop fully independent tests to reuse all the benefits of the multi-threading execution. For example, [Zebrunner Selenium Grid](https://zebrunner.com/) provides **1000 threads** as default limitation and allows to execute your full regression scenarios in minutes!
Use dependent methods via `dependsOnMethods` Test Annotation only if it is really required by Test logic. Carina will preserve all drivers for dependent methods so you can start a driver in one method and proceed with the page in another.
```
public class WebSampleSingleDriver implements IAbstractTest {
    HomePage homePage = null;
    CompareModelsPage comparePage = null;
    List<ModelSpecs> specs = new ArrayList<>();

    @BeforeSuite
    public void startDriver() {
        // Open GSM Arena home page and verify page is opened
        homePage = new HomePage(getDriver());
    }
    
    @Test
    public void testOpenPage() {
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");
    }
    
    @Test(dependsOnMethods="testOpenPage") //for dependent tests Carina keeps driver sessions by default
    public void testOpenCompare() {
        // Open GSM Arena home page and verify page is opened
        // Open model compare page
        FooterMenu footerMenu = homePage.getFooterMenu();
        Assert.assertTrue(footerMenu.isUIObjectPresent(2), "Footer menu wasn't found!");
        comparePage = footerMenu.openComparePage();

    }
    
    @Test(dependsOnMethods="testOpenCompare") //for dependent tests Carina keeps driver sessions by default
    public void testReadSpecs() {
        // Compare 3 models
        specs = comparePage.compareModels("Samsung Galaxy J3", "Samsung Galaxy J5", "Samsung Galaxy J7 Pro");
    }
    
    @Test(dependsOnMethods="testReadSpecs") //for dependent tests Carina keeps driver sessions by default
    public void testCompareModels() {
        // Verify model announced dates
        SoftAssert() softAssert = new SoftAssert();
        softAssert.assertEquals(specs.get(0).readSpec(SpecType.ANNOUNCED), "2016, March 31");
        softAssert.assertEquals(specs.get(1).readSpec(SpecType.ANNOUNCED), "2015, June 19");
        softAssert.assertEquals(specs.get(2).readSpec(SpecType.ANNOUNCED), "2017, June");
        softAssert.assertAll();
    }
}
```
