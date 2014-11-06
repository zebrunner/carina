/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.FileManager;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;


public class ReportContext
{
	private static final Logger LOGGER = Logger.getLogger(ReportContext.class);

	private static File baseDirectory;

	private static File tempDirectory;
	
	private static long rootID;

	/**
	 * Crates new screenshot directory at first call otherwise returns created
	 * directory. Directory is specific for any new test suite launch.
	 * 
	 * @return root screenshot folder for test launch.
	 */
	public static synchronized File getBaseDir()
	{
		if (baseDirectory == null)
		{
			File projectRoot = new File(String.format("%s/%s", System.getProperty("user.dir"),
					Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY)));
			if (!projectRoot.exists())
			{
				boolean isCreated = projectRoot.mkdirs();
				if (!isCreated)
				{
					throw new RuntimeException("Folder not created");
				}
			}

			rootID = System.currentTimeMillis();
			String directory = String.format("%s/%s/%d", System.getProperty("user.dir"),
					Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), rootID);
			baseDirectory = new File(directory);
			boolean isCreated = baseDirectory.mkdir();
			if (!isCreated)
			{
				throw new RuntimeException("Folder not created");
			}
		}
		return baseDirectory;
	}
	
	public static synchronized File getTempDir()
	{
		if (tempDirectory == null)
		{
			tempDirectory = new File(getBaseDir().getAbsolutePath() + "/temp");
			boolean isCreated = tempDirectory.mkdir();
			if (!isCreated)
			{
				throw new RuntimeException("Folder not created");
			}
		}
		return tempDirectory;
	}

	/**
	 * Crates new screenshot directory at first call otherwise returns created
	 * directory. Directory is specific for any new test launch.
	 * 
	 * @param test
	 *            = name of test.
	 * @return test screenshot folder.
	 */
	public static File getTestDir(String test)
	{
		String directory = String.format("%s/%s", getBaseDir(), test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		File screenDir = new File(directory);
		if (!screenDir.exists())
		{
			boolean isCreated = screenDir.mkdir();
			if (!isCreated)
			{
				throw new RuntimeException("Folder not created");
			}
			File thumbDir = new File(screenDir.getAbsolutePath() + "/thumbnails");
			isCreated = thumbDir.mkdir();
			if (!isCreated)
			{
				throw new RuntimeException("Folder not created");
			}
		}
		return screenDir;
	}

	/**
	 * Removes emailable html report and oldest screenshots directories according to history size defined
	 * in config.
	 */
	public static void removeOldReports()
	{
		File baseDir = new File(String.format("%s/%s", System.getProperty("user.dir"),
				Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY)));
		
		if (baseDir.exists())
		{
			//remove old emailable report
			File reportFile = new File(String.format("%s/%s/%s", System.getProperty("user.dir"),
					Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), SpecialKeywords.HTML_REPORT));
			// if file doesnt exists, then create it
			if (reportFile.exists()) {
				reportFile.delete();
			}
			
			List<File> files = FileManager.getFilesInDir(baseDir);
			List<File> screenshotFolders = new ArrayList<File>();
			for(File file : files)
			{
				if(file.isDirectory() && !file.getName().startsWith("."))
				{
					screenshotFolders.add(file);
				}
			}
			
			int maxHistory = Configuration.getInt(Parameter.MAX_SCREENSHOOT_HISTORY);

			if (screenshotFolders.size() > maxHistory && maxHistory != 0)
			{
				Comparator<File> comp = new Comparator<File>()
				{
					@Override
					public int compare(File file1, File file2)
					{
						return file2.getName().compareTo(file1.getName());
					}
				};
				Collections.sort(screenshotFolders, comp);
				for (int i = maxHistory - 1; i < screenshotFolders.size(); i++)
				{
					try
					{
						FileUtils.deleteDirectory(screenshotFolders.get(i));
					}
					catch (IOException e)
					{
						LOGGER.error(e.getMessage());
					}
				}
			}
		}
		//create base dir and initialize rootID
		getBaseDir();		
	}

	public static void removeTestReport(String test)
	{
		try
		{
			File toDelete = new File(ReportContext.getBaseDir() + "/" + test.replaceAll("[^a-zA-Z0-9.-]", "_"));
			FileUtils.deleteDirectory(toDelete);
		}
		catch (IOException e)
		{
			LOGGER.error(e.getMessage());
		}
	}
	
	public static void generateHtmlReport(String content) {
		try {
			File reportFile = new File(String.format("%s/%s/%s", System.getProperty("user.dir"),
					Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), SpecialKeywords.HTML_REPORT));

			// if file doesnt exists, then create it
			if (!reportFile.exists()) {
				reportFile.createNewFile();
			}
 
			FileWriter fw = new FileWriter(reportFile.getAbsoluteFile());
			try
			{
				BufferedWriter bw = new BufferedWriter(fw);
				try
				{
					bw.write(content);
				} finally
				{
					if (bw != null)
					{
						bw.close();
					}
				}
			} finally
			{
				if (fw != null)
				{
					fw.close();
				}
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Returns URL for test screenshot folder.
	 * 
	 * @param test
	 * @return - URL for test screenshot folder.
	 */
	public static String getTestScreenshotsLink(String test)
	{
		String link = "";
		if (!Configuration.get(Parameter.REPORT_URL).equalsIgnoreCase("null")) {
			//remove report url and make link relative
			//link = String.format("./%d/%s/report.html", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
			link = String.format("%s/%d/%s/report.html", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		else {
			link = String.format("file://%s/%s/report.html", baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		
		return link;
		
	}

	/**
	 * Returns URL for test log.
	 * 
	 * @param test
	 * @return - URL to test log folder.
	 */
	public static String getTestLogLink(String test)
	{
		String link = "";
		if (!Configuration.get(Parameter.REPORT_URL).equalsIgnoreCase("null")) {
			//remove report url and make link relative
			//link = String.format("./%d/%s/test.log", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
			link = String.format("%s/%d/%s/test.log", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		else {
			link = String.format("file://%s/%s/test.log", baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		
		return link;
	}

	
	/**
	 * Returns URL for test video record.
	 * 
	 * @param test
	 * @return - URL to test log folder.
	 */
	public static String getTestVideoLink(String test)
	{
		String link = "";
		if (!Configuration.get(Parameter.REPORT_URL).equalsIgnoreCase("null")) {
			//remove report url and make link relative
			//link = String.format("./%d/%s/video.mp4", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
			link = String.format("%s/%d/%s/video.mp4", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		else {
			link = String.format("file://%s/%s/video.mp4", baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		
		return link;
	}	
	/**
	 * Returns URL for performance report.
	 * 
	 * @return - URL to test log folder.
	 */
	public static String getPerformanceReportLink()
	{
		
		String link = "";
		if (!Configuration.get(Parameter.REPORT_URL).equalsIgnoreCase("null")) {
			//remove report url and make link relative
			//link = String.format("./%d/report.html", rootID);
			link=String.format("%s/%d/report.html", Configuration.get(Parameter.REPORT_URL), rootID);
		}
		else {
			link = String.format("file://%s/report.html", baseDirectory);
		}
		
		return link;
	}
}
