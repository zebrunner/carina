package com.qaprosoft.carina.browsermobproxy;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.proxy.SystemProxy;

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
		
		ProxyPool.setupBrowserMobProxy();
		SystemProxy.setupProxy();

		BrowserMobProxy proxy = ProxyPool.getProxy();
		proxy.addHeader(header, headerValue);
	}
	
	@Test
	public void testIsBrowserModStarted() {
		Assert.assertTrue(ProxyPool.getProxy().isStarted(), "BrowserMobProxy is not started!");
	}
	
	@Test
	public void testBrowserModProxySystemIntegration() {
		Assert.assertEquals(Configuration.get(Parameter.PROXY_HOST), System.getProperty("http.proxyHost"));
		Assert.assertEquals(Configuration.get(Parameter.PROXY_PORT), System.getProperty("http.proxyPort"));
	}

	@Test
	public void testBrowserModProxyHeader() {
		Map<String, String> headers = ProxyPool.getProxy().getAllHeaders();
		Assert.assertTrue(headers.containsKey(header), "There is no custom header: " + header);
		Assert.assertTrue(headers.get(header).equals(headerValue), "There is no custom header value: " + headerValue);
		
		ProxyPool.getProxy().removeHeader(header);
		if (ProxyPool.getProxy().getAllHeaders().size() != 0) {
			Assert.fail("Custom header was not removed: " + header);
		}
	}
}
