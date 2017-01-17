package com.qaprosoft.carina.core.foundation.report;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.ISuite;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.retry.RetryCounter;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.utils.ownership.Ownership;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.zafira.client.model.config.Argument;
import com.qaprosoft.zafira.client.model.config.Configuration;
import com.qaprosoft.zafira.listener.IConfigurator;

/**
 * Carina-based implementation of IConfigurator that provides better integration with Zafira reporting tool.
 * 
 * @author akhursevich
 */
public class ZafiraConfigurator implements IConfigurator
{
	private List<String> uniqueKeys = Arrays.asList(R.CONFIG.get("unique_testrun_fields").split(","));
	
	@Override
	public Configuration getConfiguration()
	{
		Configuration conf = new Configuration();
		
		for (Parameter parameter : Parameter.values()) 
		{
			conf.getArg().add(buildArgument(parameter.getKey(), R.CONFIG.get(parameter.getKey())));
		}
		
		// add custom arguments from browserStack
		conf.getArg().add(buildArgument("platform", R.CONFIG.get("os")));
		conf.getArg().add(buildArgument("platform_version", R.CONFIG.get("os_version")));
		
		// add custom arguments from current mobile device
		Device device = DevicePool.getDevice();
		if (!device.getName().isEmpty()) 
		{
			conf.getArg().add(buildArgument("device", device.getName()));
			conf.getArg().add(buildArgument("platform", device.getOs()));
			conf.getArg().add(buildArgument("platform_version", device.getOsVersion()));
		}
		
		return conf;
	}
	
	private Argument buildArgument(String key, String value)
	{
		Argument arg = new Argument();
		arg.setKey(key);
		arg.setValue(value);
		arg.setUnique(uniqueKeys.contains(key));
		return arg;
	}

	@Override
	public String getOwner(ISuite suite)
	{
		String owner = suite.getParameter("suiteOwner");
		return owner != null ? owner : "";
	}

	@Override
	public String getOwner(ITestResult test)
	{
		// TODO: re-factor that
		return Ownership.getMethodOwner(test);
	}

	@Override
	public String getTestName(ITestResult test)
	{
		// TODO: avoid TestNamingUtil
		return TestNamingUtil.getCanonicalTestName(test);
	}

	@Override
	public String getTestMethodName(ITestResult test)
	{
		// TODO: avoid TestNamingUtil
		return TestNamingUtil.getCanonicalTestMethodName(test);
	}

	@Override
	public String getLogURL(ITestResult test)
	{
		return ReportContext.getTestLogLink(getTestName(test));
	}

	@Override
	public String getDemoURL(ITestResult test)
	{
		return ReportContext.getTestScreenshotsLink(getTestName(test));
	}

	@Override
	public List<String> getTestWorkItems(ITestResult test)
	{
		return Jira.getTickets(test);
	}

	@Override
	public int getRunCount(ITestResult test)
	{
		return RetryCounter.getRunCount(getTestName(test));
	}

	@Override
	public Map<String, Long> getTestMetrics(ITestResult test)
	{
		return Timer.readAndClear();
	}

	@Override
	public boolean isClassMode()
	{
		return "class_mode".equalsIgnoreCase(R.CONFIG.get("driver_mode"));
	}
}