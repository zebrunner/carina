/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
import java.lang.invoke.MethodHandles;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.log.ThreadLogAppender;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.FileManager;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.ZipManager;
import com.zebrunner.agent.core.registrar.Artifact;

/*
 * Be careful with LOGGER usage here because potentially it could do recursive call together with ThreadLogAppender functionality
 */

public class ReportContext {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

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
                LOGGER.info("Artifacts folder already exists: " + artifactsDirectory.getAbsolutePath());
            }

            if (!isCreated) {
                throw new RuntimeException("Artifacts folder not created: " + artifactsDirectory.getAbsolutePath());
            } else {
                LOGGER.debug(("Artifacts folder created: " + artifactsDirectory.getAbsolutePath()));
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

    /**
     * download artifact from selenoid to local java machine
     * 
     * @param driver WebDriver
     * @param name String
     * @param timeout long
     * @return artifact File
     */
    public static File downloadArtifact(WebDriver driver, String name, long timeout) {
        File file = getArtifact(name);
        if (file == null) {
            // attempt to verify and download file from selenoid

            String url = getUrl(driver, name);
            String username = getField(url, 1);
            String password = getField(url, 2);

            if (!artifactExists(driver, name, timeout)) {
                Assert.fail("Unable to find artifact: " + name);
            }

            file = new File(getArtifactsFolder() + File.separator + name);
            String path = file.getAbsolutePath();
            LOGGER.debug("artifact file to download: " + path);

            if (!username.isEmpty() && !password.isEmpty()) {
                Authenticator.setDefault(new CustomAuthenticator(username, password));
            }

            try {
                FileUtils.copyURLToFile(new URL(url), file);
                LOGGER.debug("Successfully downloaded artifact: " + name);
                Artifact.attachToTest(name, file); // publish as test artifact to Zebrunner Reporting
            } catch (IOException e) {
                LOGGER.error("Artifact: " + url + " wasn't downloaded to " + path, e);
            }
        }
        return file;
    }

    public static class CustomAuthenticator extends Authenticator {

        String username;
        String password;

        public CustomAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }

    /**
     * check if artifact exists
     * 
     * @param driver WebDriver
     * @param name String
     * @param timeout long
     * @return boolean
     */
    public static boolean artifactExists(WebDriver driver, String name, long timeout) {
        String url = getUrl(driver, name);
        String username = getField(url, 1);
        String password = getField(url, 2);
        try {
            return new WebDriverWait(driver, timeout).until((k) -> checkArtifactUsingHttp(url, username, password));
        } catch (Exception e) {
            LOGGER.debug(e);
            return false;
        }
    }

    /**
     * check if artifact exists using http
     * 
     * @param url String
     * @param username String
     * @param password String
     * @return boolean
     */
    private static boolean checkArtifactUsingHttp(String url, String username, String password) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            // HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");

            if (!username.isEmpty() && !password.isEmpty()) {
                String usernameColonPassword = username + ":" + password;
                String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
                con.addRequestProperty("Authorization", basicAuthPayload);
            }

            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            LOGGER.debug("Artifact doesn't exist: " + url, e);
            return false;
        }
    }

    /**
     * get username or password from url
     * 
     * @param url String
     * @param position int
     * @return String
     */
    private static String getField(String url, int position) {
        Pattern pattern = Pattern.compile(".*:\\/\\/(.*):(.*)@");
        Matcher matcher = pattern.matcher(url);

        return matcher.find() ? matcher.group(position) : "";

    }

    /**
     * generate url for artifact by name
     * 
     * @param driver WebDriver
     * @param name String
     * @return String
     */
    private static String getUrl(WebDriver driver, String name) {
        String seleniumHost = Configuration.get(Parameter.SELENIUM_HOST).replace("wd/hub", "download/");
        WebDriver drv = (driver instanceof EventFiringWebDriver) ? ((EventFiringWebDriver) driver).getWrappedDriver() : driver;
        String sessionId = ((RemoteWebDriver) drv).getSessionId().toString();
        String url = seleniumHost + sessionId + "/" + name;
        LOGGER.debug("url: " + url);
        return url;
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
        
        Artifact.attachToTest(name, IOUtils.toByteArray(source));
    }

    public static void saveArtifact(File source) throws IOException {
        File artifact = new File(String.format("%s/%s", getArtifactsFolder(), source.getName()));
        artifact.createNewFile();
        FileUtils.copyFile(source, artifact);
        
        Artifact.attachToTest(source.getName(), artifact);
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

            testDir = new File(directory);

            if (!testDir.mkdirs()) {
                throw new RuntimeException("Test Folder(s) not created: " + testDir.getAbsolutePath());
            }
        }

        testDirectory.set(testDir);
        return testDir;
    }

    /**
     * Rename test directory from unique number to custom name.
     * 
     * @param dirName String
     * 
     * @return test report dir
     */
    public synchronized static File setCustomTestDirName(String dirName) {
        isCustomTestDirName.set(Boolean.FALSE);
        File testDir = testDirectory.get();
        if (testDir == null) {
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

    private static File renameTestDir(String test) {
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
        }
        ;
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
            link = String.format("%s/%d/artifacts", Configuration.get(Parameter.REPORT_URL), rootID);
        } else {
            link = String.format("file://%s/artifacts", getBaseDirAbsolutePath());
        }

        return link;

    }

    /**
     * Returns URL for test screenshot folder.
     * 
     * @return - URL for test screenshot folder.
     */
    public static String getTestScreenshotsLink() {
        String link = "";
        try {
            if (FileUtils.listFiles(ReportContext.getTestDir(), new String[] { "png" }, false).isEmpty()) {
                // no png screenshot files at all
                return link;
            }
        } catch (Exception e) {
            LOGGER.error("Exception during report directory scanning", e);
        }
        
        String test = testDirectory.get().getName().replaceAll("[^a-zA-Z0-9.-]", "_");
        
        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            link = String.format("%s/%d/%s/report.html", Configuration.get(Parameter.REPORT_URL), rootID, test);
        } else {
            link = String.format("file://%s/%s/report.html", getBaseDirAbsolutePath(), test);
        }

        return link;

    }

    /**
     * Returns list of links to video in local file system for recorded driver sessions
     * 
     * @param sessionIds
     *            String list of session Ids to find video files for
     * @return
     *         String list of generated links
     */
    public static List<String> getTestVideoLinks(List<String> sessionIds) {
        List<String> links = new ArrayList<>();
        for (String sessionId : sessionIds) {
            String videoFileName = String.format(SpecialKeywords.DEFAULT_VIDEO_FILENAME, sessionId);
            File videoFile = new File(ReportContext.getArtifactsFolder() + File.separator + videoFileName);
            if (!videoFile.exists()) {
                // no video file at all
                continue;
            }

            links.add(getTestArtifactsLink() + File.separator + videoFileName);
        }
        return links;
    }

    /**
     * Returns URL for test log.
     * @return - URL to test log folder.
     */
    public static String getTestLogLink() {
        String link = "";
        File testLogFile = new File(ReportContext.getTestDir() + "/" + "test.log");
        if (!testLogFile.exists()) {
            // no test.log file at all
            return link;
        }

        String test = testDirectory.get().getName().replaceAll("[^a-zA-Z0-9.-]", "_");
        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            link = String.format("%s/%d/%s/test.log", Configuration.get(Parameter.REPORT_URL), rootID, test);
        } else {
            link = String.format("file://%s/%s/test.log", getBaseDirAbsolutePath(), test);
        }

        return link;
    }

    /**
     * Returns URL for cucumber report.
     * 
     * @return - URL to test log folder.
     */
    public static String getCucumberReportLink() {

        String folder = SpecialKeywords.CUCUMBER_REPORT_FOLDER;
        String subFolder = SpecialKeywords.CUCUMBER_REPORT_SUBFOLDER;
        String fileName = SpecialKeywords.CUCUMBER_REPORT_FILE_NAME;

        String link = "";
        if (!Configuration.get(Parameter.REPORT_URL).isEmpty()) {
            // remove report url and make link relative
            // link = String.format("./%d/report.html", rootID);
            String report_url = Configuration.get(Parameter.REPORT_URL);
            if (report_url.contains("n/a")) {
                LOGGER.error("Contains n/a. Replace it.");
                report_url = report_url.replace("n/a", "");
            }
            link = String.format("%s/%d/%s/%s/%s", report_url, rootID, folder, subFolder, fileName);
        } else {
            link = String.format("file://%s/%s/%s/%s", getBaseDirAbsolutePath(), folder, subFolder, fileName);
        }

        return link;
    }

    /**
     * Saves screenshot.
     * 
     * @param screenshot - {@link BufferedImage} file to save
     * 
     * @return - screenshot name.
     */
    public static String saveScreenshot(BufferedImage screenshot) {
        long now = System.currentTimeMillis();

        executor.execute(new ImageSaverTask(screenshot, String.format("%s/%d.png", getTestDir().getAbsolutePath(), now),
                Configuration.getInt(Parameter.BIG_SCREEN_WIDTH), Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT)));

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
                if (is == null) {
                    LOGGER.warn("Unable to find in classpath: " + GALLERY_ZIP);
                    return;
                }
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
        try {
            List<File> images = FileManager.getFilesInDir(testDir);
            List<String> imgNames = new ArrayList<String>();
            for (File image : images) {
                imgNames.add(image.getName());
            }
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

    private static String getBaseDirAbsolutePath() {
        if (baseDirectory != null) {
            return baseDirectory.getAbsolutePath();
        }
        return null;
    }

}
