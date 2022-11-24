package com.zebrunner.carina.utils.ios;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.zebrunner.carina.utils.mobile.IMobileUtils;

import io.appium.java_client.HidesKeyboardWithKeyName;
import io.appium.java_client.battery.HasBattery;
import io.appium.java_client.ios.HasIOSClipboard;
import io.appium.java_client.ios.HasIOSSettings;
import io.appium.java_client.ios.IOSBatteryInfo;
import io.appium.java_client.ios.PerformsTouchID;
import io.appium.java_client.ios.ShakesDevice;

/**
 * Contains utility methods for working with ios
 */
public interface IOSUtils extends IMobileUtils {
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

    /**
     * Set the `nativeWebTap` setting. *iOS-only method*.
     * Sets whether Safari/webviews should convert element taps into x/y taps.
     *
     * @param enabled turns nativeWebTap on if true, off if false.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings nativeWebTap(Boolean enabled) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support nativeWebTap method", e);
        }
        return driver.nativeWebTap(enabled);
    }

    /**
     * Whether to return compact (standards-compliant) and faster responses from find element/s
     * (the default setting). If set to false then the response may also contain other
     * available element attributes.
     *
     * @param enabled either true or false. The default value if true.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings setShouldUseCompactResponses(boolean enabled) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setShouldUseCompactResponses method", e);
        }
        return driver.setShouldUseCompactResponses(enabled);
    }

    /**
     * Which attributes should be returned if compact responses are disabled.
     * It works only if shouldUseCompactResponses is set to false. Defaults to "type,label" string.
     *
     * @param attrNames the comma-separated list of fields to return with each element.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings setElementResponseAttributes(String attrNames) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setElementResponseAttributes method", e);
        }
        return driver.setElementResponseAttributes(attrNames);
    }

    /**
     * The quality of the screenshots generated by the screenshots broadcaster,
     * The value of 0 represents the maximum compression
     * (or lowest quality) while the value of 100 represents the least compression (or best quality).
     *
     * @param quality an integer in range 0..100. The default value is 25.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings setMjpegServerScreenshotQuality(int quality) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setMjpegServerScreenshotQuality method", e);
        }
        return driver.setMjpegServerScreenshotQuality(quality);
    }

    /**
     * The frame rate at which the background screenshots broadcaster should broadcast screenshots in range 1..60.
     * The default value is 10 (Frames Per Second).
     * Setting zero value will cause the frame rate to be at its maximum possible value.
     *
     * @param framerate an integer in range 1..60. The default value is 10.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings setMjpegServerFramerate(int framerate) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setMjpegServerFramerate method", e);
        }
        return driver.setMjpegServerFramerate(framerate);
    }

    /**
     * Changes the quality of phone display screenshots according to XCTest/XCTImageQuality enum.
     * Sometimes setting this value to the maximum possible quality may crash XCTest because of
     * lack of the memory (lossless screenshot require more space).
     *
     * @param quality an integer in range 0..2. The default value is 1.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings setScreenshotQuality(int quality) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setScreenshotQuality method", e);
        }
        return driver.setScreenshotQuality(quality);
    }

    /**
     * The scale of screenshots in range 1..100.
     * The default value is 100, no scaling
     *
     * @param scale an integer in range 1..100. The default value is 100.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings setMjpegScalingFactor(int scale) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setMjpegScalingFactor method", e);
        }
        return driver.setMjpegScalingFactor(scale);
    }

    /**
     * Changes the 'Auto-Correction' preference in Keyboards setting.
     *
     * @param enabled Either true or false. Defaults to false when WDA starts as xctest.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings setKeyboardAutocorrection(boolean enabled) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setKeyboardAutocorrection method", e);
        }
        return driver.setKeyboardAutocorrection(enabled);
    }

    /**
     * Changes the 'Predictive' preference in Keyboards setting.
     *
     * @param enabled either true or false. Defaults to false when WDA starts as xctest.
     * @return {@link HasIOSSettings} instance for chaining.
     */
    public default HasIOSSettings setKeyboardPrediction(boolean enabled) {
        HasIOSSettings driver = null;
        try {
            driver = (HasIOSSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setKeyboardPrediction method", e);
        }
        return driver.setKeyboardPrediction(enabled);
    }
}
