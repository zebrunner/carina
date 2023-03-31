#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.pages.desktop;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import ${package}.carina.demo.gui.components.NewsItem;
import ${package}.carina.demo.gui.pages.common.NewsPageBase;
import com.zebrunner.carina.webdriver.locator.Context;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = NewsPageBase.class)
public class NewsPage extends NewsPageBase {

    @FindBy(xpath = "//div[@class='search-band']")
    private ExtendedWebElement searchDiv;

    @Context(dependsOn = "searchDiv")
    @FindBy(xpath = ".//input[@type='text']")
    private ExtendedWebElement searchTextField;

    @Context(dependsOn = "searchDiv")
    @FindBy(xpath = ".//input[@type='submit']")
    private ExtendedWebElement searchButton;

    @FindBy(xpath = "//div[@class='news-item']")
    private List<NewsItem> news;

    public NewsPage(WebDriver driver) {
        super(driver);
        setPageURL("/news.php3");
    }

    @Override
    public List<NewsItem> searchNews(String q) {
        searchTextField.type(q);
        searchButton.click();
        return news;
    }

}
