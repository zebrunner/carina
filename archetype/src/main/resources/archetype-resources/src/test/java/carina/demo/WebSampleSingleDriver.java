#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.zebrunner.agent.core.annotation.TestLabel;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import ${package}.carina.demo.gui.components.FooterMenu;
import ${package}.carina.demo.gui.components.compare.ModelSpecs;
import ${package}.carina.demo.gui.components.compare.ModelSpecs.SpecType;
import ${package}.carina.demo.gui.pages.CompareModelsPage;
import ${package}.carina.demo.gui.pages.HomePage;

/**
 * This sample shows how create Web test with dependent methods which shares existing driver between methods.
 * 
 * @author qpsdemo
 */
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
        FooterMenu footerMenu = homePage.getFooterMenu();
        Assert.assertTrue(footerMenu.isUIObjectPresent(2), "Footer menu wasn't found!");
        comparePage = footerMenu.openComparePage();

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
        // Verify model announced dates
        Assert.assertEquals(specs.get(0).readSpec(SpecType.ANNOUNCED), "2016, March 31");
        Assert.assertEquals(specs.get(1).readSpec(SpecType.ANNOUNCED), "2015, June 19");
        Assert.assertEquals(specs.get(2).readSpec(SpecType.ANNOUNCED), "2017, June");
    }


}
