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
import com.qaprosoft.carina.core.foundation.performance.TestStatistics;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.FileManager;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.ZipManager;

public class HtmlReportGenerator
{
	private static final Logger LOGGER = Logger.getLogger(HtmlReportGenerator.class);
	private static final String REPORT_NAME = "/report.html";
	private static final String GALLERY_ZIP = "gallery-lib.zip";
	private static final String TITLE = "Test steps demo";
	
	private static final String PERF_REPORT = R.REPORT.get("perf_report");
	private static final String TIME_GRAPH_DATA = "${time_graph_data}";
	private static final String SUCCESS_GRAPH_DATA = "${success_rate_data}";
	private static final String PERF_TABLE_DATA = "${perf_table_data}";
	
	private static final String PERF_TABLE = R.REPORT.get("perf_table");
	private static final String PERF_TITLE = "${title}";
	private static final String PERF_ENV = "${env}";
	private static final String PERF_FINISH_DATE = "${finish_date}";
	private static final String REPORT_URL = "${report_url}";
	private static final String PERF_DATA = "${result_rows}";
	private static final String PERF_TITLE_ROW = R.REPORT.get("perf_title_row");
	private static final String PERF_DATA_ROW = R.REPORT.get("perf_data_row");
	
	public synchronized static String generatePerformanceReport(String rootDir, List<TestStatistics> testStatistics)
	{
		String env = !Configuration.isNull(Parameter.ENV) ? Configuration.get(Parameter.ENV) : Configuration.get(Parameter.URL);
		String perfTable = PERF_TABLE.replace(PERF_TITLE, "Performance report")
								      .replace(PERF_ENV, env)
								      .replace(PERF_FINISH_DATE, DateUtils.now())
								      .replace(REPORT_URL, ReportContext.getPerformanceReportLink());
		
		StringBuilder tableData = new StringBuilder();
		StringBuilder timeGraphData = new StringBuilder();
		StringBuilder successRateGraphData = new StringBuilder();
		
		// Generate graph headers
		if(testStatistics != null && testStatistics.size() > 0)
		{
			TestStatistics ts = testStatistics.get(0);
			StringBuilder headers = new StringBuilder("['Configuration', ");
			headers.append(String.format("'%s', ", StringUtils.substringAfterLast(ts.getName(), ".")));
			successRateGraphData.append(StringUtils.removeEnd(headers.toString(), ",") + "], ");
			for(TestStatistics sts : ts.getSubTestStatistics().values())
			{
				headers.append(String.format("'-- %s', ", sts.getName()));
			}
			timeGraphData.append(StringUtils.removeEnd(headers.toString(), ",") + "], ");
		}
		
		if(testStatistics != null)
		{
			for(TestStatistics ts : testStatistics)
			{
				tableData.append(String.format(PERF_TITLE_ROW, ts.getUsers(), ts.getLoop()));
				
				tableData.append(String.format(PERF_DATA_ROW, StringUtils.substringAfterLast(ts.getName(), "."), 
						ts.getTestsCount(), ts.getFailuresCount(), ts.getSuccessRate(), ts.getAverageTime(), ts.getMinTime(), ts.getMaxTime()));
				
				timeGraphData.append(String.format("['%d | %d | %d', %d, ",  ts.getUsers(), ts.getLoop(), ts.getRumpup(), ts.getAverageTime()));
				successRateGraphData.append(String.format("['%d | %d | %d', %d], ",  ts.getUsers(), ts.getLoop(), ts.getRumpup(), ts.getSuccessRate()));
				
				for(TestStatistics sts : ts.getSubTestStatistics().values())
				{
					tableData.append(String.format(PERF_DATA_ROW, "-- " + sts.getName(), sts.getTestsCount(), sts.getFailuresCount(), 
							sts.getSuccessRate(), sts.getAverageTime(), sts.getMinTime(), sts.getMaxTime()));
					
					timeGraphData.append(String.format("%d, ", sts.getAverageTime()));
				}
				timeGraphData = new StringBuilder(StringUtils.removeEnd(timeGraphData.toString(), ",") + "], ");
			}
		}
		
		perfTable = perfTable.replace(PERF_DATA, tableData.toString());
		
		String perfReport = PERF_REPORT.replace(PERF_TABLE_DATA, perfTable);
		perfReport = perfReport.replace(SUCCESS_GRAPH_DATA, StringUtils.removeEnd(successRateGraphData.toString(), ", "));
		perfReport = perfReport.replace(TIME_GRAPH_DATA, StringUtils.removeEnd(timeGraphData.toString(), ", "));
		FileManager.createFileWithContent(rootDir + REPORT_NAME, perfReport);
		return perfTable;
	}

	public static void generate(String rootDir)
	{
		copyGalleryLib();
		List<File> testFolders = FileManager.getFilesInDir(new File(rootDir));
		for (File testFolder : testFolders)
		{
			List<File> images = FileManager.getFilesInDir(testFolder);
			createReportAsHTML(testFolder, images);
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
			if(imgNames.size() == 0) return;
			
			Collections.sort(imgNames);

			StringBuilder report = new StringBuilder();
			for (int i = 0; i < imgNames.size(); i++)
			{
				// convert toString 
				String image = R.REPORT.get("image").toString();
				image = image.replace("${image}", imgNames.get(i).toString());
				image = image.replace("${thumbnail}", imgNames.get(i).toString());
				image = image.replace("${alt}", imgNames.get(i).toString());
				String title = TestLogCollector.getScreenshotComment(imgNames.get(i));
				image = image.replace("${title}", title.toString());
				report.append(image);
			}
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
		File reportsRootDir = new File(System.getProperty("user.dir") + "/" + Configuration.get(Parameter.ROOT_REPORT_DIRECTORY));
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
