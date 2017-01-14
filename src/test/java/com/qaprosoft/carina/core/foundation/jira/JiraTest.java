package com.qaprosoft.carina.core.foundation.jira;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

public class JiraTest {

	@Test
	public void testJiraTickets() {
		Assert.assertNull(Jira.jiraTickets.get());

		Jira.setTickets("bug1");
		Assert.assertEquals(Jira.jiraTickets.get().size(), 1);

		Jira.clearTickets();
		Assert.assertNull(Jira.jiraTickets.get());

		List<String> tickets = new ArrayList<String>();
		tickets.add("bug1");
		tickets.add("bug2");
		Jira.setTickets(tickets);
		Assert.assertEquals(Jira.jiraTickets.get().size(), 2);
	}

	@AfterMethod
	public void testGetJiraTickets(ITestResult result) {
		R.CONFIG.put("track_known_issues", "true");
		List<String> tickets = Jira.getTickets(result);
		Assert.assertTrue(tickets.contains("bug1"));
		Assert.assertTrue(tickets.contains("bug2"));

		Assert.assertTrue(Jira.isRetryDisabled(result));
	}
}
