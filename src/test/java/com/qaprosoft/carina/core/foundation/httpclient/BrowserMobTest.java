package com.qaprosoft.carina.core.foundation.httpclient;

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
		R.CONFIG.put("browsermob_port", "0");
		R.CONFIG.put("proxy_set_to_system", "true");
		
		HttpClient.setupProxy();

		BrowserMobProxy proxy = DriverPool.getProxy();
		proxy.addHeader(header, headerValue);
	}
	
	@Test
	public void testIsBrowserModStarted() {
		Assert.assertTrue(DriverPool.getProxy().isStarted(), "BrowserMobProxy is not started!");
	}
	
	@Test
	public void testBrowserModProxySystemIntegration() {
		Assert.assertEquals(HttpClient.getIpAddress(), System.getProperty("http.proxyHost"));
		Assert.assertEquals(Integer.toString(DriverPool.getProxy().getPort()), System.getProperty("http.proxyPort"));
	}

	@Test
	public void testBrowserModProxyHeader() {
		Map<String, String> headers = DriverPool.getProxy().getAllHeaders();
		Assert.assertTrue(headers.containsKey(header), "There is no custom header: " + header);
		Assert.assertTrue(headers.get(header).equals(headerValue), "There is no custom header value: " + headerValue);
		
		DriverPool.getProxy().removeHeader(header);
		if (DriverPool.getProxy().getAllHeaders().size() != 0) {
			Assert.fail("Custom header was not removed: " + header);
		}
	}
}
