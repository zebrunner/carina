# Databases

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
