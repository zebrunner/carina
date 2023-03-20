#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.components;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractUIObject;

public class NewsItem extends AbstractUIObject {

    @FindBy(xpath = ".//a/*[name()='h3' or name()='h2']")
    public ExtendedWebElement titleLink;

    public NewsItem(WebDriver driver, SearchContext sc) {
        super(driver, sc);
    }

    public String readTitle() {
        return titleLink.getElement().getText();
    }
}
