package com.qaprosoft.carina.core.foundation.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

/*
 *  This appender logs groups test outputs by test method so they don't mess up each other even they runs in parallel.
 */
public class ThreadLogAppender extends AppenderSkeleton
{
	private final ConcurrentHashMap<String, BufferedWriter> test2file = new ConcurrentHashMap<String, BufferedWriter>();

	@Override
	public synchronized void append(LoggingEvent event)
	{
		try
		{
			String test = TestNamingUtil.getTestByThread(Thread.currentThread().getId());
			if(test == null)
			{
				return;
			}
			BufferedWriter fw = test2file.get(test);
			if (fw == null)
			{
			    File testLogFile = new File(ReportContext.getTestDir(test) + "/test.log");
			    if (!testLogFile.exists()) testLogFile.createNewFile();
				fw = new BufferedWriter(new FileWriter(testLogFile));
				test2file.put(test, fw);
			}
			fw.write(event.getMessage().toString());
			fw.write("\n");
			fw.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void closeResource(String test)
	{
		try
		{
			test2file.get(test).close();
			test2file.remove(test);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void closeResources()
	{
		for(String test : test2file.keySet())
		{
			closeResource(test);
		}
	}
	
	@Override
	public void close()
	{
	}

	@Override
	public boolean requiresLayout()
	{
		return false;
	}
}
