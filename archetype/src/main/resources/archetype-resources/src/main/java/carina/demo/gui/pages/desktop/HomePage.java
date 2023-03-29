#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.desktop;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ${package}.carina.demo.gui.components.footer.FooterMenu;
import ${package}.carina.demo.gui.pages.common.AllBrandsPageBase;
import ${package}.carina.demo.gui.pages.common.BrandModelsPageBase;
import ${package}.carina.demo.gui.pages.common.CompareModelsPageBase;
import ${package}.carina.demo.gui.pages.common.HomePageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FindBy(id = "footmenu")
    private FooterMenu footerMenu;

    @FindBy(xpath = "//div[contains(@class, 'brandmenu-v2')]//a")
    private List<ExtendedWebElement> brandLinks;

    @FindBys({ @FindBy(xpath = "//p[contains(@class, 'pad')]"), @FindBy(xpath = ".//*[contains(@class, 'pad-single')]") })
    private ExtendedWebElement phoneFinderButton;

    @FindBy(className = "news-column-index")
    private ExtendedWebElement newsColumn;

    @FindBy(xpath = "//span[text()='All brands']//parent::a")
    private ExtendedWebElement allBrandsButton;

    public HomePage(WebDriver driver) {
        super(driver);
        setUiLoadedMarker(newsColumn);
    }

    @Override
    public FooterMenu getFooterMenu() {
        return footerMenu;
    }

    @Override
    public CompareModelsPageBase openComparePage() {
        return getFooterMenu().openComparePage();
    }

    @Override
    public BrandModelsPageBase selectBrand(String brand) {
        LOGGER.info("selecting '" + brand + "' brand...");
        for (ExtendedWebElement brandLink : brandLinks) {
            String currentBrand = brandLink.getText();
            LOGGER.info("currentBrand: " + currentBrand);
            if (brand.equalsIgnoreCase(currentBrand)) {
                brandLink.click();
                return initPage(driver, BrandModelsPageBase.class);
            }
        }
        throw new RuntimeException("Unable to open brand: " + brand);
    }

    public ExtendedWebElement getPhoneFinderButton() {
        return phoneFinderButton;
    }

    public AllBrandsPageBase openAllBrandsPage(){
        allBrandsButton.click();
        return initPage(driver, AllBrandsPageBase.class);
    }

}
