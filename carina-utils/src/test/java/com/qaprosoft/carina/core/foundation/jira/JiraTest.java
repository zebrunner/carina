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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

public class JiraTest {
    private static final String customTicket1 = "bug1";
    private static final String customTicket2 = "bug2";
    
    private static final String customTicket3 = "bug3 with spaces";
    private static final String customTicket3_verify = "bug3";
    
    private static final String customTicket4 = "bug4, with comma";
    private static final String customTicket4_verify = "bug4";
    
    private static final String customTicket5 = "bug5 , with comma after space";
    private static final String customTicket5_verify = "bug5";

    private static final String customTicket6 = "Ticket From Test Description";
    private static final String customTicket6_verify = "Ticket";
    
    private static final String ticket50Char        = "1234567890123456789012345678901234567890123456";
    private static final String ticket50Char_verify = "123456789012345678901234567890123456789012345";

    @Test
    public void testJiraTickets() {
        Assert.assertNull(Jira.jiraTickets.get());

        Jira.setTickets(customTicket1);
        Assert.assertEquals(Jira.jiraTickets.get().size(), 1);

        Jira.clearJiraArtifacts();
        Assert.assertNull(Jira.jiraTickets.get());

        List<String> tickets = new ArrayList<String>();
        tickets.add(customTicket1);
        tickets.add(customTicket2);
        Jira.setTickets(tickets);
        Assert.assertEquals(Jira.jiraTickets.get().size(), 2);
        
        tickets.add(customTicket3);
        Jira.setTickets(tickets);
        Assert.assertTrue(Jira.jiraTickets.get().contains(customTicket3_verify));
        
        tickets.add(customTicket4);
        Jira.setTickets(tickets);
        Assert.assertTrue(Jira.jiraTickets.get().contains(customTicket4_verify));
        
        tickets.add(customTicket5);
        Jira.setTickets(tickets);
        Assert.assertTrue(Jira.jiraTickets.get().contains(customTicket5_verify));
        
        Assert.assertEquals(Jira.jiraTickets.get().size(), 5);
        
        tickets.add(ticket50Char);
        Jira.setTickets(tickets);
        
        Assert.assertEquals(Jira.jiraTickets.get().size(), 6);
        Assert.assertTrue(Jira.jiraTickets.get().contains(ticket50Char_verify));
    }
    
    @Test(description = "JIRA# " + customTicket6)
    public void testJiraTicketFromTestDescriptionAnnotation() {
        // do nothing. verification is in AfterMethod
    }

    @AfterMethod
    public void testGetJiraTickets(ITestResult result) {
        if (result.getMethod().getMethodName().equals("testJiraTickets")) {
            R.CONFIG.put("track_known_issues", "true");
            List<String> tickets = Jira.getTickets(result);
            Assert.assertTrue(tickets.contains(customTicket1));
            Assert.assertTrue(tickets.contains(customTicket2));

            Assert.assertTrue(Jira.isRetryDisabled(result));
        }

        if (result.getMethod().getMethodName().equals("testJiraTicketFromTestDescriptionAnnotation")) {
            List<String> tickets = Jira.getTickets(result);
            // #938 parse all spaces and commas before registration!
            Assert.assertTrue(tickets.contains(customTicket6_verify));

        }

    }
}
