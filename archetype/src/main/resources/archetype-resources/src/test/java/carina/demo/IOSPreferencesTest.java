#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.qaprosoft.carina.core.foundation.utils.mobile.IMobileUtils;
import ${package}.carina.demo.mobile.gui.pages.ios.PreferencesPage;

public class IOSPreferencesTest implements IAbstractTest, IMobileUtils {
    
    @Test
    public void nativePreferencesTest() {
        WebDriver driver = getDriver();
        PreferencesPage preferencesPage = new PreferencesPage(driver);
        preferencesPage.clickGeneralBtn();
        driver.navigate().back();
    }

}