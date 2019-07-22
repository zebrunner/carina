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
package com.qaprosoft.carina.core.foundation.report;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.log.ThreadLogAppender;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.FileManager;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.ZipManager;

/*
 * Be careful with LOGGER usage here because potentially it could do recursive call together with ThreadLogAppender functionality
 */

public class ReportContext {
    private static final Logger LOGGER = Logger.getLogger(ReportContext.class);

    public static final String ARTIFACTS_FOLDER = "artifacts";
    
    private static final String GALLERY_ZIP = "gallery-lib.zip";
    private static final String REPORT_NAME = "/report.html";
    private static final int MAX_IMAGE_TITLE = 300;
    private static final String TITLE = "Test steps demo";

    public static final String TEMP_FOLDER = "temp";

    private static File baseDirectory = null;

    private static File tempDirectory;

    private static File artifactsDirectory;

    private static File metaDataDirectory;

    private static long rootID;

    private static final ThreadLocal<File> testDirectory = new ThreadLocal<File>();
    private static final ThreadLocal<Boolean> isCustomTestDirName = new ThreadLocal<Boolean>();

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    
    // Collects screenshot comments. Screenshot comments are associated using screenshot file name.
    private static Map<String, String> screenSteps = Collections.synchronizedMap(new HashMap<String, String>());

    public static long getRootID() {
        return rootID;
    }

