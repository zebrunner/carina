#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages;

import java.lang.invoke.MethodHandles;
import java.util.List;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractPage;
import ${package}.carina.demo.gui.components.FooterMenu;
import ${package}.carina.demo.gui.components.WeValuePrivacyAd;


public class HomePage extends AbstractPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FindBy(id = "footmenu")
    private FooterMenu footerMenu;

    @FindBy(xpath = "//div[contains(@class, 'brandmenu-v2')]//a")
    private List<ExtendedWebElement> brandLinks;

    @FindBy(className = "news-column-index")
    private ExtendedWebElement newsColumn;

    public HomePage(WebDriver driver) {
        super(driver);
        setUiLoadedMarker(newsColumn);
        setPageAbsoluteURL(R.CONFIG.get(Configuration.Parameter.URL.getKey()));
    }

    public FooterMenu getFooterMenu() {
        return footerMenu;
    }

    public BrandModelsPage selectBrand(String brand) {
        LOGGER.info("selecting '" + brand + "' brand...");
        for (ExtendedWebElement brandLink : brandLinks) {
            String currentBrand = brandLink.getText();
            LOGGER.info("currentBrand: " + currentBrand);
            if (brand.equalsIgnoreCase(currentBrand)) {
                brandLink.click();
                return new BrandModelsPage(driver);
            }
        }
        throw new RuntimeException("Unable to open brand: " + brand);
    }
    
    public WeValuePrivacyAd getWeValuePrivacyAd() {
    	return new WeValuePrivacyAd(driver);
    }
}
