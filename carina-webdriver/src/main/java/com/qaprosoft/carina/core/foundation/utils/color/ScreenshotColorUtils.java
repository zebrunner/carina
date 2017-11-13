package com.qaprosoft.carina.core.foundation.utils.color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.qaprosoft.carina.core.foundation.color.ColorName;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;

//TODO check methods on Android devices, Android simulator, iOS devices (currently tested only on iOS simulator)
public class ScreenshotColorUtils {

    protected static final Logger LOGGER = Logger.getLogger(ScreenshotColorUtils.class);

    /**
     * Compare color of the same point on the screenshot and on the device
     * screen
     * 
     * @param path - path to the test file
     * @param x - x-cooordinate of point
     * @param y - y-cooordinate of point
     * @return boolean
     */
    public static boolean compareColorWithColorOnExapleByCoordinates(String path, int x, int y) {
        File testScreenShot = new File(path);
        BufferedImage image = null;
        try {
            image = ImageIO.read(testScreenShot);
        } catch (IOException e) {
            LOGGER.info("Image can't be read! " + e.getStackTrace());
        }
        ColorName colorFromExample = getColorFromImageByCoordinates(x, y, image);
        ColorName colorFromDevice = getColorFromDeviceScreenByCoordinates(x, y);
        if (colorFromExample.equals(colorFromDevice))
            return true;
        return false;
    }

    private static int[] getCenterOfElement(WebElement element) {
        int x = element.getLocation().getX() + element.getSize().getWidth() / 2;
        int y = element.getLocation().getY() + element.getSize().getHeight() / 2;
        return new int[] { x, y };
    }

    /**
     * Compare color of the same point (center of the element) on the screenshot
     * and on the device screen
     * 
     * @param path - path to the test file
     * @param element - color of element defined as color of it's center
     * @param isSaveScreenshot
     * @return
     */
    public static boolean compareColorOfElementWithExample(String path, WebElement element) {
        int[] coordinates = getCenterOfElement(element);
        return compareColorWithColorOnExapleByCoordinates(path, coordinates[0], coordinates[1]);
    }

    /**
     * 
     * @param x- x-coordinate of point
     * @param y - y-coordinate of point
     * @return color of pixel with coordinates (x,y)
     */
    public static ColorName getColorFromDeviceScreenByCoordinates(int x, int y) {
        BufferedImage image = null;
        try {
            image = Screenshot.takeScreenshotForColorComparation(DriverPool.getDriver(), "");
        } catch (IOException e) {
            LOGGER.info("Screenshot can't be done!");
        }
        return getColorFromImageByCoordinates(x, y, image);

    }

    /**
     * Get color from image using coordinates (x,y) of point
     * 
     * @param x x-coordinate of point
     * @param y y-coordinate of point
     * @param image - image to work with
     * @return ColorName object (name, r,g,b - values)
     */
    public static ColorName getColorFromImageByCoordinates(int x, int y, BufferedImage image) {
        Dimension dimensions = DriverPool.getDriver().manage().window().getSize();
        int screenWidth = dimensions.getWidth();
        int screenHeight = dimensions.getHeight();

        LOGGER.info("Image size: width - " + image.getWidth() + ", height - " + image.getHeight());
        LOGGER.info("Device screen size: width - " + screenWidth + ", height - " + screenHeight);

        Double xCoeff = new Double(image.getWidth() / screenWidth);
        Double yCoeff = new Double(image.getHeight() / screenHeight);

        LOGGER.info("xCoeff: " + xCoeff + ", yCoeff " + yCoeff);

        int colorValue = image.getRGB(new Double(x * xCoeff).intValue(), new Double(y * yCoeff).intValue());

        ColorName color = ColorUtils.getColorFromHex(colorValue);
        LOGGER.info("Color is " + color.getName());
        return color;
    }

    /**
     * 
     * @param element
     * @param isSaveScreenshot
     * @return ColorName object (name, r,g,b-values)
     *
     */
    public static ColorName getColorOfTheElement(WebElement element) {
        int[] coordinates = getCenterOfElement(element);
        return getColorFromDeviceScreenByCoordinates(coordinates[0], coordinates[1]);
    }

}
