#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import com.qaprosoft.carina.core.foundation.cucumber.CucumberBaseTest;

import io.cucumber.testng.CucumberOptions;

@CucumberOptions(features = "src/test/resources/features/GSMArenaNews.feature",
        glue = "${package}.carina.demo.cucumber.steps",
        plugin={"pretty",
                "html:target/cucumber-core-test-report",
                "pretty:target/cucumber-core-test-report.txt",
                "json:target/cucumber-core-test-report.json",
                "junit:target/cucumber-core-test-report.xml"}
)
public class CucumberWebSampleTest extends CucumberBaseTest {
    //do nothing here as everything is declared in "GSMArenaNews.feature" and steps

}
