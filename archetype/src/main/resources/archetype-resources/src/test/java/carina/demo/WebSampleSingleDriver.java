#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import ${package}.carina.demo.gui.components.footer.FooterMenuBase;
import ${package}.carina.demo.gui.pages.common.CompareModelsPageBase;
import ${package}.carina.demo.gui.pages.common.HomePageBase;
import ${package}.carina.demo.gui.components.compare.ModelSpecs;
import ${package}.carina.demo.gui.components.compare.ModelSpecs.SpecType;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.agent.core.annotation.TestLabel;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;

/**
 * This sample shows how create Web test with dependent methods which shares existing driver between methods.
 *
 * @author qpsdemo
 */
public class WebSampleSingleDriver implements IAbstractTest {

    private HomePageBase homePage = null;
    private CompareModelsPageBase comparePage = null;
    private List<ModelSpecs> specs = new ArrayList<>();

    @BeforeSuite
    public void startDriver() {
        // Open GSM Arena home page and verify page is opened
        homePage = initPage(getDriver(), HomePageBase.class);
    }

    @Test()
    @MethodOwner(owner = "qpsdemo")
    @TestLabel(name = "feature", value = {"web", "regression"})
    public void testOpenPage() {
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");
    }

    @Test(dependsOnMethods="testOpenPage") //for dependent tests Carina keeps driver sessions by default
    @MethodOwner(owner = "qpsdemo")
    @TestLabel(name = "feature", value = {"web", "regression"})
    public void testOpenCompare() {
        // Open GSM Arena home page and verify page is opened
        // Open model compare page
        FooterMenuBase footerMenu = homePage.getFooterMenu();
        Assert.assertTrue(footerMenu.isUIObjectPresent(2), "Footer menu wasn't found!");
        comparePage = homePage.openComparePage();
        comparePage.isPageOpened();
    }

    @Test(dependsOnMethods="testOpenCompare") //for dependent tests Carina keeps driver sessions by default
    @MethodOwner(owner = "qpsdemo")
    @TestLabel(name = "feature", value = {"web", "regression"})
    public void testReadSpecs() {
        // Compare 3 models
        specs = comparePage.compareModels("Samsung Galaxy J3", "Samsung Galaxy J5", "Samsung Galaxy J7 Pro");
    }

    @Test(dependsOnMethods="testReadSpecs") //for dependent tests Carina keeps driver sessions by default
    @MethodOwner(owner = "qpsdemo")
    @TestLabel(name = "feature", value = {"web", "acceptance"})
    public void testCompareModels() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(specs.get(0).readSpec(SpecType.ANNOUNCED), "2016, March 31");
        softAssert.assertEquals(specs.get(1).readSpec(SpecType.ANNOUNCED), "2015, June 19");
        //for desktop could be compared 3 devices, when for mobile only 2
        if (specs.size() > 2) {
            softAssert.assertEquals(specs.get(2).readSpec(SpecType.ANNOUNCED), "2017, June");
        }
        softAssert.assertAll();
    }

}
