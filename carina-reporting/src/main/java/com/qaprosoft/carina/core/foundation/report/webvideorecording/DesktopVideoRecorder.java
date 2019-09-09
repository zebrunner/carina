/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.report.webvideorecording;

import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.AudioFormatKeys.EncodingKey;
import static org.monte.media.AudioFormatKeys.FrameRateKey;
import static org.monte.media.AudioFormatKeys.KeyFrameIntervalKey;
import static org.monte.media.AudioFormatKeys.MIME_AVI;
import static org.monte.media.AudioFormatKeys.MediaTypeKey;
import static org.monte.media.AudioFormatKeys.MimeTypeKey;
import static org.monte.media.FormatKeys.MediaType;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.VideoFormatKeys.MIME_QUICKTIME;

import java.awt.*;
import java.io.File;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;
import org.monte.media.AudioFormatKeys;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.report.ReportContext;

public class DesktopVideoRecorder {

    /**
     * This feature record whole desktop with script executing in video file.
     * By default in reports 'artifacts' folder.
     * 
     * Usage of Desktop video recorder
     * 
     * import com.qaprosoft.carina.core.foundation.report.webvideorecording.DesktopVideoRecorder;
     * 
     * In base test class add global variable
     * DesktopVideoRecorder recorder = new DesktopVideoRecorder();
     * 
     * in @BeforeClass add one of below functions. For ex. :
     * recorder.startVideoDesktopRecording(getDriver());
     * 
     * in @AfterClass(alwaysRun = true) do not forget to close video recording as below:
     * recorder.stopVideoDesktopRecording();
     */
    private ScreenRecorder screenRecorder;

    private static final Logger LOGGER = Logger.getLogger(DesktopVideoRecorder.class);

    public enum VideoFormat {
        AVI,
        MOV
    };

    /**
     * start Desktop Recording for full screen with default parameters
     * 
     * Requirements:
     * ScreenRecoder supports "AVI" and "QuickTime" format for recording the video.
     * For "AVI" format you need to install TSCC Codec (Techsmith Screen Capture Codec)
     * while "QuickTime" format is supported by Apple's QuickTime Player.
     * https://www.techsmith.com/download.html
     * https://1481d.wpc.azureedge.net/801481D/origin.assets.techsmith.com/Downloads/TSCC.msi
     * 
     * Folder will be from ReportContext.getArtifactsFolder()
     * fileName will start from 'TestExecuting' and date with time in format: yyyyMMdd_HH_mm_ss
     * 
     * @throws Exception java.lang.Exception
     */
    public void startDesktopScreenRecording() throws Exception {
        startDesktopScreenRecording(null, VideoFormat.AVI, false);
    }

    /**
     * start Desktop Recording for browser with default parameters
     * 
     * Requirements:
     * ScreenRecoder supports "AVI" and "QuickTime" format for recording the video.
     * For "AVI" format you need to install TSCC Codec (Techsmith Screen Capture Codec)
     * while "QuickTime" format is supported by Apple's QuickTime Player.
     * https://www.techsmith.com/download.html
     * https://1481d.wpc.azureedge.net/801481D/origin.assets.techsmith.com/Downloads/TSCC.msi
     * 
     * Folder will be from ReportContext.getArtifactsFolder()
     * fileName will start from 'TestExecuting' and date with time in format: yyyyMMdd_HH_mm_ss
     *
     * @param using_driver WebDriver
     * 
     * @throws Exception java.lang.Exception
     */
    public void startDesktopScreenRecording(WebDriver using_driver) throws Exception {
        startDesktopScreenRecording(using_driver, VideoFormat.AVI, false);
    }

