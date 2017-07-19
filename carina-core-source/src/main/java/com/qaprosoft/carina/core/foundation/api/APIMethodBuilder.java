package com.qaprosoft.carina.core.foundation.api;

import java.io.File;
import java.io.PrintStream;
import java.util.UUID;

import com.jayway.restassured.filter.log.RequestLoggingFilter;
import com.jayway.restassured.filter.log.ResponseLoggingFilter;
import com.qaprosoft.carina.core.foundation.report.ReportContext;

public class APIMethodBuilder
{
	private File temp;
	private PrintStream ps;
	
	public APIMethodBuilder()
	{
		temp = new File(String.format("%s/%s.tmp", ReportContext.getTempDir().getAbsolutePath(), UUID.randomUUID()));
		try
		{
			ps = new PrintStream(temp);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public <T extends AbstractApiMethod> T build(T method)
	{
		method.getRequest().filter(new RequestLoggingFilter(ps)).filter(new ResponseLoggingFilter(ps));
		return method;
	}

	public File getTempFile()
	{
		return temp;
	}

	public void close()
	{
		try
		{
			ps.close();
			temp.delete();
		}
		catch(Exception e) {}
	}
}
