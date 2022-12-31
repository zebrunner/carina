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
package com.qaprosoft.carina.core.foundation.webdriver.screenshot;

import java.nio.file.Path;
import java.time.Duration;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openqa.selenium.Beta;

import com.qaprosoft.carina.core.foundation.webdriver.ScreenshotType;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.report.ReportContext;

public interface IScreenshotRule {

    public abstract ScreenshotType getScreenshotType();

    public abstract boolean isTakeScreenshot();

    public abstract boolean isAllowFullSize();

    /**
     * Get image resize dimensions
     * 
     * @return {@link ImmutablePair}, left - width, right - height
     */
    default ImmutablePair<Integer, Integer> getImageResizeDimensions() {
        return new ImmutablePair<>(Configuration.getInt(Configuration.Parameter.BIG_SCREEN_WIDTH),
                Configuration.getInt(Configuration.Parameter.BIG_SCREEN_HEIGHT));
    }

    /**
     * Path where to save screenshot
     * 
     * @return {@link Path} to the folder
     */
    default Path getSaveFolder() {
        return Path.of(ReportContext.getTestDir().getAbsolutePath());
    }

    /**
     * Get screenshot filename. The return name must be unique for the test run
     * 
     * @return screenshot filename
     */
    default String getFilename() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Get screenshot file extension
     * 
     * @return image file extension
     */
    @Beta
    default String getFileExtension() {
        return "png";
    }

    default Duration getTimeout() {
        int divider = isAllowFullSize() ? 2 : 3;
        return Duration.ofSeconds(Configuration.getInt(Configuration.Parameter.EXPLICIT_TIMEOUT) / divider);
    }

    /**
     * If need to check rules when capture screenshot<br>
     * Method that validate rules: com.qaprosoft.carina.core.foundation.webdriver.Screenshot#validateRule(IScreenshotRule)
     * 
     * @return true if need, false otherwise
     */
    @Beta
    default boolean isEnableValidation() {
        return "DEBUG".equalsIgnoreCase(Configuration.get(Configuration.Parameter.CORE_LOG_LEVEL));
    }
}
