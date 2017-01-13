package com.qaprosoft.carina.core.foundation.browsermob;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.http.HttpClient;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;

import net.lightbody.bmp.BrowserMobProxy;

public class BrowserMobTest {
	private static String header = "my_header";
	private static String headerValue = "my_value";

	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		// do nothing
		R.CONFIG.put("browsermob_proxy", "true");

		HttpClient.setupProxy();

		BrowserMobProxy proxy = DriverPool.getBrowserMobProxy();
		proxy.addHeader(header, headerValue);
	}

	@Test
	public void testBrowserModheader() {
		Map<String, String> headers = DriverPool.getBrowserMobProxy().getAllHeaders();
		Assert.assertTrue(headers.containsKey(header), "Tehre is no custom header: " + header);
		Assert.assertTrue(headers.get(header).equals(headerValue), "Tehre is no custom header value: " + headerValue);
	}

}
