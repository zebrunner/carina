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
package com.qaprosoft.carina.core.foundation.utils.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.testng.ITestContext;

import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;


public class XLSDSBean
{
	Map<String, String> testParams;
	private List<String> args;
	private List<String> uidArgs;
	private String xlsFile;
	private String xlsSheet;


	public XLSDSBean(ITestContext context)
	{
		this(context.getCurrentXmlTest().getAllParameters());
	}
	
	public XLSDSBean(Map<String, String> testParams)
	{
		this.testParams = testParams;
		this.xlsFile = testParams.get(SpecialKeywords.EXCEL_DS_FILE);
		this.xlsSheet = testParams.get(SpecialKeywords.EXCEL_DS_SHEET);
		this.args = new ArrayList<String>();
		this.uidArgs = new ArrayList<String>();

		if(testParams.get(SpecialKeywords.EXCEL_DS_ARGS) != null)
		{
			args = Arrays.asList(testParams.get(SpecialKeywords.EXCEL_DS_ARGS).replace(",", ";").replace(" ", "").split(";"));
		}
		if(testParams.get(SpecialKeywords.EXCEL_DS_UID) != null)
		{
			uidArgs = Arrays.asList(testParams.get(SpecialKeywords.EXCEL_DS_UID).replace(",", ";").replace(" ", "").split(";"));
		}
	}

	public XLSDSBean(String xlsFile, String xlsSheet, String dsArgs, String dsUids)
	{
		this.xlsFile = xlsFile;
		this.xlsSheet = xlsSheet;
		this.args = new ArrayList<String>();
		this.uidArgs = new ArrayList<String>();

		if(!dsArgs.isEmpty())
		{
			args = Arrays.asList(dsArgs.replace(",", ";").replace(" ", "").split(";"));
		}
		if(!dsUids.isEmpty())
		{
			uidArgs = Arrays.asList(dsUids.replace(",", ";").replace(" ", "").split(";"));
		}
	}
	
	public String getXlsFile()
	{
		return xlsFile;
	}

	public void setXlsFile(String xlsFile)
	{
		this.xlsFile = xlsFile;
	}

	public String getXlsSheet()
	{
		return xlsSheet;
	}

	public void setXlsSheet(String xlsSheet)
	{
		this.xlsSheet = xlsSheet;
	}

	public List<String> getUidArgs()
	{
		return uidArgs;
	}
	public List<String> getArgs()
	{
		return args;
	}

	public void setArgs(List<String> args)
	{
		this.args = args;
	}
	
	public Map<String, String> getTestParams()
	{
		return testParams;
	}

	public void setTestParams(Map<String, String> testParams)
	{
		this.testParams = testParams;
	}

	public String argsToString(List<String> args, Map<String, String> params)
	{
		if(args.size() == 0)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(String arg : args)
		{
			if(SpecialKeywords.TUID.equals(arg)) continue;
			sb.append(String.format("%s=%s; ", arg, params.get(arg)));
		}
		return StringUtils.removeEnd(sb.toString(), "; ");
	}
	
	public String setDataSorceUUID(String testName, Map<String, String> params)
	{
		
		if(!uidArgs.isEmpty())
		{
			if(uidArgs.contains(SpecialKeywords.TUID))
			{
				testName = params.get(SpecialKeywords.TUID) + " - " + testName + " [" + argsToString(uidArgs, params) + "]";
			}
			else
			{
				if (!argsToString(uidArgs, params).isEmpty())
					testName = testName + " [" + argsToString(uidArgs, params) + "]";
			}
		}		
		
		return testName;
	}	
}
