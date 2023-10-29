package com.zebrunner.carina.core.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.FileManager;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.config.IParameter;
import com.zebrunner.carina.utils.report.ReportContext;

public class ReportConfiguration extends Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String CUCUMBER_REPORT_FOLDER = "cucumber-reports";
    private static final String CUCUMBER_REPORT_SUBFOLDER = "cucumber-html-reports";
    private static final String CUCUMBER_REPORT_FILE_NAME = "overview-features.html";

    public enum Parameter implements IParameter {

        /**
         * Application version/build number for reporting.<br>
         * Example: {@code 1.2.5}
         */
        APP_VERSION("app_version"),

        /**
         * todo add doc
         */
        @Deprecated
        REPORT_URL("report_url"),

        /**
         * todo add doc
         */
        CI_BUILD_URL("ci_build_url"),

        /**
         * Level for Carina logging. <b>Default: {@code INFO}</b>
         */
        CORE_LOG_LEVEL("core_log_level"),

        /**
         * Date format for DateUtils.class. <b>Default: {@code HH:mm:ss yyyy-MM-dd}</b>
         */
        DATE_FORMAT("date_format"),

        /**
         * Time format for DateUtils.class. <b>Default: {@code HH:mm:ss}</b>
         */
        TIME_FORMAT("time_format"),

        /**
         * Max number of reports artifacts saved in history. <b>Default: 10</b>
         */
        MAX_SCREENSHOOT_HISTORY("max_screen_history"),

        /**
         * todo add description
         */
        SUITE_NAME("suite_name"),

        /**
         * The pattern by which the name of the test method will be formed. <b>Default: {@code {tuid} {test_name} - {method_name}}</b>
         */
        TEST_NAMING_PATTERN("test_naming_pattern"),

        /**
         * todo add description
         */
        TESTRAIL_ENABLED("testrail_enabled"),

        /**
         * todo add description
         */
        INCLUDE_ALL("include_all"),

        /**
         * todo add description
         */
        MILESTONE("milestone"),

        /**
         * todo add description
         */
        RUN_NAME("run_name"),

        /**
         * todo add description
         */
        ASSIGNEE("assignee"),

        /**
         * todo add description
         */
        GIT_HASH("git_hash");

        private final String key;

        Parameter(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    /**
     * Generate HTML report. <b>For internal usage only</b>
     */
    public static void generateHtmlReport(String content) {
        String emailableReport = SpecialKeywords.HTML_REPORT;
        try {
            Files.write(Path.of(System.getProperty("user.dir"))
                    .resolve(Configuration.getRequired(Configuration.Parameter.PROJECT_REPORT_DIRECTORY))
                    .resolve(emailableReport),
                    content.getBytes(StandardCharsets.UTF_8));
            Files.write(ReportContext.getBaseDirectory().resolve(emailableReport),
                    content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("generateHtmlReport failure", e);
        }
    }

    /**
     * Get link to the test artifacts folder
     *
     * @return url
     */
    public static String getTestArtifactsLink() {
        Optional<String> reportURL = Configuration.get(ReportConfiguration.Parameter.REPORT_URL);
        if (reportURL.isPresent()) {
            return String.format("%s/%s/artifacts", reportURL.get(), ReportContext.getBaseDirectory().getFileName().toString());
        } else {
            return String.format("file://%s/artifacts", ReportContext.getBaseDirectory().toString());
        }
    }

    /**
     * Get link to the test screenshots folder
     *
     * @return url
     */
    public static String getTestScreenshotsLink() {
        String link = "";
        try(Stream<Path> stream =  Files.list(ReportContext.getTestDirectory())
                .filter(Files::isRegularFile)
                .filter(path -> path.endsWith("png"))) {

            if (stream.findAny().isEmpty()) {
                // no png screenshot files at all
                return link;
            }
        } catch (Exception e) {
            LOGGER.error("Exception during report directory scanning", e);
        }

        String test = ReportContext.getTestDirectory()
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9.-]", "_");
        Optional<String> reportURL = Configuration.get(Parameter.REPORT_URL);
        if (reportURL.isPresent()) {
            link = String.format("%s/%s/%s/report.html",
                    reportURL.get(), ReportContext.getBaseDirectory().getFileName(), test);
        } else {
            link = String.format("file://%s/%s/report.html", ReportContext.getBaseDirectory().toAbsolutePath(), test);
        }
        return link;
    }

    /**
     * Get link to the test log
     * 
     * @return url
     */
    public static String getTestLogLink() {
        String link = "";
        Path testLogFile = ReportContext.getTestDirectory().resolve("test.log");
        if (!Files.exists(testLogFile)) {
            // no test.log file at all
            return link;
        }

        String test = ReportContext.getTestDirectory()
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9.-]", "_");
        Optional<String> reportURL = Configuration.get(Parameter.REPORT_URL);

        if (reportURL.isPresent()) {
            link = String.format("%s/%s/%s/test.log",
                    reportURL.get(),
                    ReportContext.getBaseDirectory()
                            .getFileName()
                            .toString(), test);
        } else {
            link = String.format("file://%s/%s/test.log", ReportContext.getBaseDirectory().toAbsolutePath(), test);
        }
        return link;
    }

    /**
     * Get link to the cucumber report
     *
     * @return url
     */
    public static String getCucumberReportLink() {
        Optional<String> ciBuildURL = Configuration.get(Parameter.CI_BUILD_URL);
        if (ciBuildURL.isPresent()) {
            return String.format("%s/%s", ciBuildURL.get(), "CucumberReport");
        } else {
            return String.format("file://%s/%s/%s/%s", ReportContext.getBaseDirectory().toAbsolutePath(), CUCUMBER_REPORT_FOLDER,
                    CUCUMBER_REPORT_SUBFOLDER, CUCUMBER_REPORT_FILE_NAME);
        }
    }

    /**
     * Generate test report. <b>For internal usage only</b>
     */
    public static void generateTestReport() {
        File testDir = ReportContext.getTestDirectory()
                .toFile();
        try {
            List<File> images = FileManager.getFilesInDir(testDir);
            List<String> imgNames = new ArrayList<>();
            for (File image : images) {
                imgNames.add(image.getName());
            }
            imgNames.remove("test.log");
            imgNames.remove("sql.log");
            if (imgNames.isEmpty())
                return;

            Collections.sort(imgNames);

            StringBuilder report = new StringBuilder();
            for (String imgName : imgNames) {
                // convert toString
                String image = R.REPORT.get("image");

                image = image.replace("${image}", imgName);

                image = image.replace("${title}", StringUtils.substring("", 0, 300));
                report.append(image);
            }
            String wholeReport = R.REPORT.get("container").replace("${images}", report.toString());
            wholeReport = wholeReport.replace("${title}", "Test steps demo");
            String folder = testDir.getAbsolutePath();
            FileManager.createFileWithContent(folder + "/report.html", wholeReport);
        } catch (Exception e) {
            LOGGER.error("generateTestReport failure", e);
        }
    }

    /**
     * Removes emailable html report and oldest screenshots directories according to history size defined in config.
     * <b>for internal usage only</b>
     */
    public static void removeOldReports() {
        File baseDir = new File(String.format("%s/%s", System.getProperty("user.dir"),
                Configuration.getRequired(Configuration.Parameter.PROJECT_REPORT_DIRECTORY)));

        if (baseDir.exists()) {
            // remove old emailable report
            File reportFile = new File(String.format("%s/%s/%s", System.getProperty("user.dir"),
                    Configuration.getRequired(Configuration.Parameter.PROJECT_REPORT_DIRECTORY), SpecialKeywords.HTML_REPORT));
            if (reportFile.exists()) {
                boolean isSuccessful = reportFile.delete();
                if (!isSuccessful) {
                    System.out.println(String.format("Report file can't be deleted: %s", reportFile.getAbsolutePath()));
                }
            }

            List<File> files = FileManager.getFilesInDir(baseDir);
            List<File> screenshotFolders = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory() && !file.getName().startsWith(".")) {
                    screenshotFolders.add(file);
                }
            }

            int maxHistory = Configuration.getRequired(Parameter.MAX_SCREENSHOOT_HISTORY, Integer.class);

            if (maxHistory > 0 && screenshotFolders.size() + 1 > maxHistory) {
                Comparator<File> comp = (file1, file2) -> file2.getName().compareTo(file1.getName());
                screenshotFolders.sort(comp);
                for (int i = maxHistory - 1; i < screenshotFolders.size(); i++) {
                    if (screenshotFolders.get(i).getName().equals("gallery-lib")) {
                        continue;
                    }
                    try {
                        FileUtils.deleteDirectory(screenshotFolders.get(i));
                    } catch (IOException e) {
                        System.out.println((e + "\n" + e.getMessage()));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        Optional<String> asString = asString(Parameter.values());
        if (asString.isEmpty()) {
            return "";
        }
        return "\n============= Report configuration ============\n" +
                asString.get();
    }
}
