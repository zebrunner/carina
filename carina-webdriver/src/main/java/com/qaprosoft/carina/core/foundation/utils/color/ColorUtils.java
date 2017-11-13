package com.qaprosoft.carina.core.foundation.utils.color;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.color.ColorList;
import com.qaprosoft.carina.core.foundation.color.ColorName;
import com.qaprosoft.carina.core.foundation.utils.marshaller.MarshallerHelper;

public class ColorUtils {

    private static String COLOR_XML = "color.xml";
    public static ArrayList<ColorName> colorNames = new ArrayList<ColorName>();
    protected static final Logger LOGGER = Logger.getLogger(ColorUtils.class);

    private static void initList() {
        if (colorNames.size() == 0) {
            InputStream in = ColorUtils.class.getResourceAsStream(COLOR_XML);
            File file = null;
            file = stream2file(in);
            colorNames = ((ColorList) MarshallerHelper.unmarshall(file, ColorList.class)).getListOfColors();
        }
    }

    private static File stream2file(InputStream in) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("tempfile", "xml");
            tempFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);
        } catch (IOException e) {
            LOGGER.info("Can't transform inputStream to file");
        }
        return tempFile;
    }

    /**
     * Get the closest color name from list
     * 
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static ColorName getColorFromRgb(int r, int g, int b) {
        initList();
        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorNames) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            return closestMatch;
        } else {
            throw new RuntimeException("No matched color is found!");
        }
    }

    /**
     * Convert hexColor to rgb, then call getColorFromRgb(r, g, b)
     * 
     * @param hexColor
     * @return ColorName object(contains name and r,g,b-values)
     */
    public static ColorName getColorFromHex(int hexColor) {
        int r = (hexColor & 0xFF0000) >> 16;
        int g = (hexColor & 0xFF00) >> 8;
        int b = (hexColor & 0xFF);
        return getColorFromRgb(r, g, b);
    }

    /**
     * Convert color to hex value
     * 
     * @param color
     * @return hex value
     */
    public static int colorToHex(Color color) {
        return Integer.decode("0x" + Integer.toHexString(color.getRGB()).substring(2));
    }

    /**
     * Get color name from Color object
     * 
     * @param color
     * @return color name
     */
    public static String getColorNameFromColor(Color color) {
        return getColorFromRgb(color.getRed(), color.getGreen(), color.getBlue()).getName();
    }

    /**
     * 
     * 
     * @param name
     * @return ColorName object
     */
    public static ColorName getColorByName(String name) {
        initList();
        ColorName colorName = new ColorName("No matching", -1, -1, -1);
        for (ColorName color : colorNames) {
            if (color.getName().equalsIgnoreCase(name)) {
                return color;
            }
        }
        return colorName;
    }

}