    /**
     * Crates new screenshot directory at first call otherwise returns created directory. Directory is specific for any
     * new test suite launch.
     * 
     * @return root screenshot folder for test launch.
     */
    public static synchronized File getBaseDir() {
        try {
            if (baseDirectory == null) {
                removeOldReports();
                File projectRoot = new File(String.format("%s/%s", URLDecoder.decode(System.getProperty("user.dir"), "utf-8"),
                        Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY)));
                if (!projectRoot.exists()) {
                    boolean isCreated = projectRoot.mkdirs();
                    if (!isCreated) {
                        throw new RuntimeException("Folder not created: " + projectRoot.getAbsolutePath());
                    }
                }
                rootID = System.currentTimeMillis();
                String directory = String.format("%s/%s/%d", URLDecoder.decode(System.getProperty("user.dir"), "utf-8"),
                        Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), rootID);
                File baseDirectoryTmp = new File(directory);
                boolean isCreated = baseDirectoryTmp.mkdir();
                if (!isCreated) {
                    throw new RuntimeException("Folder not created: " + baseDirectory.getAbsolutePath());
                }

                baseDirectory = baseDirectoryTmp;
                
                copyGalleryLib();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Folder not created: " + baseDirectory.getAbsolutePath());
        }
        return baseDirectory;
    }

    public static boolean isBaseDirCreated() {
        return baseDirectory != null;
    }

    public static synchronized File getTempDir() {
        if (tempDirectory == null) {
            tempDirectory = new File(String.format("%s/%s", getBaseDir().getAbsolutePath(), TEMP_FOLDER));
            boolean isCreated = tempDirectory.mkdir();
            if (!isCreated) {
                throw new RuntimeException("Folder not created: " + tempDirectory.getAbsolutePath());
            }
        }
        return tempDirectory;
    }

    public static synchronized void removeTempDir() {
        if (tempDirectory != null) {
            try {
                FileUtils.deleteDirectory(tempDirectory);
            } catch (IOException e) {
                LOGGER.debug("Unable to remove artifacts temp directory!", e);
            }
        }
    }

    public static synchronized File getArtifactsFolder() {
        if (artifactsDirectory == null) {
        	String absolutePath = getBaseDir().getAbsolutePath();
        	
            try {
            	if (Configuration.get(Parameter.CUSTOM_ARTIFACTS_FOLDER).isEmpty()) {
            		artifactsDirectory = new File(String.format("%s/%s", URLDecoder.decode(absolutePath, "utf-8"), ARTIFACTS_FOLDER));
            	} else {
            		artifactsDirectory = new File(Configuration.get(Parameter.CUSTOM_ARTIFACTS_FOLDER));
            	}
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Artifacts folder not created in base dir: " + absolutePath);
            }
            
            boolean isCreated = artifactsDirectory.exists() && artifactsDirectory.isDirectory();
            if (!isCreated) {
            		isCreated = artifactsDirectory.mkdir();
            } else {
            	LOGGER.info("Artifacts folder already exists: "  + artifactsDirectory.getAbsolutePath());
            }
            
            if (!isCreated) {
                throw new RuntimeException("Artifacts folder not created: " + artifactsDirectory.getAbsolutePath());
            }
        }
        return artifactsDirectory;
    }

    public static synchronized File getMetadataFolder() {
        if (metaDataDirectory == null) {
        	String absolutePath = getBaseDir().getAbsolutePath();
            try {
                metaDataDirectory = new File(String.format("%s/%s/metadata", URLDecoder.decode(absolutePath, "utf-8"), ARTIFACTS_FOLDER));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Artifacts metadata folder is not created in base dir: " + absolutePath);
            }
            boolean isCreated = metaDataDirectory.mkdir();
            if (!isCreated) {
                throw new RuntimeException("Artifacts metadata folder is not created in base dir: " + absolutePath);
            }
        }
        return metaDataDirectory;
    }

    /**
     * Check that Artifacts Folder exists.
     * 
     * @return boolean
     */
    public static boolean isArtifactsFolderExists() {
        try {
            File f = new File(String.format("%s/%s", getBaseDir().getAbsolutePath(), ARTIFACTS_FOLDER));
            if (f.exists() && f.isDirectory()) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.debug("Error happen during checking that Artifactory Folder exists or not. Error: " + e.getMessage());
        }
        return false;
    }

    public static List<File> getAllArtifacts() {
        return Arrays.asList(getArtifactsFolder().listFiles());
    }

    public static File getArtifact(String name) {
        File artifact = null;
        for (File file : getAllArtifacts()) {
            if (file.getName().equals(name)) {
                artifact = file;
                break;
            }
        }
        return artifact;
    }

    public static void deleteAllArtifacts() {
        for (File file : getAllArtifacts()) {
            file.delete();
        }
    }

    public static void deleteArtifact(String name) {
        for (File file : getAllArtifacts()) {
            if (file.getName().equals(name)) {
                file.delete();
                break;
            }
        }
    }

    public static void saveArtifact(String name, InputStream source) throws IOException {
        File artifact = new File(String.format("%s/%s", getArtifactsFolder(), name));
        artifact.createNewFile();
        FileUtils.writeByteArrayToFile(artifact, IOUtils.toByteArray(source));
    }

    public static void saveArtifact(File source) throws IOException {
        File artifact = new File(String.format("%s/%s", getArtifactsFolder(), source.getName()));
        artifact.createNewFile();
        FileUtils.copyFile(source, artifact);
    }

    /**
     * Creates new test directory at first call otherwise returns created directory. Directory is specific for any new
     * test launch.
     * 
     * @return test log/screenshot folder.
     */
    public static File getTestDir() {
        return getTestDir(StringUtils.EMPTY);
    }
    
    public static File getTestDir(String dirName) {
        File testDir = testDirectory.get();
        if (testDir == null) {
            String uniqueDirName = UUID.randomUUID().toString();
            if (!dirName.isEmpty()) {
                uniqueDirName = dirName;
            }
            String directory = String.format("%s/%s", getBaseDir(), uniqueDirName);
            // System.out.println("First request for test dir. Just generate unique folder: " + directory);

            testDir = new File(directory);
            File thumbDir = new File(testDir.getAbsolutePath() + "/thumbnails");

            if (!thumbDir.mkdirs()) {
                throw new RuntimeException("Test Folder(s) not created: " + testDir.getAbsolutePath() + " and/or " + thumbDir.getAbsolutePath());
            }
        }

        testDirectory.set(testDir);
        return testDir;
    }
    
    /**
     * Rename test directory from unique number to custom name.
     * 
     * @param dirName
     * 
     * @return test report dir
     */
    public synchronized static File setCustomTestDirName(String dirName) {
        isCustomTestDirName.set(Boolean.FALSE);
        File testDir = testDirectory.get();
        if(testDir == null) {
            LOGGER.debug("Test dir will be created.");
            testDir = getTestDir(dirName);
        } else {
            LOGGER.debug("Test dir will be renamed to custom name.");
            renameTestDir(dirName);
        }
        isCustomTestDirName.set(Boolean.TRUE);
        return testDir;
    }
    
    public static void emptyTestDirData() {
        LOGGER.debug("testDir and isCustomTestDirName variables will be empty.");
        testDirectory.remove();
        isCustomTestDirName.set(Boolean.FALSE);
        closeThreadLogAppender();
    }
    
    private static void closeThreadLogAppender() {
        try {
            ThreadLogAppender tla = (ThreadLogAppender) Logger.getRootLogger().getAppender("ThreadLogAppender");
            if (tla != null) {
                tla.close();
            }

        } catch (NoSuchMethodError e) {
            LOGGER.error("Exception while closing thread log appender.");
        }
    }

    public static File renameTestDir(String test) {
        File testDir = testDirectory.get();
        initIsCustomTestDir();
        if (testDir != null && !isCustomTestDirName.get()) {
            File newTestDir = new File(String.format("%s/%s", getBaseDir(), test.replaceAll("[^a-zA-Z0-9.-]", "_")));

            if (!newTestDir.exists()) {
                // close ThreadLogAppender resources before renaming
                closeThreadLogAppender();
                testDir.renameTo(newTestDir);
                testDirectory.set(newTestDir);    
                LOGGER.debug("Test directory is set to : " + newTestDir);
            }
        } else {
            LOGGER.error("Unexpected case with absence of test.log for '" + test + "'");
        }
        
        return testDir;
    }
    
    private static void initIsCustomTestDir() {
        if (isCustomTestDirName.get() == null) {
            isCustomTestDirName.set(Boolean.FALSE);
        };
    }

    /**
     * Removes emailable html report and oldest screenshots directories according to history size defined in config.
     */
    private static void removeOldReports() {
        File baseDir = new File(String.format("%s/%s", System.getProperty("user.dir"),
                Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY)));

        if (baseDir.exists()) {
            // remove old emailable report
            File reportFile = new File(String.format("%s/%s/%s", System.getProperty("user.dir"),
                    Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), SpecialKeywords.HTML_REPORT));
            if (reportFile.exists()) {
                reportFile.delete();
            }

            List<File> files = FileManager.getFilesInDir(baseDir);
            List<File> screenshotFolders = new ArrayList<File>();
            for (File file : files) {
                if (file.isDirectory() && !file.getName().startsWith(".")) {
                    screenshotFolders.add(file);
                }
            }

            int maxHistory = Configuration.getInt(Parameter.MAX_SCREENSHOOT_HISTORY);

            if (maxHistory > 0 && screenshotFolders.size() + 1 > maxHistory && maxHistory != 0) {
                Comparator<File> comp = new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {
                        return file2.getName().compareTo(file1.getName());
                    }
                };
                Collections.sort(screenshotFolders, comp);
                for (int i = maxHistory - 1; i < screenshotFolders.size(); i++) {
                    if (screenshotFolders.get(i).getName().equals("gallery-lib")) {
                        continue;
                    }
                    try {
                        FileUtils.deleteDirectory(screenshotFolders.get(i));
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public static void generateHtmlReport(String content) {
    	String emailableReport = SpecialKeywords.HTML_REPORT;
    	
        try {
            File reportFile = new File(String.format("%s/%s/%s", System.getProperty("user.dir"),
                    Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), emailableReport));

            // if file doesn't exists, then create it
            if (!reportFile.exists()) {
                reportFile.createNewFile();
            }

            FileWriter fw = new FileWriter(reportFile.getAbsoluteFile());
            try {
                BufferedWriter bw = new BufferedWriter(fw);
                try {
                    bw.write(content);
                } finally {
					bw.close();
                }
            } finally {
				fw.close();
            }

        } catch (IOException e) {
            LOGGER.error("generateHtmlReport failure", e);
        }
    }

    /**
     * Returns URL for test artifacts folder.
     * 
     * @return - URL for test screenshot folder.
     */
    public static String getTestArtifactsLink() {
        String link = "";
        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            // remove report url and make link relative
            // link = String.format("./%d/%s/report.html", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
            link = String.format("%s/%d/artifacts", Configuration.get(Parameter.REPORT_URL), rootID);
        } else {
            link = String.format("file://%s/%d/artifacts", baseDirectory, rootID);
        }

        return link;

    }

    /**
     * Returns URL for test screenshot folder.
     * 
     * @param test
     *            test name
     * @return - URL for test screenshot folder.
     */
    public static String getTestScreenshotsLink(String test) {
        // TODO: find unified solution for screenshots presence determination. Combine it with
        // AbstractTestListener->createTestResult code
        String link = "";
        if (FileUtils.listFiles(ReportContext.getTestDir(), new String[] { "png" }, false).isEmpty()) {
            // no png screenshot files at all
            return link;
        }

        // TODO: remove reference using "String test" argument
        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            // remove report url and make link relative
            // link = String.format("./%d/%s/report.html", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
            link = String.format("%s/%d/%s/report.html", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
        } else {
            // TODO: it seems like defect
            link = String.format("file://%s/%s/report.html", baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
        }

        return link;

    }

    /**
     * Returns URL for test log.
     * 
     * @param test
     *            test name
     * @return - URL to test log folder.
     */
    // TODO: refactor removing "test" argument
    public static String getTestLogLink(String test) {
        String link = "";
        File testLogFile = new File(ReportContext.getTestDir() + "/" + "test.log");
        if (!testLogFile.exists()) {
            // no test.log file at all
            return link;
        }

        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            // remove report url and make link relative
            // link = String.format("./%d/%s/test.log", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
            link = String.format("%s/%d/%s/test.log", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
        } else {
            // TODO: it seems like defect
            link = String.format("file://%s/%s/test.log", baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
        }

        return link;
    }
    
//    TODO: refactor as soon as getLogLink will be updated
    public static String getSysLogLink(String test) {
        String link = "";
        File testLogFile = new File(ReportContext.getTestDir() + "/" + "logcat.log");
        if (!testLogFile.exists()) {
            // no test.log file at all
            return link;
        }

        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            link = String.format("%s/%d/%s/logcat.log", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
        } else {
            link = String.format("file://%s/%s/logcat.log", baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
        }
        LOGGER.debug("Extracted syslog link: ".concat(link));
        return link;
    }
    
    // TODO: refactor as soon as getLogLink will be updated
    public static String getUIxLink(String test, String uixFileName) {
        String link = "";
        File testLogFile = new File(ReportContext.getTestDir() + "/" + uixFileName);
        if (!testLogFile.exists()) {
            // no test.log file at all
            return link;
        }

        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            link = String.format("%s/%d/%s/".concat(uixFileName), Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
        } else {
            link = String.format("file://%s/%s/".concat(uixFileName), baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
        }
        LOGGER.info("Extracted uix link: ".concat(link));
        return link;
    }

    /**
     * Returns URL for cucumber report.
     * @return - URL to test log folder.
     */
    public static String getCucumberReportLink() {

        String folder = SpecialKeywords.CUCUMBER_REPORT_FOLDER;
        String subFolder = SpecialKeywords.CUCUMBER_REPORT_SUBFOLDER;
        
        String link = "";
        // String subfolder = "cucumber-html-reports";
        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            // remove report url and make link relative
            // link = String.format("./%d/report.html", rootID);
            String report_url = Configuration.get(Parameter.REPORT_URL);
            if (report_url.contains("n/a")) {
                LOGGER.error("Contains n/a. Replace it.");
                report_url = report_url.replace("n/a", "");
            }
            link = String.format("%s/%d/%s/%s/%s/feature-overview.html", report_url, rootID, ARTIFACTS_FOLDER, folder, subFolder);
        } else {
            link = String.format("file://%s/%s/%s/feature-overview.html", artifactsDirectory, folder, subFolder);
        }

        return link;
    }

    /**
     * Saves screenshot with thumbnail.
     * 
     * @param screenshot - {@link BufferedImage} file to save
     */
    public static String saveScreenshot(BufferedImage screenshot) {
        long now = System.currentTimeMillis();

        executor.execute(new ImageSaverTask(screenshot, String.format("%s/%d.png", getTestDir().getAbsolutePath(), now),
                Configuration.getInt(Parameter.BIG_SCREEN_WIDTH), Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT)));

        executor.execute(new ImageSaverTask(screenshot, String.format("%s/thumbnails/%d.png", getTestDir().getAbsolutePath(), now),
                Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH), Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT)));

        return String.format("%d.png", now);
    }

    /**
     * Asynchronous image saver task.
     */
    private static class ImageSaverTask implements Runnable {
        private BufferedImage image;
        private String path;
        private Integer width;
        private Integer height;

        public ImageSaverTask(BufferedImage image, String path, Integer width, Integer height) {
            this.image = image;
            this.path = path;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {
            try {
                if (width > 0 && height > 0) {
                    BufferedImage resizedImage = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, width, height,
                            Scalr.OP_ANTIALIAS);
                    if (resizedImage.getHeight() > height) {
                        resizedImage = Scalr.crop(resizedImage, resizedImage.getWidth(), height);
                    }
                    ImageIO.write(resizedImage, "PNG", new File(path));
                } else {
                    ImageIO.write(image, "PNG", new File(path));
                }

            } catch (Exception e) {
                LOGGER.error("Unable to save screenshot: " + e.getMessage());
            }
        }
    }
    
    private static void copyGalleryLib() {
        File reportsRootDir = new File(System.getProperty("user.dir") + "/" + Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY));
        if (!new File(reportsRootDir.getAbsolutePath() + "/gallery-lib").exists()) {
            try {
                InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(GALLERY_ZIP);
                ZipManager.copyInputStream(is, new BufferedOutputStream(new FileOutputStream(reportsRootDir.getAbsolutePath() + "/"
                        + GALLERY_ZIP)));
                ZipManager.unzip(reportsRootDir.getAbsolutePath() + "/" + GALLERY_ZIP, reportsRootDir.getAbsolutePath());
                File zip = new File(reportsRootDir.getAbsolutePath() + "/" + GALLERY_ZIP);
                zip.delete();
            } catch (Exception e) {
                LOGGER.error("Unable to copyGalleryLib!", e);
            }
        }
    }

    public static void generateTestReport() {
        File testDir = testDirectory.get();
        List<File> images = FileManager.getFilesInDir(testDir);
        try {
            List<String> imgNames = new ArrayList<String>();
            for (File image : images) {
                imgNames.add(image.getName());
            }
            imgNames.remove("thumbnails");
            imgNames.remove("test.log");
            imgNames.remove("sql.log");
            if (imgNames.size() == 0)
                return;

            Collections.sort(imgNames);

            StringBuilder report = new StringBuilder();
            for (int i = 0; i < imgNames.size(); i++) {
                // convert toString
                String image = R.REPORT.get("image");

                image = image.replace("${image}", imgNames.get(i));
                image = image.replace("${thumbnail}", imgNames.get(i));

                String title = getScreenshotComment(imgNames.get(i));
                if (title == null) {
                    title = "";
                }
                image = image.replace("${title}", StringUtils.substring(title, 0, MAX_IMAGE_TITLE));
                report.append(image);
            }
            // String wholeReport = R.REPORT.get("container").replace("${images}", report.toString());
            String wholeReport = R.REPORT.get("container").replace("${images}", report.toString());
            wholeReport = wholeReport.replace("${title}", TITLE);
            String folder = testDir.getAbsolutePath();
            FileManager.createFileWithContent(folder + REPORT_NAME, wholeReport);
        } catch (Exception e) {
            LOGGER.error("generateTestReport failure", e);
        }
    }

    /**
     * Stores comment for screenshot.
     *
     * @param screenId screenId id
     * @param msg message
     * 
     */
    public static void addScreenshotComment(String screenId, String msg) {
        if (!StringUtils.isEmpty(screenId)) {
            screenSteps.put(screenId, msg);
        }
    }

    /**
     * Return comment for screenshot.
     * 
     * @param screenId Screen Id
     * 
     * @return screenshot comment
     */
    public static String getScreenshotComment(String screenId) {
        String comment = "";
        if (screenSteps.containsKey(screenId))
            comment = screenSteps.get(screenId);
        return comment;
    }

}
