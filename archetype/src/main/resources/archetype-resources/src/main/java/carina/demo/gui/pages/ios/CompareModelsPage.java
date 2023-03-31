#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.ios;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.gui.components.compare.CandidateBlock;
import ${package}.carina.demo.gui.components.compare.ModelSpecs;
import ${package}.carina.demo.gui.pages.common.CompareModelsPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

@DeviceType(pageType = DeviceType.Type.IOS_PHONE, parentClass = CompareModelsPageBase.class)
public class CompareModelsPage extends CompareModelsPageBase {

    @FindBy(xpath = "//h1[@class='section nobor']")
    private ExtendedWebElement compareBlock;

    @FindBy(xpath = "//div[contains(@class, 'candidate-search')]")
    private List<CandidateBlock> candidateBlocks;

    public CompareModelsPage(WebDriver driver) {
        super(driver);
        setUiLoadedMarker(compareBlock);
    }

    @Override
    public List<ModelSpecs> compareModels(String... models) {
        CandidateBlock candidateBlock;
        List<ModelSpecs> modelSpecs = new ArrayList<>();
        ModelSpecs modelSpec;
        for (int index = 0; index < models.length && index < 2; index++) {
            modelSpec = new ModelSpecs();
            candidateBlock = candidateBlocks.get(index);
            candidateBlock.sendKeysToInputField(models[index]);
            candidateBlock.getFirstPhone();
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
