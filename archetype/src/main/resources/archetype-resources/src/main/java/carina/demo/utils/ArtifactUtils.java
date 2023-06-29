#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.utils;

import com.zebrunner.carina.utils.report.SessionContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.List;

public final class ArtifactUtils {

    private ArtifactUtils() {
        // hide
    }

    public static boolean isArtifactPresent(FluentWait<WebDriver> wait, String name) {
        boolean isFound = false;
        try {
            isFound = wait.until(dr -> {
                List<String> list = SessionContext.listArtifacts(dr);
                if (list.contains(name)) {
                    return true;
                }
                return null;
            });
        } catch (TimeoutException e) {
            // do nothing
        }
        return isFound;
    }
}