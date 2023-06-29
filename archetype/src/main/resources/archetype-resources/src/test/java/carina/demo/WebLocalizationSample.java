#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import com.zebrunner.agent.core.annotation.TestLabel;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import ${package}.carina.demo.gui.pages.desktop.WikipediaHomePage;
import ${package}.carina.demo.gui.pages.desktop.WikipediaLocalePage;
import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.resources.L10N;
import com.zebrunner.carina.webdriver.config.WebDriverConfiguration;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Locale;

/**
 * This sample shows how create Web Localization test with Resource Bundle.
 *
 * @author qpsdemo
 */
public class WebLocalizationSample implements IAbstractTest {

    @BeforeClass
    public void testLocaleLoad() {
        Locale locale = L10N.getLocale();
        String loadedLocale = locale.getLanguage() + "_" + locale.getCountry();
        String configLocale = Configuration.getRequired(WebDriverConfiguration.Parameter.LOCALE);
        Assert.assertEquals(loadedLocale, configLocale);
    }

    @Test
    @MethodOwner(owner = "qpsdemo")
    @TestLabel(name = "feature", value = "l10n")
    public void testLanguages() {
        WikipediaHomePage wikipediaHomePage = new WikipediaHomePage(getDriver());
        wikipediaHomePage.open();

        WikipediaLocalePage wikipediaLocalePage = wikipediaHomePage.goToWikipediaLocalePage(getDriver());

        wikipediaLocalePage.clickMoreButton();
        wikipediaLocalePage.hoverContribElem();
        wikipediaLocalePage.clickDiscussionBtn();

        L10N.assertAll();
    }

    @Test
    @MethodOwner(owner = "qpsdemo")
    @TestLabel(name = "feature", value = "l10n")
    public void testAddNewLanguages() {
        WikipediaHomePage wikipediaHomePage = new WikipediaHomePage(getDriver());
        wikipediaHomePage.open();

        WikipediaLocalePage wikipediaLocalePage = wikipediaHomePage.goToWikipediaLocalePage(getDriver());

        wikipediaLocalePage.hoverCreateAccountElem();
        wikipediaLocalePage.hoverWelcomeText();

        wikipediaLocalePage.hoverHeaders();

        wikipediaLocalePage.clickMoreButton();
        wikipediaLocalePage.hoverContribElem();
        wikipediaLocalePage.clickDiscussionBtn();

        L10N.flush();
        L10N.assertAll();
    }

    @Test
    @MethodOwner(owner = "qpsdemo")
    @TestLabel(name = "feature", value = "l10n")
    public void testElementsSearchWithL10n() {
        WikipediaHomePage wikipediaHomePage = new WikipediaHomePage(getDriver());
        wikipediaHomePage.open();

        WikipediaLocalePage wikipediaLocalePage = wikipediaHomePage.goToWikipediaLocalePage(getDriver());

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(wikipediaLocalePage.isWelcomeTextPresent());
        String actual = wikipediaLocalePage.getDiscussionText();
        String expected = L10N.getText("WikipediaLocalePage.discussionElem");
        softAssert.assertEquals(actual, expected);
        softAssert.assertAll();
    }

}