    /**
     * start Desktop Recording with specified format and with audio for MOV format
     * 
     * Requirements:
     * ScreenRecoder supports "AVI" and "QuickTime" format for recording the video.
     * For "AVI" format you need to install TSCC Codec (Techsmith Screen Capture Codec)
     * while "QuickTime" format is supported by Apple's QuickTime Player.
     * https://www.techsmith.com/download.html
     * https://1481d.wpc.azureedge.net/801481D/origin.assets.techsmith.com/Downloads/TSCC.msi
     * 
     * Folder will be from ReportContext.getArtifactsFolder()
     * fileName will start from 'TestExecuting'
     *
     * @param using_driver WebDriver
     * @param videoFileFormat can be VideoFormat.AVI or VideoFormat.MOV
     * @param withSound if true will try to record sound. Works only on MOV and with a lot of noises.
     * @throws Exception java.lang.Exception
     */
    public void startDesktopScreenRecording(WebDriver using_driver, VideoFormat videoFileFormat, boolean withSound) throws Exception {
        File file = ReportContext.getArtifactsFolder();
        Rectangle captureSize;
        String fileNameBase = "TestExecuting";
        if (null == using_driver) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = screenSize.width;
            int height = screenSize.height;
            captureSize = new Rectangle(0, 0, width, height);
        } else {
            org.openqa.selenium.Dimension windowScreenSize = using_driver.manage().window().getSize();
            Point startPoint = using_driver.manage().window().getPosition();
            int width = windowScreenSize.width;
            int height = windowScreenSize.height;

            captureSize = checkBrowserSize(startPoint.x, startPoint.y, width, height);
        }

