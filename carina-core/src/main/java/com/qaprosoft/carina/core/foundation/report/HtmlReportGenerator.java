/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.FileManager;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.ZipManager;

public class HtmlReportGenerator
{
	private static final Logger LOGGER = Logger.getLogger(HtmlReportGenerator.class);
	
	private static final int MAX_IMAGE_TITLE = 300;
	
	private static final String REPORT_NAME = "/report.html";
	private static final String GALLERY_ZIP = "gallery-lib.zip";
	private static final String TITLE = "Test steps demo";

	public static void generate(String rootDir)
	{
		copyGalleryLib();
		List<File> folders = FileManager.getFilesInDir(new File(rootDir));
		for (File folder : folders)
		{
			if(!ReportContext.ARTIFACTS_FOLDER.equals(folder.getName()))
			{
				List<File> images = FileManager.getFilesInDir(folder);
				createReportAsHTML(folder, images);
			}
		}
	}

	private static synchronized void createReportAsHTML(File testFolder, List<File> images)
	{
		try
		{
			List<String> imgNames = new ArrayList<String>();
			for (File image : images)
			{
				imgNames.add(image.getName());
			}
			imgNames.remove("thumbnails");
			imgNames.remove("test.log");
			imgNames.remove("sql.log");
			if(imgNames.size() == 0) return;
			
			Collections.sort(imgNames);

			StringBuilder report = new StringBuilder();
			for (int i = 0; i < imgNames.size(); i++)
			{
				// convert toString
				String image = R.REPORT.get("image");
				
				image = image.replace("${image}", imgNames.get(i));
				image = image.replace("${thumbnail}", imgNames.get(i));
				if(i == imgNames.size() - 1)
				{
					image = image.replace("onload=\"\"", "onload=\"this.click()\"");
				}
				
				String title = TestLogCollector.getScreenshotComment(imgNames.get(i));
				if (title == null) 
				{
					title = "";
				}
				image = image.replace("${title}", StringUtils.substring(title, 0, MAX_IMAGE_TITLE));
				report.append(image);
			}
			//String wholeReport = R.REPORT.get("container").replace("${images}", report.toString());
			String wholeReport = R.REPORT.get("container").replace("${images}", report.toString());
			wholeReport = wholeReport.replace("${title}", TITLE);
			String folder = testFolder.getAbsolutePath();
			FileManager.createFileWithContent(folder + REPORT_NAME, wholeReport);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			LOGGER.error(e.getStackTrace().toString());
		}
	}

	private static void copyGalleryLib()
	{
		File reportsRootDir = new File(System.getProperty("user.dir") + "/" + Configuration.get(Parameter.PROJECT_REPORT_DIRECTORY));
		if (!new File(reportsRootDir.getAbsolutePath() + "/gallery-lib").exists())
		{
			try
			{
				InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(GALLERY_ZIP);
				ZipManager.copyInputStream(is, new BufferedOutputStream(new FileOutputStream(reportsRootDir.getAbsolutePath() + "/"
						+ GALLERY_ZIP)));
				ZipManager.unzip(reportsRootDir.getAbsolutePath() + "/" + GALLERY_ZIP, reportsRootDir.getAbsolutePath());
				File zip = new File(reportsRootDir.getAbsolutePath() + "/" + GALLERY_ZIP);
				zip.delete();
			}
			catch (Exception e)
			{
				LOGGER.error(e.getMessage());
			}
		}
	}
}
