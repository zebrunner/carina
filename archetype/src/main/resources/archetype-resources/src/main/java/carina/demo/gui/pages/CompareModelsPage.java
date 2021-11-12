#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractPage;
import ${package}.carina.demo.gui.components.compare.CondidateBlock;
import ${package}.carina.demo.gui.components.compare.ModelSpecs;

public class CompareModelsPage extends AbstractPage {

    private final String comparePageUrl = "https://www.gsmarena.com/compare.php3";

    @FindBy(xpath = "//div[contains(@class, 'candidate-search')]")
    private List<CondidateBlock> condidateBlocks;

    @FindBy(className = "compare-candidates")
    private ExtendedWebElement compareMenu;

    public CompareModelsPage(WebDriver driver) {
        super(driver);
        setUiLoadedMarker(compareMenu);
        setPageAbsoluteURL(comparePageUrl);
        //setPageURL("/compare.php3");
    }

    public List<ModelSpecs> compareModels(String... models) {
        CondidateBlock condidateBlock;
        List<ModelSpecs> modelSpecs = new ArrayList<>();
        ModelSpecs modelSpec;
        for (int index = 0; index < models.length; index++) {
            modelSpec = new ModelSpecs();
            condidateBlock = condidateBlocks.get(index);
            condidateBlock.sendKeysToInputField(models[index]);
            condidateBlock.getFirstPhone();
            for (ModelSpecs.SpecType type : ModelSpecs.SpecType.values()) {
                ExtendedWebElement spec = findExtendedWebElement(By.xpath(
                        String.format("//tr[.//a[text()='%s']]//td[@class='nfo'][%d]", type.getType(), index + 1)));
                modelSpec.setToModelSpecsMap(type, spec.getText());
            }
            modelSpecs.add(modelSpec);
        }
        return modelSpecs;
    }
}
