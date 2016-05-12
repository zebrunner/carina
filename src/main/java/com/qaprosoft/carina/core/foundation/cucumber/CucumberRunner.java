package com.qaprosoft.carina.core.foundation.cucumber;

import com.qaprosoft.carina.core.foundation.UITest;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import cucumber.api.testng.CucumberFeatureWrapper;
import cucumber.api.testng.TestNGCucumberRunner;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public abstract class CucumberRunner extends UITest {
    private TestNGCucumberRunner testNGCucumberRunner;

    public static final String CUCUMBER_REPORT_FOLDER = "CucumberReport";

    protected static final Logger LOGGER = Logger.getLogger(CucumberRunner.class);

    public CucumberRunner() {
    }

    @BeforeClass(alwaysRun = true)
    public void setUpClass() throws Exception {
        if (!isCucumberTest()) {
            LOGGER.error("Looks like it is not Cucumber test");
            throw new Exception("Not Cucumber Test. Please check config.properties.");
            //return;
        }
        this.testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());

    }

    @Test(
            groups = {"cucumber"},
            description = "Runs Cucumber Feature",
            dataProvider = "features"
    )
    public void feature(CucumberFeatureWrapper cucumberFeature) {
        if (!isCucumberTest()) return;
        this.testNGCucumberRunner.runCucumber(cucumberFeature.getCucumberFeature());
    }

    @DataProvider
    public Object[][] features() {
        return this.testNGCucumberRunner.provideFeatures();
    }

    @AfterClass
    public void tearDownClass(ITestContext context) throws Exception {
        if (!isCucumberTest()) return;
        LOGGER.info("In  @AfterClass tearDownClass");
        this.testNGCucumberRunner.finish();
        generateCucumberReport(context);
    }


    /**
     * Generate Cucumber Report based on config parameters cucumber_tests_app_version and cucumber_tests_name
     */
    public void generateCucumberReport() {
        generateCucumberReport(getCucumberTestName(), getCucumberAppVersion());
    }

    /**
     * Generate Cucumber Report based on TestNg context
     *
     * @param context ITestContext
     */
    public void generateCucumberReport(ITestContext context) {
        String suiteName = getSuiteName(context);
        String title = getTitle(context);

        String app_version = getCucumberAppVersion();

        String cucumberTestName = getCucumberTestName();

        if ((app_version.isEmpty()) || (app_version.toLowerCase().contains("app")) || (app_version.toLowerCase().contains("jenkins"))) {
            if (!Configuration.isNull(Configuration.Parameter.APP_VERSION)) {
                if (!Configuration.get(Configuration.Parameter.APP_VERSION).isEmpty()) {
                    // if nothing is specified then title will contain nothing
                    app_version = Configuration.get(Configuration.Parameter.APP_VERSION);
                }
            } else {
                app_version = "1";
            }
        }

        if ((cucumberTestName.isEmpty()) || (cucumberTestName.toLowerCase().contains("app")) || (cucumberTestName.toLowerCase().contains("jenkins"))) {
            cucumberTestName = title;
        }

        LOGGER.info("suiteName: " + suiteName);
        LOGGER.info("title: " + title);
        LOGGER.info("app_version: " + app_version);
        LOGGER.info("cucumberTestName: " + cucumberTestName);

        generateCucumberReport(cucumberTestName, app_version);
    }


    /**
     * Generate Cucumber Report based on json files in target folder
     *
     * @param buildProject String
     * @param buildNumber  String
     */
    public void generateCucumberReport(String buildProject, String buildNumber) {
        if (!isCucumberTest()) return;

        try {
            //String RootDir = System.getProperty("user.dir");
            File file = ReportContext.getArtifactsFolder();

            File reportOutputDirectory = new File(String.format("%s/%s", file, CUCUMBER_REPORT_FOLDER));
            //File reportOutputDirectory = new File(file, "/CucumberReport");

            File dir = new File("target/");

            File[] finder = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".json");
                }
            });

            List<String> list = new ArrayList<String>();

            for (File fl : finder) {
                LOGGER.info("Report json: " + fl.getName());
                list.add("target/" + fl.getName());
            }

            //buildNumber should be parsable Integer
            buildNumber= buildNumber.replace(".","").replace(",","");


            if (list.size() > 0) {
                String pluginUrlPath = "";
                //String buildNumber = "1";
                //String buildProject = "CUCUMBER";
                boolean skippedFails = true;
                boolean pendingFails = true;
                boolean undefinedFails = true;
                boolean missingFails = true;
                boolean flashCharts = true;
                boolean runWithJenkins = false;
                boolean highCharts = false;
                boolean parallelTesting = true;
                boolean artifactsEnabled = false;
                String artifactConfig = "";

                net.masterthought.cucumber.Configuration configuration = new net.masterthought.cucumber.Configuration(reportOutputDirectory, buildProject + " Cucumber Test Results");
                configuration.setStatusFlags(skippedFails, pendingFails, undefinedFails, missingFails);
                // configuration.setParallelTesting(parallelTesting);
                // configuration.setJenkinsBasePath(jenkinsBasePath);
                // configuration.setRunWithJenkins(runWithJenkins);
                configuration.setBuildNumber(buildNumber);


                ReportBuilder reportBuilder = new ReportBuilder(list, configuration);
                reportBuilder.generateReports();
            } else {
                LOGGER.info("There are no json files for cucumber report.");
                return;
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static boolean isCucumberTest() {
        boolean ret = false;
        if (!Configuration.isNull(Configuration.Parameter.CUCUMBER_TESTS)) {
            if (Configuration.get(Configuration.Parameter.CUCUMBER_TESTS).toLowerCase().contains("true")) {
                ret = true;
                LOGGER.debug("It is Cucumber Test");
            }
        } else {
            LOGGER.debug("Looks like it is not Cucumber test");
        }
        return ret;
    }


    /**
     * Check that CucumberReport Folder exists.
     * @return boolean
     */
    public static boolean isCucumberReportFolderExists() {
        try {
            File reportOutputDirectory = new File(String.format("%s/%s", ReportContext.getArtifactsFolder(), CUCUMBER_REPORT_FOLDER));
            if (reportOutputDirectory.exists() && reportOutputDirectory.isDirectory()) {
                if(reportOutputDirectory.list().length>0){
                    LOGGER.debug("Cucumber Report Folder is not empty!");
                    return true;
                }else{
                    LOGGER.error("Cucumber Report Folder is empty!");
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error happen during checking that CucumberReport Folder exists or not. Error: "+e.getMessage());
        }
        return false;
    }


    /**
     * Returns URL for cucumber report.
     *
     * @return - URL to test log folder.
     */
    public static String getCucumberReportResultLink()
    {
        String link = "";

/*
        if (!Configuration.get(Configuration.Parameter.REPORT_URL).isEmpty()) {
            //remove report url and make link relative
            link = String.format("%s/%s/feature-overview.html", Configuration.get(Configuration.Parameter.REPORT_URL), ReportContext.getArtifact(CUCUMBER_REPORT_FOLDER));
        }
        else {
            link = String.format("file://%s/feature-overview.html", ReportContext.getArtifact(CUCUMBER_REPORT_FOLDER));
        }
*/
        link=ReportContext.getCucumberReportLink(CUCUMBER_REPORT_FOLDER);

        LOGGER.info("Cucumber Report link: "+link);

        return link;
    }

    private String getCucumberAppVersion() {
        String ret = "";
        try {
            if (!Configuration.isNull(Configuration.Parameter.CUCUMBER_TESTS_APP_VERSION)) {
                if (!Configuration.get(Configuration.Parameter.CUCUMBER_TESTS_APP_VERSION).isEmpty()) {
                    LOGGER.info("Get Cucumber Test App Version from config");
                    ret = Configuration.get(Configuration.Parameter.CUCUMBER_TESTS_APP_VERSION);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Cucumber App Version=" + ret);
        return ret;
    }

    /**
     * getCucumberTestName
     * @return String
     */
    private String getCucumberTestName() {
        String ret = "";
        try {
            if (!Configuration.isNull(Configuration.Parameter.CUCUMBER_TESTS_NAME)) {
                if (!Configuration.get(Configuration.Parameter.CUCUMBER_TESTS_NAME).isEmpty()) {
                    LOGGER.debug("Get Cucumber Test Name from config");
                    ret = Configuration.get(Configuration.Parameter.CUCUMBER_TESTS_NAME);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Cucumber Test Name=" + ret);
        return ret;
    }

    /**
     * useJSinCucumberReport
     * @return boolean
     */
    public static boolean useJSinCucumberReport() {
        boolean ret = false;
        try {
            if (!Configuration.isNull(Configuration.Parameter.CUCUMBER_USE_JS_IN_REPORT)) {
                if (!Configuration.get(Configuration.Parameter.CUCUMBER_USE_JS_IN_REPORT).isEmpty()) {
                    LOGGER.debug("Get Cucumber Test Name from config");
                    if (Configuration.get(Configuration.Parameter.CUCUMBER_USE_JS_IN_REPORT).toLowerCase().contains("true"))
                        return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return ret;
    }
}