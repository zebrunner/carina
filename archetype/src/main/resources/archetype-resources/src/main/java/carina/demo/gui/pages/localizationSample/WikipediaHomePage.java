#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.localizationSample;

import java.util.List;
import java.util.Locale;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractPage;

public class WikipediaHomePage extends AbstractPage {

    @FindBy(xpath = "//div[@id='js-lang-lists']//a")
    private List<ExtendedWebElement> langList;

    @FindBy(id = "js-lang-list-button")
    private ExtendedWebElement langListBtn;

    public WikipediaHomePage(WebDriver driver) {
        super(driver);
        setPageAbsoluteURL("https://www.wikipedia.org/");
    }

    public WikipediaLocalePage goToWikipediaLocalePage(WebDriver driver) {
        openLangList();
        if (!langList.isEmpty()) {
            for (ExtendedWebElement languageBtn : langList) {
                String localeStr = Configuration.get(Configuration.Parameter.LOCALE);
                Locale locale = parseLocale(localeStr);
                if (languageBtn.getAttribute("lang").equals(locale.getLanguage())) {
                    languageBtn.click();
                    return new WikipediaLocalePage(driver);
                }
            }
        }
        throw new RuntimeException("No language ref was found");
    }

    public void openLangList() {
        langListBtn.clickIfPresent();
    }

    private Locale parseLocale(String localeToParse) {
        String[] localeSetttings = localeToParse.trim().split("_");
        String lang, country = "";
        lang = localeSetttings[0];
        if (localeSetttings.length > 1) {
            country = localeSetttings[1];
        }

        return new Locale(lang, country);
    }
}
