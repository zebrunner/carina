#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.ios;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.gui.components.ModelItem;
import ${package}.carina.demo.gui.pages.common.BrandModelsPageBase;
import ${package}.carina.demo.gui.pages.common.ModelInfoPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

@DeviceType(pageType = DeviceType.Type.IOS_PHONE, parentClass = BrandModelsPageBase.class)
public class BrandModelsPage extends BrandModelsPageBase {

    @FindBy(xpath = "//div[@class='general-menu']//li")
    private List<ModelItem> models;

    @FindBy(xpath = "//div[@class='cls-btn']")
    private ExtendedWebElement adCloseButton;

    public BrandModelsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public ModelInfoPageBase selectModel(String modelName) {
        adCloseButton.clickIfPresent();
        for (ModelItem model : models) {
            if (modelName.equalsIgnoreCase(model.readModel())) {
                return model.openModelPage();
            }
        }
        throw new RuntimeException("Unable to open model: " + modelName);

    }

    @Override
    public List<ModelItem> getModels() {
        return models;
    }

}
