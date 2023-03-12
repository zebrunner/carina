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

import java.awt.image.BufferedImage;

import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;

import com.zebrunner.carina.webdriver.Screenshot;

/**
 * Interface represents base actions for screenshots capturing.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 * @deprecated use {@link Screenshot} instead
 */
@Deprecated(forRemoval = true, since = "8.0.5")
public interface ICapturable {
    enum ScreenArea {
        FULL_SCREEN,
        VISIBLE_SCREEN
    };

    /**
     * Captures screenshot.
     * 
     * @param area - screen area to capture
     * @return screenshot file
     */
    ICapturable capture(ScreenArea area);

    /**
     * Highlights dot on screenshot.
     * 
     * @param point - of region to highlight
     * @return {@link ICapturable} instance
     */
    ICapturable highlight(Point point);

    /**
     * Highlights rectangle on screenshot.
     * 
     * @param rect - of region to highlight
     * @return {@link ICapturable} instance
     */
    ICapturable highlight(Rectangle rect);

    /**
     * Adds comment to screenshot.
     * 
     * @param comment - for screenshot
     * @return {@link ICapturable} instance
     */
    ICapturable comment(String comment);

    /**
     * Returns {@link BufferedImage} instance of captured screenshot.
     * 
     * @return {@link ICapturable} instance
     */
    BufferedImage getImage();
}