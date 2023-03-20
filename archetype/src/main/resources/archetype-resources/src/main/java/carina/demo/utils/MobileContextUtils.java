#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.utils;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import org.openqa.selenium.ContextAware;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.decorators.Decorated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.webdriver.DriverHelper;
import com.zebrunner.carina.webdriver.IDriverPool;

import io.appium.java_client.remote.SupportsContextSwitching;

public class MobileContextUtils implements IDriverPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Returns a pure driver without listeners
     */
    public WebDriver getPureDriver(WebDriver driver) {
        if (driver instanceof Decorated<?>) {
            driver = (WebDriver) ((Decorated<?>) driver).getOriginal();
        }
        return driver;
    }

    public void switchMobileContext(View context){
        switchMobileContext(context, null);
    }

    public void switchMobileContext(View context, View exclude) {
        WebDriver driver = getDriver();
        DriverHelper help = new DriverHelper();
        Set<String> contextHandles = help.performIgnoreException(((ContextAware) driver)::getContextHandles);
        String desiredContext = "";
        boolean isContextPresent = false;
        LOGGER.info("Existing contexts: ");
        for (String cont : contextHandles) {
            if (cont.contains(context.getView())) {
                if (exclude != null && cont.contains(exclude.getView())){
                    continue;
                }
                desiredContext = cont;
                isContextPresent = true;
            }
            LOGGER.info(cont);
        }
        if (!isContextPresent) {
            throw new NotFoundException("Desired context is not present");
        }
        LOGGER.info("Switching to context : " + desiredContext);
        ((SupportsContextSwitching) driver).context(desiredContext);
    }

    public enum View {
        NATIVE("NATIVE_APP"),
        WEB_CARINA("WEBVIEW_com.solvd.carinademoapplication"),

        WEB_BROWSER("WEBVIEW_");

        String viewName;

        View(String viewName) {
            this.viewName = viewName;
        }

        public String getView() {
            return this.viewName;
        }
    }

}
