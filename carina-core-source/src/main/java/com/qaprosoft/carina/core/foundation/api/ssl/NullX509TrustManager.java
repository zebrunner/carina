package com.qaprosoft.carina.core.foundation.api.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Created by mk on 6/30/15.
 */
public class NullX509TrustManager implements X509TrustManager
{

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
	{
		// do nothing
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
	{
		// do nothing
	}

	public X509Certificate[] getAcceptedIssuers()
	{
		return new X509Certificate[0];
	}
}
