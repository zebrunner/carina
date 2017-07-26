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
	private static final String customTicket3 = "Ticket From Test Description";

	@Test
	public void testJiraTickets() {
		Assert.assertNull(Jira.jiraTickets.get());

		Jira.setTickets(customTicket1);
		Assert.assertEquals(Jira.jiraTickets.get().size(), 1);

		Jira.clearTickets();
		Assert.assertNull(Jira.jiraTickets.get());

		List<String> tickets = new ArrayList<String>();
		tickets.add(customTicket1);
		tickets.add(customTicket2);
		Jira.setTickets(tickets);
		Assert.assertEquals(Jira.jiraTickets.get().size(), 2);
	}

	
	@Test(description="JIRA# " + customTicket3)
	public void testJiraTicketFromTestDescriptionAnnotation() {
		//do nothing. verification is in AfterMethod
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
			Assert.assertTrue(tickets.contains(customTicket3));

		}
		
	}
}