        startDesktopScreenRecording(fileNameBase, file, captureSize, videoFileFormat, withSound);
    }

    /**
     * start Desktop Recording with a lot of parameters.
     * if you have more than one monitor it will record just first one.
     * 
     * Requirements:
     * ScreenRecoder supports "AVI" and "QuickTime" format for recording the video.
     * For "AVI" format you need to install TSCC Codec (Techsmith Screen Capture Codec)
     * while "QuickTime" format is supported by Apple's QuickTime Player.
     * https://www.techsmith.com/download.html
     * https://1481d.wpc.azureedge.net/801481D/origin.assets.techsmith.com/Downloads/TSCC.msi
     *
     * @param fileNameBase String
     * @param file File
     * @param captureSize Rectangle
     * @param videoFileFormat can be VideoFormat.AVI or VideoFormat.MOV
     * @param withSound if true will try to record sound. Works only on MOV and with a lot of noises.
     * @throws Exception java.lang.Exception
     */
    public void startDesktopScreenRecording(String fileNameBase, File file, Rectangle captureSize,
            VideoFormat videoFileFormat, boolean withSound) throws Exception {

        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
        Format audio = null;

        // AVI format
        if (videoFileFormat.equals(VideoFormat.AVI)) {
            if (withSound) {

                // Not working AVI audio capabilities. Investigate if will be needed.
                /*
                 * audio = new Format(MediaTypeKey, MediaType.AUDIO,
                 * EncodingKey, ENCODING_AVI_PCM,
                 * ByteOrderKey,ByteOrder.LITTLE_ENDIAN,
                 * SignedKey,true,SampleSizeInBitsKey,24);
                 */
                /*
                 * audio = new Format(MediaTypeKey,MediaType.AUDIO,EncodingKey,ENCODING_PCM_UNSIGNED,
                 * FrameRateKey, new Rational(48000L, 1L),
                 * SampleSizeInBitsKey, 16,
                 * ChannelsKey, 2,
                 * SignedKey,false);
                 */
                /*
                 * audio = new Format(MediaTypeKey, MediaType.AUDIO,
                 * EncodingKey, ENCODING_PCM_UNSIGNED,
                 * FrameRateKey, new Rational(48000L, 1L),
                 * SampleSizeInBitsKey,8,
                 * ChannelsKey, 2,
                 * SampleRateKey, new Rational(48000L, 1L),
                 * SignedKey, Boolean.valueOf(false)
                 * //,
                 * //AudioFormatKeys.ByteOrderKey, ByteOrder.BIG_ENDIAN
                 * );
                 */

            }
            this.screenRecorder = new SpecializedScreenRecorder(gc, captureSize,
                    new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                    new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                            CompressorNameKey,
                            ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                            DepthKey, 24, FrameRateKey, Rational.valueOf(15),
                            QualityKey, 1.0f,
                            KeyFrameIntervalKey, 15 * 60),
                    new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                            FrameRateKey, Rational.valueOf(30)),
                    audio, file, fileNameBase);
        }

        // MOV format
        if (videoFileFormat.equals(VideoFormat.MOV)) {

            if (withSound) {
                audio = new Format(MediaTypeKey, MediaType.AUDIO,
                        EncodingKey,
                        ENCODING_QUICKTIME_TWOS_PCM,
                        FrameRateKey, new Rational(48000L, 1L),
                        SampleSizeInBitsKey, 16,
                        ChannelsKey, 2,
                        SampleRateKey, new Rational(48000L, 1L),
                        SignedKey, Boolean.valueOf(true),
                        AudioFormatKeys.ByteOrderKey, ByteOrder.BIG_ENDIAN);
            }

            this.screenRecorder = new SpecializedScreenRecorder(gc, captureSize,
                    new Format(MediaTypeKey, MediaType.FILE, FormatKeys.MimeTypeKey, MIME_QUICKTIME),
                    new Format(MediaTypeKey, MediaType.VIDEO,
                            EncodingKey, ENCODING_QUICKTIME_ANIMATION, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_ANIMATION,
                            DepthKey, 24, FrameRateKey, Rational.valueOf(15)),
                    new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                            FrameRateKey, Rational.valueOf(30)),
                    audio,
                    file, fileNameBase);
        }

        LOGGER.info("Start video recording. \nAbsolute path: " + file.getAbsolutePath() + " . \nStarting name is '" + fileNameBase +
                "'. \nDimensions: " + captureSize.toString());

        this.screenRecorder.start();
    }

    /**
     * start Video Desktop Recording of whole screen with default parameters
     * 
     * @return boolean true if everything is ok.
     */
    public boolean startVideoDesktopRecording() {
        return startVideoDesktopRecording(null);
    }

    /**
     * start Video Desktop Recording with default parameters only for browser OR
     * whole screen if there were problems with getting browser dimensions
     * 
     * @param using_driver
     *            WebDriver from which we should get browser dimensions.
     * @return boolean true if everything is ok.
     */

    public boolean startVideoDesktopRecording(WebDriver using_driver) {
        boolean res = false;
        try {
            if (null != using_driver) {
                if (setBrowserWindowOnTop(using_driver)) {
                    LOGGER.debug("Start Web Video recording with Browser on Top!");
                    startDesktopScreenRecording(using_driver);
                    res = true;
                } else {
                    LOGGER.error("Can't set browser focus.");
                    LOGGER.debug("Start Web Video recording of whole screen!");
                    startDesktopScreenRecording();
                    res = true;
                }
            } else {
                LOGGER.debug("Start Web Video recording of whole screen!");
                startDesktopScreenRecording();
                res = true;
            }
        } catch (Exception e) {
            LOGGER.error("Screen Recording wasn't started.");
        }
        return res;
    }

    /**
     * stop Video Desktop Recording
     *
     * @return true if there is no error.
     */
    public boolean stopVideoDesktopRecording() {
        boolean res = false;
        try {
            stopDesktopScreenRecording();
            LOGGER.info("Stop Desktop Screen Video recording!!!");
            res = true;
        } catch (Exception e) {
            LOGGER.error("Screen Recording wasn't stopped.");
        }
        return res;
    }

    /**
     * stop Web Recording
     *
     * @throws Exception java.lang.Exception
     */
    public void stopDesktopScreenRecording() throws Exception {
        this.screenRecorder.stop();
    }

    /**
     * Try to set actual browser from getDriver() on top and focused.
     * Not stable. Often browser do not switch on top and video record everything from desktop. :(
     *
     * @return boolean.
     */
    private boolean setBrowserWindowOnTop(WebDriver using_driver) {
        boolean res = false;
        try {
            using_driver.manage().window().maximize();
            String windowHandle = using_driver.getWindowHandle();
            using_driver.switchTo().window(windowHandle);
            JavascriptExecutor js = (JavascriptExecutor) using_driver;
            js.executeScript("window.focus();");
            res = true;
        } catch (Exception e) {
            LOGGER.error("Exception during set focus to browser window: " + e);

        }
        return res;
    }

    /**
     * check Browser Size. If it too small capture full screen.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return Rectangle
     */
    private Rectangle checkBrowserSize(int x, int y, int width, int height) {
        int res_x = 0, res_y = 0;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int res_width = screenSize.width;
        int res_height = screenSize.height;
        if (x >= 0)
            res_x = x;
        if (y >= 0)
            res_y = y;

        if ((res_width - width) <= width)
            res_width = width;
        if ((res_height - height) <= height)
            res_height = height;

        Rectangle ret = new Rectangle(res_x, res_y, res_width, res_height);
        LOGGER.debug("Got browser parameters:" + x + ", " + y + ", " + width + ", " + height + " \n Capture in rectangle:" + ret.toString());
        return ret;

    }
}
