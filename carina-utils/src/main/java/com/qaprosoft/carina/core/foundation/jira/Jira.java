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
package com.qaprosoft.carina.core.foundation.jira;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/*
 * Jira
 * 
 * @author Alex Khursevich
 */
public class Jira {
    private static final int MAX_LENGTH = 45;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected static ThreadLocal<List<String>> jiraTickets = new ThreadLocal<>();

    private static void clearTickets() {
        jiraTickets.remove();
    }

    public static void setTickets(List<String> tickets) {
        List<String> tempTickets = new ArrayList<String>();
        for (String ticket : tickets) {
            tempTickets.add(parseTicket(ticket));
        }
        
        jiraTickets.set(tempTickets);
    }

    public static void setTickets(String... tickets) {
        List<String> tempTickets = new ArrayList<String>();
        for (String ticket : tickets) {
            tempTickets.add(parseTicket(ticket));
        }
        setTickets(tempTickets);
    }

    @SuppressWarnings("unlikely-arg-type")
    public static List<String> getTickets(ITestResult result) {
        // return any specified jira tickets by tests
        if (jiraTickets.get() != null) {
            return jiraTickets.get();
        }

        List<String> tickets = new ArrayList<String>();

        if (result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.JIRA_TICKET) != null) {
            tickets.add(
                    parseTicket(
                            result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.JIRA_TICKET)));
        }
        if (result.getMethod().getDescription() != null && result.getMethod().getDescription().contains(SpecialKeywords.JIRA_TICKET)) {
            tickets.clear();
            String description = result.getMethod().getDescription();
            
            if (description.split("#").length > 1) {
	            try {
	                tickets.add(parseTicket(description.split("#")[1].trim()));
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

    public static void clearJiraArtifacts() {
        clearTickets();
    }
    
    private static String parseTicket(String ticket) {
        /*
        #938 jira ticket allow anomalies in registration logic
        JIRA# and space, i.e.
        "JIRA#TICKET111, JIRA bla-bla" -> "TICKET111"
        "JIRA# TICKET111, JIRA bla-bla" -> "TICKET111"
        */
        if (ticket.contains(",")) {
            ticket = ticket.split(",")[0];
        }
        if (ticket.contains(" ")) {
            ticket = ticket.split(" ")[0];
        }
        
        if (ticket.length() > 45) {
            LOG.error("Too big jira ticket will be cut (45 chars max!) Ticket: '" + ticket +"';");
            ticket = ticket.substring(0, MAX_LENGTH);
        }
        return ticket;
    }


}
