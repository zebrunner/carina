#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.desktop;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.gui.pages.common.AllBrandsPageBase;
import ${package}.carina.demo.gui.pages.common.BrandModelsPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.decorator.PageOpeningStrategy;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = AllBrandsPageBase.class)
public class AllBrandsPage extends AllBrandsPageBase {

    @FindBy(xpath = "//h1[@class='article-info-name']")
    private ExtendedWebElement pageTitle;

    public AllBrandsPage(WebDriver driver) {
        super(driver);
        setPageOpeningStrategy(PageOpeningStrategy.BY_ELEMENT);
        setUiLoadedMarker(pageTitle);
    }

    @Override
    public BrandModelsPageBase selectBrand(String brandName){
        brandName = brandName.toUpperCase();
        for (ExtendedWebElement brand: findExtendedWebElements(By.xpath("//div[@class='st-text']//td/a"))){
            if (brand.getText().contains(brandName)){
                brand.click();
                return initPage(driver, BrandModelsPageBase.class);
            }
        }
        throw new RuntimeException("Unable to open brand page: " + brandName);
    }

}
