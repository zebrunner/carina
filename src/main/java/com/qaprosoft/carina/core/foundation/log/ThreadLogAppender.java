package com.qaprosoft.carina.core.foundation.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

/*
 *  This appender logs groups test outputs by test method so they don't mess up each other even they runs in parallel.
 */
public class ThreadLogAppender extends AppenderSkeleton
{
	private final ConcurrentHashMap<String, BufferedWriter> test2file = new ConcurrentHashMap<String, BufferedWriter>();

	@Override
	public synchronized void append(LoggingEvent event)
	{
		int count = 0;
		//wait 10 seconds until folder structure is create
		while (!ReportContext.isBaseDireCreated() && ++count<3) {
			pause(1);
		}
		if (!ReportContext.isBaseDireCreated()) {
			System.out.println("Folder structure is not created yet!");
			return;
		}
		try
		{
			if (!TestNamingUtil.isTestNameRegistered()) {
				System.out.println("don't write any message into the log if thread is not associated anymore with test");
				//don't write any message into the log if thread is not associated anymore with test 
				return;
			}
			String test = TestNamingUtil.getTestNameByThread();
			if (test == null) {
				System.out.println("TestNamingUtil.getTestNameByThread returned test=null!");
				return;
			} else {
				System.out.println("test: " + test);
			}
			BufferedWriter fw = test2file.get(test);
			if (fw == null)
			{
			    File testLogFile = new File(ReportContext.getTestDir(test) + "/test.log");
			    if (!testLogFile.exists()) testLogFile.createNewFile();
				fw = new BufferedWriter(new FileWriter(testLogFile));
				test2file.put(test, fw);
			}
			if (event != null) {
				//append time, thread, class name and device name if any
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss"); //2016-05-26 04:39:16
				String time = dateFormat.format(event.getTimeStamp());
				//System.out.println("time: " + time);
				
				long threadId = Thread.currentThread().getId();
				//System.out.println("thread: " + threadId);
				String fileName = event.getLocationInformation().getFileName();
				//System.out.println("fileName: " + fileName);
				
				String logLevel = event.getLevel().toString();
				
				Device device = DevicePool.getDevice();

				String deviceName = "";
				if (device != null) {
					deviceName = " [" + device.getName() + "] ";
					//System.out.println("device: " + device.getName());
				}
				
				String message = "[%s] [%s] [%s] [%s]%s %s";
				fw.write(String.format(message, time, fileName, threadId, logLevel, deviceName, event.getMessage().toString()));
			} else {
				fw.write("null");
			}
			fw.write("\n");
			fw.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void closeResource(String test)
	{
		try
		{
			if (test2file.get(test) != null) {
				test2file.get(test).close();
				test2file.remove(test);
			}
		} catch (Exception e)
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
	
	private void pause(long timeout) {
		try {
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
