package com.qaprosoft.carina.core.foundation.api.ssl;

import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;
import com.qaprosoft.carina.core.foundation.api.ssl.SSLContextBuilder;

public class PutDocTest
{
	// @Test
	public void testPath()
	{
		PutDocMethod putDocMethod = new PutDocMethod();
		putDocMethod.expectResponseStatus(HttpResponseStatusType.OK_200);
		putDocMethod.setSSLContext(new SSLContextBuilder("src/test/resources/keysecure", true).createSSLContext());
		putDocMethod.callAPI();
	}

	// @Test
	public void testClasspath()
	{
		PutDocMethod putDocMethod = new PutDocMethod();
		putDocMethod.expectResponseStatus(HttpResponseStatusType.OK_200);
		putDocMethod.setSSLContext(new SSLContextBuilder(true).createSSLContext());
		putDocMethod.callAPI();
	}

	// @Test
	public void testDefaultTLS()
	{
		PutDocMethod putDocMethod = new PutDocMethod();
		putDocMethod.expectResponseStatus(HttpResponseStatusType.OK_200);
		putDocMethod.setDefaultTLSSupport();
		putDocMethod.callAPI();
	}

	// @Test
	public void testCfgParam()
	{
		PutDocMethod putDocMethod = new PutDocMethod();
		putDocMethod.expectResponseStatus(HttpResponseStatusType.OK_200);
		putDocMethod.setSSLContext(new SSLContextBuilder(true).createSSLContext());
		putDocMethod.callAPI();
	}
}
