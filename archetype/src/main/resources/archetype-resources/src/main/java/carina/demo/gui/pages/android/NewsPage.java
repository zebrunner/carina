#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.android;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.gui.components.NewsItem;
import ${package}.carina.demo.gui.pages.common.NewsPageBase;
import com.zebrunner.carina.webdriver.locator.Context;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.decorator.PageOpeningStrategy;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = NewsPageBase.class)
public class NewsPage extends NewsPageBase {

    @FindBy(xpath = "//div[@class='search-field']")
    private ExtendedWebElement searchComp;

    @Context(dependsOn = "searchComp")
    @FindBy(tagName = "input")
    private ExtendedWebElement searchField;

    @Context(dependsOn = "searchComp")
    @FindBy(tagName = "button")
    private ExtendedWebElement searchButton;

    @FindBy(xpath = "//div[@class='news-item']")
    private List<NewsItem> news;

    public NewsPage(WebDriver driver) {
        super(driver);
        setPageOpeningStrategy(PageOpeningStrategy.BY_ELEMENT);
        setUiLoadedMarker(searchField);
    }

    @Override
    public List<NewsItem> searchNews(String searchInput) {
        searchField.type(searchInput);
        searchButton.click();

        return news;
    }

}
