#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.localizationSample;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Localized;
import com.qaprosoft.carina.core.gui.AbstractPage;

public class WikipediaLocalePage extends AbstractPage {

    @Localized
    @FindBy(xpath = "//*[@id='{L10N:HomePage.welcomeTextId}' or @class='welcome-title']")
    private ExtendedWebElement welcomeText;

    @Localized
    @FindBy(xpath = "//nav[@id='p-navigation']/descendant::ul[@class='vector-menu-content-list']/*")
    private List<ExtendedWebElement> pageLinks;

    @Localized
    @FindBy(id = "pt-anoncontribs")
    private ExtendedWebElement contribElem;

    @Localized
    @FindBy(id = "pt-createaccount")
    private ExtendedWebElement createAccountElem;

    @Localized
    @FindBy(id = "pt-anontalk")
    private ExtendedWebElement discussionElem;

    @FindBy(linkText = "{L10N:discussionElem}")
    private ExtendedWebElement discussionBtn;

    public String getDiscussionText(){
        if (discussionBtn.isPresent()) {
            return discussionBtn.getText();
        }
        return "";
    }

    public WikipediaLocalePage(WebDriver driver) {
        super(driver);
    }

    public String getWelcomeText(){
        if (welcomeText.isPresent()) {
            return welcomeText.getText();
        }
        return "";
    }

    public void hoverWelcomeText(){
        welcomeText.hover();
    }

    public void hoverContribElem(){
        contribElem.hover();
    }

    public void hoverCreateAccountElem(){
        createAccountElem.hover();
    }

    public void clickDiscussionBtn() {
        discussionElem.click();
    }

    public void hoverHeaders(){
        for (ExtendedWebElement pageLink: pageLinks) {
            pageLink.hover();
        }
    }
}
