# Dataproviders

Modern test automation frameworks should support data-driven testing, when you are capable to verify a variety of edge cases providing test data sets into the tests using external data sources. Carina is not an exclusion, framework supports multiple ways of tests parametrization (see [samples](https://github.com/qaprosoft/carina-demo/blob/master/src/test/java/com/qaprosoft/carina/demo/DataprovidersSampleTest.java)):

* Java data-providers
* XML parametrization
* XLS data-providers
* CSV data-providers

## Java data-provider
As far as Carina is based on TestNG framework you are able to use well known Java data-providers that returns matrix of Objects and passed to appropriate test arguments. You have to use dataProvider attribute along with @Test annotation and implement method annotated with `@DataProvider` that will return `Object[][]` as test data set:
```java
@Test(dataProvider = "DP1")
public void testMuliplyOperation(int a, int b, int c)
{
	int actual = a * b;
	int expected = c;
	Assert.assertEquals(actual, expected, "Invalid sum result!");
}

@DataProvider(parallel = false, name = "DP1")
public static Object[][] dataprovider()
{
	return new Object[][] {
		{ 2, 3, 6 },
		{ 6, 6, 36 },
		{ 5, 8, 40 } 
	};
}
```

## XML parametrization
TestNG supports parametrization from XML files, when you organize tests suites using XML files. Parameterized test method should be annotated with `@Parameters({ "a", "b", "c" })` and appropriate method arguments should be listed. Also, you have to pass all required parameters from XML suite file:
```java
@Test
@Parameters({ "a", "b", "c" })
public void testSubstractOperation(int a, int b, int c) {
	int actual = Integer.valueOf(a) - Integer.valueOf(b);
	int expected = Integer.valueOf(c);
	Assert.assertEquals(actual, expected, "Invalid substract result!");
}

...
<test name="Substract operation test">
	<parameter name="a" value="12"/>
	<parameter name="b" value="3"/>
	<parameter name="c" value="9"/>
	<classes>
		<class name="com.qaprosoft.carina.demo.DataprovidersSampleTest">
			<methods>
				<include name="testSubstractOperation"/>
			</methods>
		</class>
	</classes>
</test>
...
```

## XLS/CSV data-providers
Carina test framework provides the possibility to write all tests with data providers in one place include parametrization using external XLS/CSV spreadsheets. First of all you need to declare test class that extends `AbstractTest.java`:
After that, you can specify data providers tests as follows.
```java
public class DataprovidersSampleTest extends AbstractTest {
	@Test(dataProvider = "DataProvider")
	@XlsDataSourceParameters(path = "xls/demo.xlsx", sheet = "Calculator", dsUid = "TUID", dsArgs = "a,b,c")
	public void testSumOperation(String a, String b, String c) {
		int actual = Integer.valueOf(a) + Integer.valueOf(b);
		int expected = Integer.valueOf(c);
		Assert.assertEquals(actual, expected, "Invalid sum result!");
	}
}
```

This test uses XLS/CSV files as the data source. Every row in a spreadsheet is a set of arguments for a test. You should specify the `dataProvider` parameter for TestNG annotation `@Test` . Carina test framework initially defines several data providers methods in `AbstractTest.java`, which you've extended earlier:

* createData method (data provider name = "DataProvider") for common usecases

* createDataSingeThread method (data provider name = "SingleDataProvider") for single-thread execution.

To specify XLS/CSV spreadsheets for test you should declare `@XlsDataSourceParameters` annotation and define its parameters:

* path - file path located in src/test/resources
* sheet - sheet name
* dsUid - data-source unique identifier
* dsArgs - column names from sheet

Here you can look at the spreadsheet as a data provider example for your further tests:

![XLS file - Data Provider - screenshot](./img/xlsscreen.png)

In TUID column you should specify some unique test identifier that will be set at the beginning of test name in test results report. In next columns you can specify arguments for a test and their values in lower rows. They will be used as parameters in `@XlsDataSourceParameters`. In this example values of a,b,c arguments were defined in 3 sets of values with different TUID.
