#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.zebrunner.carina.core.IAbstractTest;
import ${package}.carina.demo.mobile.gui.pages.ios.PreferencesPage;
import ${package}.carina.demo.utils.MobileContextUtils;
import com.zebrunner.carina.utils.mobile.IMobileUtils;

import io.appium.java_client.InteractsWithApps;

public class IOSPreferencesTest implements IAbstractTest, IMobileUtils {
    
    @Test
    public void nativePreferencesTest() {
        WebDriver driver = getDriver();
        MobileContextUtils contextUtils = new MobileContextUtils();
        ((InteractsWithApps) contextUtils.getPureDriver(driver)).activateApp("com.apple.Preferences");
        PreferencesPage preferencesPage = new PreferencesPage(driver);
        preferencesPage.clickGeneralBtn();
        driver.navigate().back();
    }

}
