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
package com.qaprosoft.carina.core.foundation.jira;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.qaprosoft.zafira.models.db.workitem.BaseWorkItem;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;

/*
 * Jira
 * 
 * @author Alex Khursevich
 */
public class Jira {
    private static final Logger LOG = Logger.getLogger(Jira.class);
    private static IJiraUpdater updater;
    private static JiraClient jira;
    private static boolean isInitialized = false;
    private static CryptoTool cryptoTool;
    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    protected static ThreadLocal<List<String>> jiraTickets = new ThreadLocal<>();
    protected static ThreadLocal<BaseWorkItem> knownIssue = new ThreadLocal<>();

    static {
        try {
            cryptoTool = new CryptoTool(Configuration.get(Parameter.CRYPTO_KEY_PATH));
            updater = (IJiraUpdater) Class.forName(Configuration.get(Parameter.JIRA_UPDATER)).newInstance();
            BasicCredentials creds = new BasicCredentials(cryptoTool.decryptByPattern(Configuration.get(Parameter.JIRA_USER), CRYPTO_PATTERN),
                    (cryptoTool.decryptByPattern(Configuration.get(Parameter.JIRA_PASSWORD), CRYPTO_PATTERN)));
            jira = new JiraClient(Configuration.get(Parameter.JIRA_URL), creds);
            isInitialized = true;
        } catch (Exception e) {
            LOG.info("Jira update utility not initialized (specify jira_updater, jira_url, jira_user, jira_password, crypto_key_path): "
                    + e.getMessage());
        }
    }

    public synchronized static void updateAfterTest(ITestResult result) {
        if (isInitialized) {
            try {
                updater.updateAfterTest(jira, result);
            } catch (Exception e) {
                LOG.error("Jira 'updateAfterTest' not performed: " + e.getMessage());
            }
        }
    }

    public synchronized static void updateAfterSuite(ITestContext context, List<TestResultItem> results) {
        if (isInitialized) {
            try {
                updater.updateAfterSuite(jira, context, results);
            } catch (Exception e) {
                LOG.error("Jira 'updateAfterSuite' not performed: " + e.getMessage());
            }
        }
    }

    private static void clearTickets() {
        jiraTickets.remove();
    }

    public static void setTickets(List<String> tickets) {
        jiraTickets.set(tickets);
    }

    public static void setTickets(String... tickets) {
        List<String> tempTickets = new ArrayList<String>();
        for (String ticket : tickets) {
            tempTickets.add(ticket);
        }
        setTickets(tempTickets);
    }

    public synchronized static List<String> getTickets(ITestResult result) {
        // return any specified jira tickets by tests
        if (jiraTickets.get() != null) {
            return jiraTickets.get();
        }

        List<String> tickets = new ArrayList<String>();

        if (result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.JIRA_TICKET) != null) {
            tickets.add(result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.JIRA_TICKET));
        }
        if (result.getMethod().getDescription() != null && result.getMethod().getDescription().contains(SpecialKeywords.JIRA_TICKET)) {
            tickets.clear();
            String description = result.getMethod().getDescription();
            
            if (description.split("#").length > 1) {
	            try {
	                tickets.add(description.split("#")[1].trim());
	            } catch (Exception e) {
	                LOG.error("Incorrect Jira-ticket format: " + description, e);
	            }
            }
        }

        @SuppressWarnings("unchecked")
        Map<Object[], String> testnameJiraMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.JIRA_ARGS_MAP);
        if (testnameJiraMap != null) {
            String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
            if (testnameJiraMap.containsKey(testHash)) {
                tickets.clear();
                tickets.add(testnameJiraMap.get(testHash));
            }
        }
        return tickets;
    }

    public static boolean isRetryDisabled(ITestResult result) {
        boolean ignoreKnownIssue = Configuration.getBoolean(Parameter.TRACK_KNOWN_ISSUES);
        int knownIssuesCount = getTickets(result).size();

        // [VD] QUALITY-1408 disable retry test execution if ignore_known_issues is enabled and jira ticket(s) provided
        boolean disableRetryForKnownIssues = ignoreKnownIssue && (knownIssuesCount > 0);

        return disableRetryForKnownIssues;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized static String processBug(ITestResult result) {
        if (isInitialized) {
            Class clazz = result.getMethod().getRealClass();
            Method m;
            try {
                m = clazz.getMethod(result.getMethod().getMethodName(), result.getMethod().getConstructorOrMethod().getMethod().getParameterTypes());
            } catch (Exception e) {
                LOG.error("Exception during test name getting", e);
                return null;
            }
            /* priority 1: bug set from test code */
            String test = TestNamingUtil.getTestNameByThread();
            String bugId = TestNamingUtil.getBug(test);
            /* priority 2: bug set from data provider */
            if (bugId == null) {
                Map<Object[], String> testnameBugMap = (Map<Object[], String>) result.getTestContext().getAttribute(
                        SpecialKeywords.BUG_ARGS_MAP);
                if (testnameBugMap != null) {
                    String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
                    if (testnameBugMap.containsKey(testHash)) {
                        bugId = testnameBugMap.get(testHash);
                    }
                }
            }
            /* priority 3: bug set from @Bug annotation */
            if (bugId == null) {
                if (m.isAnnotationPresent(Bug.class)) {
                    Bug annotation = m.getAnnotation(Bug.class);
                    bugId = annotation.id();
                }
            }
            if (bugId != null && !Configuration.get(Parameter.JIRA_URL).isEmpty()) {
                String bugUrl = Configuration.get(Parameter.JIRA_URL) + "/browse/" + bugId;
                LOG.info("Bug URL retrieved: " + bugUrl);

                try {
                    Issue bug = jira.getIssue(bugId);
                    return String.format("Bug %s \"%s\" with status \"%s\" associated", bugUrl, bug.getSummary(), bug.getStatus().getName());
                } catch (Exception e) {
                    LOG.error("Exception during retrieving bug info: " + e.getMessage());
                    LOG.debug("Exception during retrieving bug info.", e);
                    return null;
                }
            }
        }
        return null;
    }

    public synchronized static void setKnownIssue(String jiraId) {
        setKnownIssue(jiraId, null);
    }

    public synchronized static void setKnownIssue(String jiraId, String description) {
        setKnownIssue(jiraId, description, false);
    }

    public synchronized static void setKnownIssue(String jiraId, String description, boolean blocker) {
        BaseWorkItem workItem = new BaseWorkItem(jiraId, description, blocker);
        knownIssue.set(workItem);
    }

    public static BaseWorkItem getKnownIssue() {
        return knownIssue.get();
    }

    private static void clearKnownIssue() {
        knownIssue.remove();
    }

    public static void clearJiraArtifacts() {
        clearTickets();
        clearKnownIssue();
    }

}
