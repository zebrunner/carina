#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.components.compare;

import java.util.List;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractUIObject;

public class CandidateBlock extends AbstractUIObject {

    @FindBy(xpath = ".//input[contains(@id, 'sSearch')]")
    private ExtendedWebElement inputField;

    @FindBy(xpath = "./div[contains(@class, 'autocomplete-search')]//a[not(@class)]")
    private List<ExtendedWebElement> autocompleteSearchElements;

    public CandidateBlock(WebDriver driver, SearchContext searchContext) {
        super(driver, searchContext);
    }

    public void sendKeysToInputField(String text) {
        inputField.click();
        inputField.type(text);
    }

    public void getFirstPhone() {
        pause(1);
        Assert.assertTrue(autocompleteSearchElements.size() > 0, "No phones found!");
        autocompleteSearchElements.get(0).assertElementPresent();
        autocompleteSearchElements.get(0).click();
    }
}
