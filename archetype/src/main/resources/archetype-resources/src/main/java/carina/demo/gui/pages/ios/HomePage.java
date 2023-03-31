#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.ios;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.gui.components.footer.MobileFooterMenu;
import ${package}.carina.demo.gui.pages.common.AllBrandsPageBase;
import ${package}.carina.demo.gui.pages.common.BrandModelsPageBase;
import ${package}.carina.demo.gui.pages.common.CompareModelsPageBase;
import ${package}.carina.demo.gui.pages.common.HomePageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

@DeviceType(pageType = DeviceType.Type.IOS_PHONE, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {

    @FindBy(xpath = "//div[contains(@class, 'general-menu material-card')]//a")
    private List<ExtendedWebElement> brandLinks;

    @FindBy(id = "news-container")
    private ExtendedWebElement newsColumn;

    @FindBy(xpath = "//span[@class='menu']")
    private ExtendedWebElement headerMenuButton;

    @FindBy(xpath = "//a[text()='Compare']")
    private ExtendedWebElement compareButton;

    @FindBy(xpath = "//ul[@class='links right']//a[text()='[...]']")
    private ExtendedWebElement allBrandsButton;

    @FindBy(xpath = "//header//nav//ul//a[text()='News']")
    private ExtendedWebElement newsButton;

    @FindBy(xpath = "//footer")
    private MobileFooterMenu footer;

    public HomePage(WebDriver driver) {
        super(driver);
        setUiLoadedMarker(newsColumn);
    }

    @Override
    public BrandModelsPageBase selectBrand(String brandName) {
        for (ExtendedWebElement brandLink : brandLinks) {
            String currentBrand = brandLink.getText();
            if (brandName.equalsIgnoreCase(currentBrand)) {
                brandLink.click();
                return initPage(driver, BrandModelsPageBase.class);
            }
        }
        throw new RuntimeException("Unable to open brand: " + brandName);
    }

    @Override
    public MobileFooterMenu getFooterMenu() {
        return footer;
    }

    @Override
    public CompareModelsPageBase openComparePage() {
        headerMenuButton.click();
        compareButton.click();
        return initPage(driver, CompareModelsPageBase.class);
    }

    @Override
    public AllBrandsPageBase openAllBrandsPage() {
        headerMenuButton.click();
        allBrandsButton.click();
        return initPage(driver, AllBrandsPageBase.class);
    }

}
