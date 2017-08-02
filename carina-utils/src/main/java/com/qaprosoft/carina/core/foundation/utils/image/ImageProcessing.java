package com.qaprosoft.carina.core.foundation.utils.image;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Class for adding useful utils for Image processing
 */
public class ImageProcessing {

    protected static final Logger LOGGER = Logger.getLogger(ImageProcessing.class);


    /**
     * Image Resize tool for screenshots
     * Screenshot should be saved as OutputType.BYTES
     * Example:
     * byte[] screenshot=((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
     * <p>
     * percent - <code>int</code> Percent of Scaling from default image. 100 - same size.
     * Will be taken from config.properties or set as default 30 (%) - will be 0.3 of actual image size
     *
     * @param fileData -<code> byte[] </code> - ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
     * @return <code>byte[]</code> - default file format is PNG
     */
    public static byte[] imageResize(byte[] fileData) {
        int percent = 30;

        if (!Configuration.isNull(Configuration.Parameter.CUCUMBER_TESTS_RESULTS_IMAGE_RESIZE)) {
            if (!Configuration.get(Configuration.Parameter.CUCUMBER_TESTS_RESULTS_IMAGE_RESIZE).isEmpty()) {
                try {
                    percent = Integer.parseInt(Configuration.get(Configuration.Parameter.CUCUMBER_TESTS_RESULTS_IMAGE_RESIZE));
                } catch (Exception e) {
                    LOGGER.error("Error in parsing to int config parameter CUCUMBER_TESTS_RESULTS_IMAGE_RESIZE");
                }
            }
        }

        return imageResize(fileData, percent);
    }

    /**
     * Image Resize tool for screenshots
     * Screenshot should be saved as OutputType.BYTES
     * Example:
     * byte[] screenshot=((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
     *
     * @param fileData -<code> byte[] </code> - ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
     * @param percent  - <code>int</code> Percent of Scaling from default image. 100 - same size.
     * @return <code>byte[]</code> - default file format is PNG
     */
    public static byte[] imageResize(byte[] fileData, int percent) {
        ByteArrayInputStream in = new ByteArrayInputStream(fileData);
        try {
            BufferedImage img = ImageIO.read(in);

            double prop = (double) percent / 100;

            LOGGER.debug("Input image: img.getHeight()=" + img.getHeight() + " and " +
                    "img.getWidth()=" + img.getWidth() + ", proportion is " + prop);

            if ((img.getHeight() == 0) || (img.getWidth() == 0)) {
                LOGGER.error("Input image is not correct.");
                throw new Exception("Input image is not correct. img.getHeight()=" + img.getHeight() + " and " +
                        "img.getWidth()=" + img.getWidth() + ", prop is " + prop);
            }

            int height = (int) (prop * img.getHeight());
            int width = (int) (prop * img.getWidth());
            Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0, 0, 0), null);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            ImageIO.write(imageBuff, "png", buffer);

            return buffer.toByteArray();
        } catch (IOException e) {
            LOGGER.error("IOException in scale");
            return fileData;
        } catch (Exception e) {
            LOGGER.error("Exception in scale: " + e.getMessage());
            return fileData;
        }

    }

    /**
     * Image Resize tool for screenshots
     * Screenshot should be saved as OutputType.BYTES
     * Example:
     * byte[] screenshot=((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
     *
     * @param fileData   -<code> byte[] </code> - ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
     * @param width      - <code> int </code> new width (if 0 - will be scaled proportionally to height)
     * @param height     - <code> int </code> new height (if 0 - will be scaled proportionally to width)
     * @param formatName - <code> String </code> file output format can be
     * @return <code>byte[]</code> - return resized image in required format
     */
    public static byte[] imageResize(byte[] fileData, int width, int height, String formatName) {
        ByteArrayInputStream in = new ByteArrayInputStream(fileData);
        try {
            BufferedImage img = ImageIO.read(in);

            if (height == 0) {
                height = (width * img.getHeight()) / img.getWidth();
            }
            if (width == 0) {
                width = (height * img.getWidth()) / img.getHeight();
            }
            Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0, 0, 0), null);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            String[] formatVariants = {"png", "jpg", "gif", "bmp"};
            if ((formatName.isEmpty()) || (!Arrays.asList(formatVariants).contains(formatName.toLowerCase())))
                formatName = "png";

            ImageIO.write(imageBuff, formatName.toLowerCase(), buffer);

            return buffer.toByteArray();
        } catch (IOException e) {
            LOGGER.error("IOException in scale");
            return fileData;
        }
    }

}
