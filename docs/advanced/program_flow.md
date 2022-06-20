Under the hood Carina uses TestNG framework, so the first class to initialize is RemoteTestNGStarter.class. Program life cycle logic could be observed at [TestNG.class](https://github.com/cbeust/testng/blob/master/src/main/java/org/testng/TestNG.java) run() method.

The initializing turn comes to Carina when [CarinaListenerChain.class](https://github.com/zebrunner/carina/blob/master/carina-core/src/main/java/com/qaprosoft/carina/core/foundation/listeners/CarinaListenerChain.java) object created.
It extends [ListenerChain.class](http://javadox.com/com.nordstrom.tools/testng-foundation/1.10.0/com/nordstrom/automation/testng/package-summary.html)
which will create, sort and attach [AbstractTest.class](https://github.com/zebrunner/carina/blob/master/carina-core/src/main/java/com/qaprosoft/carina/core/foundation/AbstractTest.java) listeners. This whole sequence is described in [TestRunner.class](https://github.com/cbeust/testng/blob/master/src/main/java/org/testng/TestRunner.java) init() method.

[AbstractTest.class](https://github.com/zebrunner/carina/blob/master/carina-core/src/main/java/com/qaprosoft/carina/core/foundation/AbstractTest.java) listeners are:

```
@LinkedListeners({ CarinaListener.class, TestRunListener.class, DataProviderInterceptor.class })
```

Theese listeners being attached and created when transform(IListenersAnnotation annotation, Class testClass) method is called.

* `TestRunListener.class` and `DataProviderInterceptor.class` are implemented in zebrunner. 

* [CarinaListener.class](https://github.com/zebrunner/carina/blob/master/carina-core/src/main/java/com/qaprosoft/carina/core/foundation/listeners/CarinaListener.java)
as it comes from the name is a Carina's listener.

Because CarinaListener object created, the class static field is initialized in it. There are several important steps inside:

* R.reinit(). This method load's default values for all parameters from [carina-core](https://github.com/zebrunner/carina/blob/master/carina-core/src/main/resources)
   , then override's them with users configuration (api.properties, config.properties, testdata.properties, email.properties, report.properties, database.properties).
* Configure log4j properties 
* Initializing L10N feature.

Then listeners attached to different Lists according to their implementations in TestNG:

![Report link](../img/debug_entry_point1.png)

After that methods are called in appropriate order in the [ListenerChain.class](http://javadox.com/com.nordstrom.tools/testng-foundation/1.10.0/com/nordstrom/automation/testng/package-summary.html):

* onStart(ISuite suite). There is called every ISuiteListener's onStart() method that where mentioned in AbstractTest.class (CarinaListener and TestRunListener). CarinaListener.class onStart(ISuite suite) method will configure logging level and thread count.

* now your test class is considered initialized and onStart(ITestContext testContext) method is called. Udid is generated there.

* onBeforeClass(ITestClass testClass). Complete steps described in @BeforeClass annotation from your test.

* onTestStart(ITestResult result) provide described data to test.

Next TestRunner.class runs code described in your test.class. The following route depends on what you are doing in your test:

**UI (web, mobile)**

```
@Test @MethodOwner()
public void webTest() {
   HomePage chromeHomePage = new HomePage(getDriver());
   chromeHomePage.open();
   Assert.assertTrue(chromeHomePage.isPageOpened(), "Chrome home page is not opened!"); 
} 
```

Debug entry point at : [AbstractPage](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/gui/AbstractPage.java) constructor and [IDriverPool](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/IDriverPool.java) getDriver() method.
   
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

Debug entry point at : [AbstractApiMethodV2](https://github.com/zebrunner/carina/blob/master/carina-api/src/main/java/com/qaprosoft/carina/core/foundation/api/AbstractApiMethodV2.java) constructor.

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

These are user's classes, samples at carina-demo: [UserMapper](https://github.com/zebrunner/carina-demo/blob/master/src/main/java/com/qaprosoft/carina/demo/db/mappers/UserMapper.java), [ConnectionFactory](https://github.com/zebrunner/carina-demo/blob/master/src/main/java/com/qaprosoft/carina/demo/utils/ConnectionFactory.java), [User](https://github.com/zebrunner/carina-demo/blob/master/src/main/java/com/qaprosoft/carina/demo/db/models/User.java).

###FAQ

**Dependent vs. independent tests. Which approach is better?**

Try to develop fully independent tests to reuse all the benefits of the multi-threading execution. For example [Zebrunner Selenium Grid](https://zebrunner.com/) provides 1000 threads as default limitation and allow to execute your full regression scenarios in minutes!
Use dependent methods via `dependsOnMethods` Test Annotation only if it is really required by Test logic. Carina will preserve all drivers for dependent methods so you can start driver in one method and proceed to with the page in another.
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
