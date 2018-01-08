/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.report;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.log.ThreadLogAppender;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.FileManager;

/*
 	Be careful with LOGGER usage here because potentially it could do recursive call together with ThreadLogAppender functionality
 */

public class ReportContext
{
	private static final Logger LOGGER = Logger.getLogger(ReportContext.class);

	public static final String ARTIFACTS_FOLDER = "artifacts";

	public static final String TEMP_FOLDER = "temp";

	private static File baseDirectory = null;

	private static File tempDirectory;

	private static File artifactsDirectory;

	private static File metaDataDirectory;

	private static long rootID;

	private static final ThreadLocal<File> testDirectory = new ThreadLocal<File>();
	
	private static final ExecutorService executor = Executors.newCachedThreadPool();

	public static long getRootID()
	{
		return rootID;
	}

	/**
	 * Crates new screenshot directory at first call otherwise returns created directory. Directory is specific for any
	 * new test suite launch.
	 * 
	 * @return root screenshot folder for test launch.
	 */
	public static synchronized File getBaseDir()
	{
		try
		{
			if (baseDirectory == null)
			{
				removeOldReports();
				File projectRoot = new File(String.format("%s/%s", URLDecoder.decode(System.getProperty("user.dir"), "utf-8"),
						Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY)));
				if (!projectRoot.exists())
				{
					boolean isCreated = projectRoot.mkdirs();
					if (!isCreated)
					{
						throw new RuntimeException("Folder not created: " + projectRoot.getAbsolutePath());
					}
				}
				rootID = System.currentTimeMillis();
				String directory = String.format("%s/%s/%d", URLDecoder.decode(System.getProperty("user.dir"), "utf-8"),
						Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), rootID);
				File baseDirectoryTmp = new File(directory);
				boolean isCreated = baseDirectoryTmp.mkdir();
				if (!isCreated)
				{
					throw new RuntimeException("Folder not created: " + baseDirectory.getAbsolutePath());
				}

				baseDirectory = baseDirectoryTmp;
			}
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException("Folder not created: " + baseDirectory.getAbsolutePath());
		}
		return baseDirectory;
	}

	public static boolean isBaseDirCreated()
	{
		return baseDirectory != null;
	}

	public static synchronized File getTempDir()
	{
		if (tempDirectory == null)
		{
			tempDirectory = new File(String.format("%s/%s", getBaseDir().getAbsolutePath(), TEMP_FOLDER));
			boolean isCreated = tempDirectory.mkdir();
			if (!isCreated)
			{
				throw new RuntimeException("Folder not created: " + tempDirectory.getAbsolutePath());
			}
		}
		return tempDirectory;
	}

	public static synchronized void removeTempDir()
	{
		if (tempDirectory != null)
		{
			try
			{
				FileUtils.deleteDirectory(tempDirectory);
			}
			catch (IOException e)
			{
				LOGGER.debug("Unable to remove artifacts temp directory!", e);
			}
		}
	}

	public static synchronized File getArtifactsFolder()
	{
		if (artifactsDirectory == null)
		{
			try
			{
				artifactsDirectory = new File(String.format("%s/%s", URLDecoder.decode(getBaseDir().getAbsolutePath(), "utf-8"), ARTIFACTS_FOLDER));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException("Folder not created: " + artifactsDirectory.getAbsolutePath());
			}
			boolean isCreated = artifactsDirectory.mkdir();
			if (!isCreated)
			{
				throw new RuntimeException("Folder not created: " + artifactsDirectory.getAbsolutePath());
			}
		}
		return artifactsDirectory;
	}

	public static synchronized File getMetadataFolder()
	{
		if (metaDataDirectory == null)
		{
			try
			{
				metaDataDirectory = new File(String.format("%s/%s/metadata", URLDecoder.decode(getBaseDir().getAbsolutePath(), "utf-8"), ARTIFACTS_FOLDER));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException("Artifacts metadata folder not created: " + metaDataDirectory.getAbsolutePath());
			}
			boolean isCreated = metaDataDirectory.mkdir();
			if (!isCreated)
			{
				throw new RuntimeException("Artifacts metadata folder not created: " + metaDataDirectory.getAbsolutePath());
			}
		}
		return metaDataDirectory;
	}

	/**
	 * Check that Artifacts Folder exists.
	 * 
	 * @return boolean
	 */
	public static boolean isArtifactsFolderExists()
	{
		try
		{
			File f = new File(String.format("%s/%s", getBaseDir().getAbsolutePath(), ARTIFACTS_FOLDER));
			if (f.exists() && f.isDirectory())
			{
				return true;
			}
		}
		catch (Exception e)
		{
			LOGGER.debug("Error happen during checking that Artifactory Folder exists or not. Error: " + e.getMessage());
		}
		return false;
	}

	public static List<File> getAllArtifacts()
	{
		return Arrays.asList(getArtifactsFolder().listFiles());
	}

	public static File getArtifact(String name)
	{
		File artifact = null;
		for (File file : getAllArtifacts())
		{
			if (file.getName().equals(name))
			{
				artifact = file;
				break;
			}
		}
		return artifact;
	}

	public static void deleteAllArtifacts()
	{
		for (File file : getAllArtifacts())
		{
			file.delete();
		}
	}

	public static void deleteArtifact(String name)
	{
		for (File file : getAllArtifacts())
		{
			if (file.getName().equals(name))
			{
				file.delete();
				break;
			}
		}
	}

	public static void saveArtifact(String name, InputStream source) throws IOException
	{
		File artifact = new File(String.format("%s/%s", getArtifactsFolder(), name));
		artifact.createNewFile();
		FileUtils.writeByteArrayToFile(artifact, IOUtils.toByteArray(source));
	}

	public static void saveArtifact(File source) throws IOException
	{
		File artifact = new File(String.format("%s/%s", getArtifactsFolder(), source.getName()));
		artifact.createNewFile();
		FileUtils.copyFile(source, artifact);
	}

	/**
	 * Creates new test directory at first call otherwise returns created directory. Directory is specific for any new
	 * test launch.
	 * 
	 * @return test log/screenshot folder.
	 */
	public static File getTestDir()
	{
		File testDir = testDirectory.get();
		if (testDir == null)
		{
			String uniqueDirName = UUID.randomUUID().toString();

			String directory = String.format("%s/%s", getBaseDir(), uniqueDirName);
			// System.out.println("First request for test dir. Just generate unique folder: " + directory);

			testDir = new File(directory);
			File thumbDir = new File(testDir.getAbsolutePath() + "/thumbnails");

			if (!thumbDir.mkdirs())
			{
				throw new RuntimeException("Test Folder(s) not created: " + testDir.getAbsolutePath() + " and/or " + thumbDir.getAbsolutePath());
			}
		}

		testDirectory.set(testDir);
		return testDir;
	}

	/**
	 * Rename test directory from unique number to valid human readable content using test method name.
	 * 
	 * @param test
	 *            name
	 * 
	 * @return test log/screenshot folder.
	 */
	public static File renameTestDir(String test)
	{
		File testDir = testDirectory.get();
		if (testDir != null)
		{
			// remove info about old directory to register new one for the next
			// test. Extra after method/class/suite custom messages will be
			// logged into the next test.log file
			testDirectory.remove();

			File newTestDir = new File(String.format("%s/%s", getBaseDir(), test.replaceAll("[^a-zA-Z0-9.-]", "_")));

			if (!newTestDir.exists())
			{
				// close ThreadLogAppender resources before renaming
				try
				{
					ThreadLogAppender tla = (ThreadLogAppender) Logger.getRootLogger().getAppender("ThreadLogAppender");
					if (tla != null)
					{
						tla.close();
					}

				}
				catch (NoSuchMethodError e)
				{
					LOGGER.error("Unable to redefine logger level due to the conflicts between log4j and slf4j!");
				}
				testDir.renameTo(newTestDir);
			}
		}
		else
		{
			LOGGER.error("Unexpected case with absence of test.log for '" + test + "'");
		}
		return testDir;
	}

	/**
	 * Removes emailable html report and oldest screenshots directories according to history size defined in config.
	 */
	private static void removeOldReports()
	{
		File baseDir = new File(String.format("%s/%s", System.getProperty("user.dir"),
				Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY)));

		if (baseDir.exists())
		{
			// remove old emailable report
			File reportFile = new File(String.format("%s/%s/%s", System.getProperty("user.dir"),
					Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), SpecialKeywords.HTML_REPORT));
			if (reportFile.exists())
			{
				reportFile.delete();
			}

			List<File> files = FileManager.getFilesInDir(baseDir);
			List<File> screenshotFolders = new ArrayList<File>();
			for (File file : files)
			{
				if (file.isDirectory() && !file.getName().startsWith("."))
				{
					screenshotFolders.add(file);
				}
			}

			int maxHistory = Configuration.getInt(Parameter.MAX_SCREENSHOOT_HISTORY);

			if (maxHistory > 0 && screenshotFolders.size() + 1 > maxHistory && maxHistory != 0)
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
					if (screenshotFolders.get(i).getName().equals("gallery-lib"))
					{
						continue;
					}
					try
					{
						FileUtils.deleteDirectory(screenshotFolders.get(i));
					}
					catch (IOException e)
					{
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	public static void removeTestScreenshots()
	{
		try
		{
			// Lists all files in folder
			File parentFolder = ReportContext.getTestDir();
			File fList[] = parentFolder.listFiles();
			for (int i = 0; i < fList.length; i++)
			{
				if (fList[i].getName().endsWith(".png") || fList[i].getName().endsWith(".mp4") || fList[i].getName().endsWith(".html"))
				{
					fList[i].delete();
				}
			}
			File thumbnailsFolder = new File(parentFolder.getAbsoluteFile() + "/thumbnails");
			FileUtils.deleteDirectory(thumbnailsFolder);
		}
		catch (Exception e)
		{
			LOGGER.error("Exception discovered during screenshots/video removing! " + e);
		}
	}

	public static void generateHtmlReport(String content)
	{
		generateHtmlReport(content, SpecialKeywords.HTML_REPORT);
	}

	public static void generateHtmlReport(String content, String emailableReport)
	{
		try
		{
			File reportFile = new File(String.format("%s/%s/%s", System.getProperty("user.dir"),
					Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY), emailableReport));

			// if file doesnt exists, then create it
			if (!reportFile.exists())
			{
				reportFile.createNewFile();
			}

			FileWriter fw = new FileWriter(reportFile.getAbsoluteFile());
			try
			{
				BufferedWriter bw = new BufferedWriter(fw);
				try
				{
					bw.write(content);
				}
				finally
				{
					if (bw != null)
					{
						bw.close();
					}
				}
			}
			finally
			{
				if (fw != null)
				{
					fw.close();
				}
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Returns URL for test artifacts folder.
	 * 
	 * @return - URL for test screenshot folder.
	 */
	public static String getTestArtifactsLink()
	{
		String link = "";
		if (!Configuration.get(Parameter.REPORT_URL).isEmpty())
		{
			// remove report url and make link relative
			// link = String.format("./%d/%s/report.html", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
			link = String.format("%s/%d/artifacts", Configuration.get(Parameter.REPORT_URL), rootID);
		}
		else
		{
			link = String.format("file://%s/%d/artifacts", baseDirectory, rootID);
		}

		return link;

	}

	/**
	 * Returns URL for test screenshot folder.
	 * 
	 * @param test
	 *            test name
	 * @return - URL for test screenshot folder.
	 */
	public static String getTestScreenshotsLink(String test)
	{
		// TODO: find unified solution for screenshots presence determination. Combine it with
		// AbstractTestListener->createTestResult code
		String link = "";
		if (FileUtils.listFiles(ReportContext.getTestDir(), new String[]
		{ "png" }, false).isEmpty())
		{
			// no png screenshot files at all
			return link;
		}

		// TODO: remove reference using "String test" argument
		if (!Configuration.get(Parameter.REPORT_URL).isEmpty())
		{
			// remove report url and make link relative
			// link = String.format("./%d/%s/report.html", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
			link = String.format("%s/%d/%s/report.html", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		else
		{
			// TODO: it seems like defect
			link = String.format("file://%s/%s/report.html", baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}

		return link;

	}

	/**
	 * Returns URL for test log.
	 * 
	 * @param test
	 *            test name
	 * @return - URL to test log folder.
	 */
	// TODO: refactor removing "test" argument
	public static String getTestLogLink(String test)
	{
		String link = "";
		File testLogFile = new File(ReportContext.getTestDir() + "/" + "test.log");
		if (!testLogFile.exists())
		{
			// no test.log file at all
			return link;
		}

		if (!Configuration.get(Parameter.REPORT_URL).isEmpty())
		{
			// remove report url and make link relative
			// link = String.format("./%d/%s/test.log", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
			link = String.format("%s/%d/%s/test.log", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		else
		{
			// TODO: it seems like defect
			link = String.format("file://%s/%s/test.log", baseDirectory, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}

		return link;
	}

	/**
	 * Returns URL for test video record.
	 * 
	 * @param test
	 *            test name
	 * @return - URL to test log folder.
	 */
	// TODO: refactor removing "test" argument
	public static String getTestVideoLink(String test)
	{
		String link = "";
		if (!Configuration.get(Parameter.REPORT_URL).isEmpty())
		{
			// remove report url and make link relative
			// link = String.format("./%d/%s/video.mp4", rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
			link = String.format("%s/%d/%s/video.mp4", Configuration.get(Parameter.REPORT_URL), rootID, test.replaceAll("[^a-zA-Z0-9.-]", "_"));
		}
		else
		{
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
		if (!Configuration.get(Parameter.REPORT_URL).isEmpty())
		{
			// remove report url and make link relative
			// link = String.format("./%d/report.html", rootID);
			link = String.format("%s/%d/report.html", Configuration.get(Parameter.REPORT_URL), rootID);
		}
		else
		{
			link = String.format("file://%s/report.html", baseDirectory);
		}

		return link;
	}

	/**
	 * Returns URL for cucumber report.
	 * 
	 * @param CucumberReportFolderName
	 *            String
	 * @return - URL to test log folder.
	 */
	public static String getCucumberReportLink(String CucumberReportFolderName)
	{
		return getCucumberReportLink(CucumberReportFolderName, "");
	}

	/**
	 * Returns URL for cucumber report.
	 * 
	 * @param CucumberReportFolderName
	 *            String
	 * @param subfolder
	 *            String. Add subfolder if it required.
	 * @return - URL to test log folder.
	 */
	public static String getCucumberReportLink(String CucumberReportFolderName, String subfolder)
	{

		String link = "";
		// String subfolder = "cucumber-html-reports";
		if (!Configuration.get(Parameter.REPORT_URL).isEmpty())
		{
			// remove report url and make link relative
			// link = String.format("./%d/report.html", rootID);
			String report_url = Configuration.get(Parameter.REPORT_URL);
			if (report_url.contains("n/a"))
			{
				LOGGER.error("Contains n/a. Replace it.");
				report_url = report_url.replace("n/a", "");
			}
			link = String.format("%s/%d/%s/%s/%s/feature-overview.html", report_url, rootID, ARTIFACTS_FOLDER, CucumberReportFolderName, subfolder);
		}
		else
		{
			link = String.format("file://%s/%s/%s/feature-overview.html", artifactsDirectory, CucumberReportFolderName, subfolder);
		}

		return link;
	}

	/**
	 * Saves screenshot with thumbnail.
	 * @param screenshot - {@link BufferedImage} file to save
	 */
	public static String saveScreenshot(BufferedImage screenshot)
	{
		long now = System.currentTimeMillis();
		
		executor.execute(new ImageSaverTask(screenshot, String.format("%s/%d.png", getTestDir().getAbsolutePath(), now), 
				Configuration.getInt(Parameter.BIG_SCREEN_WIDTH), Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT)));
		
		executor.execute(new ImageSaverTask(screenshot, String.format("%s/thumbnails/%d.png", getTestDir().getAbsolutePath(), now), 
				Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH), Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT)));
		
		return String.format("%d.png", now);
	}
	
	/**
	 * Asynchronous image saver task.
	 */
	private static class ImageSaverTask implements Runnable
	{
		private BufferedImage image;
		private String path;
		private Integer width;
		private Integer height;

		public ImageSaverTask(BufferedImage image, String path, Integer width, Integer height)
		{
			this.image = image;
			this.path = path;
			this.width = width;
			this.height = height;
		}

		@Override
		public void run()
		{
			try
			{
				if(width > 0 && height > 0)
				{
					BufferedImage resizedImage = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, width, height, Scalr.OP_ANTIALIAS);
					if (resizedImage.getHeight() > height)
					{
						resizedImage = Scalr.crop(resizedImage, resizedImage.getWidth(), height);
					}
					ImageIO.write(resizedImage, "PNG", new File(path));
				}
				else
				{
					ImageIO.write(image, "PNG", new File(path));
				}
				
			}
			catch (Exception e) 
			{
				LOGGER.error("Unable to save screenshot: " + e.getMessage());
			}
		}
	}
}
