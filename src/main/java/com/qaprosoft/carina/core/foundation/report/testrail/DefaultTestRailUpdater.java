package com.qaprosoft.carina.core.foundation.report.testrail;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.report.Artifacts;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.testrail.core.APIClient;
import com.qaprosoft.carina.core.foundation.report.testrail.core.TestStatus;
import com.qaprosoft.carina.core.foundation.report.testrail.dto.*;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DefaultTestRailUpdater implements ITestRailUpdater {

    private static String url;

    private static String user;

    private static String password;

    private static CryptoTool cryptoTool;

    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    private List<Integer> runsId = new ArrayList<Integer>();
    private List<Integer> suitesId = new ArrayList<Integer>();

    private APIClient apiClient;

    private int userId = -1;

    private int milestoneId = -1;

    private static final Logger LOGGER = Logger.getLogger(DefaultTestRailUpdater.class);

    @SuppressWarnings("rawtypes")
    public DefaultTestRailUpdater() {

        try {
            cryptoTool = new CryptoTool();
            user = cryptoTool.decryptByPattern(Configuration.get(Configuration.Parameter.TESTRAIL_USER), CRYPTO_PATTERN);
            password = cryptoTool.decryptByPattern(Configuration.get(Configuration.Parameter.TESTRAIL_PASSWORD), CRYPTO_PATTERN);
        } catch (Exception e) {
            e.printStackTrace();
        }

        url = Configuration.get(Configuration.Parameter.TESTRAIL_URL);
        apiClient = new APIClient(url);
        apiClient.setUser(user);
        apiClient.setPassword(password);

        String assignedUser = Configuration.get(Configuration.Parameter.TESTRAIL_ASSIGNEE_USER);

        if (!assignedUser.equals(SpecialKeywords.NULL) && !assignedUser.isEmpty()) {
            Object respomce = apiClient.sendRequest(Users.getUserByEmail(assignedUser));
            userId = Integer.parseInt(((HashMap) respomce).get("id").toString());
        } else {
            Object obj = apiClient.sendRequest(Users.getUserByEmail(user));
            userId = Integer.parseInt(((HashMap) obj).get("id").toString());
        }


    }

    @SuppressWarnings("rawtypes")
    @Override
    public void updateBeforeSuite(ITestContext context, String testClass, String title) {

        int[] suitesID = getSuiteId(context, testClass);
        int[] projectsID = getProjectId(context, testClass);

        //verify that count of suites and projects is the same otherwise raise exception!
        if (suitesID.length != projectsID.length) {
            Assert.fail("Unable to update testrail as count of declared suites and projects is not the same in xml file!");
        }

        for (int i = 0; i < suitesID.length; i++) {
            int suiteID = suitesID[i];
            suitesId.add(suiteID);

            int projectID = projectsID[i];

            if (suiteID == -1 || projectID == -1) {
                return;
            }


            JSONArray jsonArray = (JSONArray) apiClient.sendRequest(Milestones.getMilestones(projectID));
            String milestoneName = Configuration.get(Configuration.Parameter.TESTRAIL_MILESTONE);

            if (!milestoneName.equals(SpecialKeywords.NULL)) {
                if (!Milestones.isMilestoneExist(jsonArray, milestoneName)) {
                    Object jsonObject = apiClient.sendRequest(Milestones.addMilestone(projectID, milestoneName));
                    milestoneId = Integer.parseInt(((HashMap) jsonObject).get("id").toString());
                } else {
                    JSONArray jsonObject = (JSONArray) apiClient.sendRequest(Milestones.getMilestones(projectID));
                    milestoneId = getMilestoneID(jsonObject, milestoneName);
                }

            }


            LOGGER.info("Suite ID: " + suiteID + "\n Project ID: " + projectID + "\nUser ID:  " + userId);


            Object obj;
            int MAX_CHAR = 250;

            int maxLength = (title.length() < MAX_CHAR) ? title.length() : MAX_CHAR;
            title = title.substring(0, maxLength);



            runsId.add(Integer.parseInt(((HashMap) getRun(title, suiteID, projectID)).get("id").toString()));
            //TODO: add milestones verification/adding


            initSuiteCases(suiteID, projectID); //put into the cache set of testcases for each suite/project
        }
    }


    private Object getRun(String title, int suiteID, int projectID) {
        JSONArray allRuns = (JSONArray) apiClient.sendRequest(Runs.getRuns(projectID));

        for (Object run : allRuns) {
            if (((HashMap) run).get("name").equals(title)) {
                return run;
            }
        }
        // TODO: combine both variants of Runs.addRun into the single method which can register with and w/o milestones
        if (milestoneId > 0) {
            return apiClient.sendRequest(Runs.addRun(suiteID, title, userId, projectID, milestoneId));
        } else {
            return apiClient.sendRequest(Runs.addRun(suiteID, title, userId, projectID));
        }


    }


    @Override
    public void updateAfterTest(ITestResult result, String errorMessage) {
        if (runsId.size() == 0) {
            return;
        }
        if (runsId.size() != suitesId.size()) {
            Assert.fail("Unable to update testrail as count of declared test runs and suites is not the same!");
        }

        for (int i = 0; i < runsId.size(); i++) {
            int runId = runsId.get(i);
            int suiteId = suitesId.get(i);

            long threadId = Thread.currentThread().getId();
            LOGGER.info("Updater Thread: " + (threadId));
            String test = TestNamingUtil.getCanonicTestNameByThread();

            if (test == null) {
                LOGGER.error("Unable to identify test name by threadId: " + threadId);
                return;
            }
            LOGGER.info("Info present");
            String demoUrl = ReportContext.getTestScreenshotsLink(test);
            LOGGER.info("demoUrl: " + demoUrl);
            String logUrl = ReportContext.getTestLogLink(test);
            LOGGER.info("logUrl: " + logUrl);

            String comment = "Demo URL: ".concat(demoUrl).concat("\n").concat("Log URL: ").concat(logUrl);

            LOGGER.info("Comment is: ".concat(comment));

            @SuppressWarnings("unchecked") List<String> cases = (List<String>) result.getAttribute(SpecialKeywords.TESTRAIL_CASES_ID);
            if (cases == null) return;

            if (errorMessage == null) errorMessage = "";

            HashMap<String, TestCaseResult> testCasesStatus = new HashMap<String, TestCaseResult>();
            TestCaseResult testCaseResult;
            long delta = result.getEndMillis() - result.getStartMillis();
            String elapsedTime = String.valueOf(delta / 1000L);
            String version = getFileName();
            switch (result.getStatus()) {
                case ITestResult.SUCCESS:
                    //all Cases  passed
                    for (String _case : cases) {
                        if (isSuiteContainTestCase(suiteId, _case)) {
                            LOGGER.info("PASS: case " + _case + " was found!");
                            testCaseResult = new TestCaseResult(version, elapsedTime, " ", TestStatus.PASSED, userId, " ");
                            testCasesStatus.put(_case.trim(), testCaseResult);
                        }
                    }
                    break;
                case ITestResult.SKIP:
                    // [IV] QAA-323 Investigate TestRail API Exception "results.status_id uses an invalid status (Untested)
                    // do not add untested status as it is specified by default and can't be added using standard put request
    /*                for (String _case : cases) {
                        if (isSuiteContainTestCase(result, _case)) {
	                        LOGGER.info("SKIP: case " + _case + " was found!");
	                        testCaseResult = new TestCaseResult(version, elapsedTime, comment, TestStatus.UNTESTED, userId, " ");
	                        testCasesStatus.put(_case.trim(), testCaseResult);
	                    }
	                }*/
                    break;
                case ITestResult.FAILURE:
                    boolean foundFailureStep = false;

                    //identify failure step and mark before cases as Passed, after them as Blocked
                    //if there is no way to identify failure step then mark all cases as Failed
                    for (String _case : cases) {
                        if (isSuiteContainTestCase(suiteId, _case)) {
                            LOGGER.info("FAILURE: case " + _case + " was found!");
                            if (errorMessage.contains(_case.trim())) {
                                foundFailureStep = true;
                            }
                        }
                    }
                    if (!foundFailureStep) {
                        for (String _case : cases) {
                            if (isSuiteContainTestCase(suiteId, _case)) {
                                LOGGER.info("FAILURE: case " + _case + " was found!");
                                testCaseResult = new TestCaseResult(version, elapsedTime, comment, TestStatus.FAILED, userId, " ");
                                testCasesStatus.put(_case.trim(), testCaseResult);
                            }
                        }
                    } else {
                        foundFailureStep = false;
                        for (String _case : cases) {
                            _case = _case.trim();
                            if (isSuiteContainTestCase(suiteId, _case)) {
                                LOGGER.info("FAILURE: case " + _case + " was founded!");
                                if (errorMessage.contains(_case)) {
                                    foundFailureStep = true;
                                    testCaseResult = new TestCaseResult(version, elapsedTime, comment, TestStatus.FAILED, userId, " ");
                                    testCasesStatus.put(_case.trim(), testCaseResult);

                                } else {
                                    if (!foundFailureStep) {
                                        testCaseResult = new TestCaseResult(version, elapsedTime, " ", TestStatus.PASSED, userId, " ");
                                        testCasesStatus.put(_case.trim(), testCaseResult);
                                    } else {
                                        testCaseResult = new TestCaseResult(version, elapsedTime, comment, TestStatus.BLOCKED, userId, " ");
                                        testCasesStatus.put(_case.trim(), testCaseResult);
                                    }
                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

            LOGGER.info(Results.addResultsWithLinks(runId, testCasesStatus).getJsonObject().toJSONString());
            if (testCasesStatus.size() > 0) {
                JSONArray newResult = (JSONArray) apiClient.sendRequest(Results.addResultsWithLinks(runId, testCasesStatus));
                for (Object trResult : newResult) {
                    Map responseMap = (HashMap) trResult;
                    String testId = responseMap.get("test_id").toString();
                    Object testInfo = apiClient.sendRequest(TestInfo.getTestInfo(testId));
                    Object testCaseNam = ((HashMap) testInfo).get("title");
                    Artifacts.add(testCaseNam.toString(), url + "/index.php?/tests/view/" + ((HashMap) trResult).get("test_id"));

                }


            } else {
                LOGGER.info("Results list is empty!");
            }
        }
    }

    private int[] getProjectId(ITestContext context, String className) {
        String projects = context.getSuite().getParameter(SpecialKeywords.TESTRAIL_PROJECT_ID);
        if (projects != null) {
            //split by comma and trim values before converting to int
            String[] project = projects.split(",");
            int[] projectsId = new int[project.length];
            for (int i = 0; i < project.length; i++) {
                projectsId[i] = Integer.valueOf(project[i].trim());
            }
            return projectsId;
        }
        //from annotation we support only single project id declaration!
        int projectId = -1;
        Class<?> testClass;
        try {
            testClass = Class.forName(className);
            if (testClass.isAnnotationPresent(TestRailSuite.class)) {
                TestRailSuite classAnnotation = testClass.getAnnotation(TestRailSuite.class);
                projectId = classAnnotation.projectId();


            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        int[] projectsId = new int[1];
        projectsId[0] = projectId;
        return projectsId;
    }


    private int[] getSuiteId(ITestContext context, String className) {
        String suites = context.getSuite().getParameter(SpecialKeywords.TESTRAIL_SUITE_ID);
        if (suites != null) {
            //split by comma and trim values before converting to int
            String[] suite = suites.split(",");
            int[] suitesId = new int[suite.length];
            for (int i = 0; i < suite.length; i++) {
                suitesId[i] = Integer.valueOf(suite[i].trim());
            }
            return suitesId;
        }
        //from annotation we support only single test suite declaration! 
        int testSuiteId = -1;
        Class<?> testClass;
        try {
            testClass = Class.forName(className);
            if (testClass.isAnnotationPresent(TestRailSuite.class)) {
                TestRailSuite classAnnotation = testClass.getAnnotation(TestRailSuite.class);
                testSuiteId = classAnnotation.testSuiteId();
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("Unable to instantiate class: " + className, e);
        }

        int[] suitesId = new int[1];
        suitesId[0] = testSuiteId;
        return suitesId;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public int getMilestoneID(JSONArray jsonArray, String milestoneName) {
        for (Object o : jsonArray) {
            String nameValue = ((HashMap<String, String>) o).get("name");
            if (nameValue.equals(milestoneName)) {
                return Integer.parseInt(((HashMap) o).get("id").toString());
            }

        }
        return 0;
    }

    protected String getFileName() {
        String path = Configuration.get(Configuration.Parameter.MOBILE_APP);
        if (!path.isEmpty()) {
            File file = new File(path);
            return file.getName();
        }
        return "";
    }

    private void initSuiteCases(int suiteId, int projectId) {
        ArrayList<TestCaseInfo> lcs = new ArrayList<TestCaseInfo>();

        Gson gson = new Gson();

        JsonParser parser = new JsonParser();
        JSONArray jsonArray = (JSONArray) apiClient.sendRequest(TestCases.getSuiteTestCases(projectId, suiteId));
        JsonArray jArray = parser.parse(jsonArray.toJSONString()).getAsJsonArray();

        for (JsonElement obj : jArray) {
            TestCaseInfo cse = gson.fromJson(obj, TestCaseInfo.class);
            lcs.add(cse);
        }
        TestRailCache.suiteCases.put(suiteId, lcs);
    }

    private ArrayList<TestCaseInfo> getSuiteCases(int suiteId) {

        ArrayList<TestCaseInfo> lcs = new ArrayList<TestCaseInfo>();
        if (TestRailCache.suiteCases.containsKey(suiteId)) {
            return TestRailCache.suiteCases.get(suiteId);
        } else {
            return lcs;
        }
    }


    private boolean isSuiteContainTestCase(int suiteId, String testCase) {
        if (testCase.isEmpty()) {
            return false;
        }

        ArrayList<TestCaseInfo> lcs = getSuiteCases(suiteId);

        Integer tc = Integer.parseInt(testCase);
        for (TestCaseInfo lc : lcs) {
            if (lc.getId().equals(tc)) {
                return true;
            }
        }
        return false;
    }

}
