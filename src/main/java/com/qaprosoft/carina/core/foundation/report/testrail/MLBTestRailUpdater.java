package com.qaprosoft.carina.core.foundation.report.testrail;

import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.report.testrail.core.APIClient;
import com.qaprosoft.carina.core.foundation.report.testrail.dto.Results;
import com.qaprosoft.carina.core.foundation.report.testrail.dto.Run;
import com.qaprosoft.carina.core.foundation.report.testrail.dto.User;
import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Pattern;

import static com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import static com.qaprosoft.carina.core.foundation.utils.Configuration.get;

/**
 * Created by Patotsky on 14.01.2015.
 */
public class MLBTestRailUpdater implements ITestRailUpdater {

    private static String url;
    private static String user;
    private static String password;
    private static CryptoTool cryptoTool;
    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);
    private int runId;
    private APIClient apiClient;
    private int userId;


    public MLBTestRailUpdater() {

        try {
            cryptoTool = new CryptoTool();
            user = cryptoTool.decryptByPattern(get(Parameter.TESTRAIL_USER), CRYPTO_PATTERN);
            password = cryptoTool.decryptByPattern(get(Parameter.TESTRAIL_PASSWORD), CRYPTO_PATTERN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        url = get(Parameter.TESTRAIL_URL);
        apiClient = new APIClient(url);
        apiClient.setUser(user);
        apiClient.setPassword(password);

        Object obj = apiClient.sendRequest(User.getUserByEmail(user));
        userId = (Integer) (((HashMap) obj).get("id"));
    }


    @Override
    public void updateBeforeSuite(String className) {
        Object obj = apiClient.sendRequest(Run.addRun(getSuiteId(className), DateUtils.now().toString(), userId, getProjectId(className)));
        runId = (Integer) (((HashMap) obj).get("id"));


    }

    @Override
    public void updateAfterTest(ITestResult result, Throwable thr) {

        HashMap<String, TestStatus> testCasesStatus = new HashMap<String, TestStatus>();

        //Spira test steps integration
        //Get a handle to the class and method
        Class<?> testClass;
        try {
            testClass = Class.forName(result.getMethod().getTestClass().getName());

            //We can't use getMethod() because we may have parameterized tests
            //for which we won't know the matching signature
            String methodName = result.getMethod().getMethodName();
            Method testMethod = null;
            Method[] possibleMethods = testClass.getMethods();
            for (Method possibleMethod : possibleMethods) {
                if (possibleMethod.getName().equals(methodName)) {
                    testMethod = possibleMethod;
                    break;
                }
            }

            if (testMethod != null) {
                //Extract the TestRailCases test case id - if present
                if (testMethod.isAnnotationPresent(TestRailCases.class)) {
                    TestRailCases methodAnnotation = testMethod.getAnnotation(TestRailCases.class);
                    String testCasesId = methodAnnotation.testCasesId();
                    String[] cases = testCasesId.split(",");
                    switch (result.getStatus()) {
                        case ITestResult.SUCCESS:
                            //all Cases  passed
                            for (String _case : cases) {
                                testCasesStatus.put(_case.trim(), TestStatus.PASSED);
                            }
                            break;
                        case ITestResult.SKIP:
                            for (String _case : cases) {
                                testCasesStatus.put(_case.trim(), TestStatus.UNTESTED);
                            }
                            break;
                        case ITestResult.FAILURE:
                            boolean foundFailureStep = false;
                            String errorMessage = thr.getMessage();

                            //identify failure step and mark before cases as Passed, after them as Blocked
                            //if there is no way to identify failure step then mark all cases as Failed
                            for (String _case : cases) {
                                if (errorMessage.contains(_case.trim())) {
                                    foundFailureStep = true;
                                }
                            }
                            if (!foundFailureStep) {
                                for (String _case : cases) {
                                    testCasesStatus.put(_case.trim(), TestStatus.FAILED);
                                }
                            } else {
                                foundFailureStep = false;
                                for (String _case : cases) {
                                    _case = _case.trim();
                                    if (errorMessage.contains(_case)) {
                                        foundFailureStep = true;
                                        testCasesStatus.put(_case.trim(), TestStatus.FAILED);
                                    } else {
                                        if (!foundFailureStep) {
                                            testCasesStatus.put(_case.trim(), TestStatus.PASSED);
                                        } else {
                                            testCasesStatus.put(_case.trim(), TestStatus.BLOCKED);
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        apiClient.sendRequest(Results.addResults(runId, testCasesStatus));
    }

    private int getProjectId(String className) {
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

        return projectId;
    }


    private int getSuiteId(String className) {
        int testSuiteId = -1;
        Class<?> testClass;
        try {
            testClass = Class.forName(className);
            if (testClass.isAnnotationPresent(TestRailSuite.class)) {
                TestRailSuite classAnnotation = testClass.getAnnotation(TestRailSuite.class);
                testSuiteId = classAnnotation.testSuiteId();


            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return testSuiteId;
    }


}
