/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zebrunner.carina.webdriver.screenshot;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.webdriver.Screenshot;
import com.zebrunner.carina.webdriver.augmenter.DriverAugmenter;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

/**
 * Captures web screenshots.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 * @deprecated use {@link Screenshot} instead
 */
@Deprecated(forRemoval = true, since = "8.0.5")
public class Screen implements ICapturable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private WebDriver driver;

    private BufferedImage screenshot;

    private Screen(WebDriver driver) {
        this.driver = driver;
    }

    public static ICapturable getInstance(WebDriver driver) {
        return new Screen(new DriverAugmenter().augment(driver));
    }

    @Override
    public ICapturable capture(ScreenArea area) {
        try {
            switch (area) {
            case VISIBLE_SCREEN:
                screenshot = new AShot().takeScreenshot(driver).getImage();
                break;

            case FULL_SCREEN:
                screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(100)).takeScreenshot(driver).getImage();
                break;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to capture screenshot: " + e.getMessage());
        }
        return this;
    }

    @Override
    public ICapturable highlight(Point point) {
        try {
            Graphics2D g2d = screenshot.createGraphics();
            g2d.setColor(Color.red);
            g2d.draw(new Ellipse2D.Double(point.getX(), point.getY(), 100, 100));
        } catch (Exception e) {
            LOGGER.error("Unable to highligh screenshot: " + e.getMessage());
        }
        return this;
    }

    @Override
    public ICapturable highlight(Rectangle rect) {
        try {
            Graphics2D g2d = screenshot.createGraphics();
            g2d.setColor(Color.red);
            g2d.drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        } catch (Exception e) {
            LOGGER.error("Unable to highligh screenshot: " + e.getMessage());
        }
        return this;
    }

    @Override
    public ICapturable comment(String comment) {
        try {
            Graphics2D g2d = screenshot.createGraphics();
            g2d.setColor(Color.red);
            g2d.setFont(new Font("Arial", Font.PLAIN, 30));
            g2d.drawString(comment, 20, screenshot.getHeight() - 20);
        } catch (Exception e) {
            LOGGER.error("Unable to comment screenshot: " + e.getMessage());
        }
        return this;
    }

    @Override
    public BufferedImage getImage() {
        return screenshot;
    }
}