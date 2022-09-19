package com.qaprosoft.carina.core.foundation.utils.ios;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.qaprosoft.carina.core.foundation.utils.mobile.IMobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;

import io.appium.java_client.HidesKeyboardWithKeyName;
import io.appium.java_client.battery.HasBattery;
import io.appium.java_client.ios.HasIOSClipboard;
import io.appium.java_client.ios.IOSBatteryInfo;
import io.appium.java_client.ios.PerformsTouchID;
import io.appium.java_client.ios.ShakesDevice;

/**
 * Contains utility methods for working with ios<br>
 * Applicable only to the {@link io.appium.java_client.ios.IOSDriver}
 */
public interface IOSUtils extends IMobileUtils, IDriverPool {
    // todo add methods from HasIOSSettings
    // todo add methods from ListensToSyslogMessages

    /**
     * Hides the keyboard by pressing the button specified by keyName if it is showing
     *
     * @param keyName a String, representing the text displayed on the button of the keyboard you want to press. For example: "Done"
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void hideKeyboard(String keyName) {
        HidesKeyboardWithKeyName driver = null;
        try {
            driver = (HidesKeyboardWithKeyName) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support hideKeyboard method", e);
        }
        driver.hideKeyboard(keyName);
    }

    /**
     * Hides the keyboard if it is showing. Hiding the keyboard often depends
     * on the way an app is implemented, no single strategy always
     * works
     *
     * @param strategy HideKeyboardStrategy
     * @param keyName a String, representing the text displayed on the button of the keyboard you want to press. For example: "Done"
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void hideKeyboard(String strategy, String keyName) {
        HidesKeyboardWithKeyName driver = null;
        try {
            driver = (HidesKeyboardWithKeyName) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support hideKeyboard method", e);
        }
        driver.hideKeyboard(strategy, keyName);
    }

    /**
     * Shake the device
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void shake() {
        ShakesDevice driver = null;
        try {
            driver = (ShakesDevice) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support shake method", e);
        }
        driver.shake();
    }

    /**
     * Simulate touchId event.
     *
     * @param match if true, simulates a successful fingerprint scan. If false, simulates a failed fingerprint scan.
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void performTouchID(boolean match) {
        PerformsTouchID driver = null;
        try {
            driver = (PerformsTouchID) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support performTouchID method", e);
        }
        driver.performTouchID(match);
    }

    /**
     * Enrolls touchId in iOS Simulators. This call will only work if Appium process or its
     * parent application (e.g. Terminal.app or Appium.app) has
     * access to Mac OS accessibility in System Preferences &gt;
     * Security &amp; Privacy &gt; Privacy &gt; Accessibility list.
     *
     * @param enabled Whether to enable or disable Touch ID Enrollment. The actual state of the feature
     *            will only be changed if the current value is different from the previous one.
     *            Multiple calls of the method with the same argument value have no effect.
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void toggleTouchIDEnrollment(boolean enabled) {
        PerformsTouchID driver = null;
        try {
            driver = (PerformsTouchID) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support performTouchID method", e);
        }
        driver.toggleTouchIDEnrollment(enabled);
    }

    /**
     * Set an image to the clipboard
     *
     * @param img the actual image to be set
     * @throws IOException if the image cannot be decoded in PNG representation
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setClipboardImage(BufferedImage img) throws IOException {
        HasIOSClipboard driver = null;
        try {
            driver = (HasIOSClipboard) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setClipboardImage method", e);
        }
        driver.setClipboardImage(img);
    }

    /**
     * Get an image from the clipboard
     *
     * @return the actual image instance
     * @throws IOException If the returned image cannot be decoded or if the clipboard is empty
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public BufferedImage getClipboardImage() throws IOException {
        HasIOSClipboard driver = null;
        try {
            driver = (HasIOSClipboard) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getClipboardImage method", e);
        }
        return driver.getClipboardImage();
    }

    /**
     * Set an URL to the clipboard
     *
     * @param url the actual URL to set
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setClipboardUrl(URL url) {
        HasIOSClipboard driver = null;
        try {
            driver = (HasIOSClipboard) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setClipboardUrl method", e);
        }
        driver.setClipboardUrl(url);
    }

    /**
     * Get an URL from the clipboard
     *
     * @return the actual URL instance
     * @throws MalformedURLException if the URL in the clipboard is not valid or if the clipboard is empty
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public URL getClipboardUrl() throws MalformedURLException {
        HasIOSClipboard driver = null;
        try {
            driver = (HasIOSClipboard) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getClipboardUrl method", e);
        }
        return driver.getClipboardUrl();
    }

    /**
     * Retrieves battery info from the device under test
     *
     * @return BatteryInfo instance, containing the battery information
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public IOSBatteryInfo getBatteryInfo() {
        HasBattery<IOSBatteryInfo> driver = null;
        try {
            driver = (HasBattery<IOSBatteryInfo>) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getBatteryInfo method", e);
        }
        return driver.getBatteryInfo();
    }
}
