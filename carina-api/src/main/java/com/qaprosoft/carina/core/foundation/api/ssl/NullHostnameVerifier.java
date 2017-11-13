package com.qaprosoft.carina.core.foundation.api.ssl;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;

@SuppressWarnings("deprecation")
public class NullHostnameVerifier implements X509HostnameVerifier
{
	@Override
	public boolean verify(String hostname, SSLSession session)
	{
		// do nothing
		return true;
	}

	@Override
	public void verify(String arg0, SSLSocket arg1) throws IOException
	{
		// do nothing
	}

	@Override
	public void verify(String arg0, X509Certificate arg1) throws SSLException
	{
		// do nothing
	}

	@Override
	public void verify(String arg0, String[] arg1, String[] arg2) throws SSLException
	{
		// do nothing
	}
}
