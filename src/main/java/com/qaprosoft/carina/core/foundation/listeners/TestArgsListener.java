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
package com.qaprosoft.carina.core.foundation.listeners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.qaprosoft.carina.core.foundation.exception.InvalidArgsException;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.L18n;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.StringGenerator;
import com.qaprosoft.carina.core.foundation.utils.parser.XLSParser;

/*
 * Test arguments listener is responsible for processing test parameters with wildcards.
 * 
 * @author Alex Khursevich
 */
public class TestArgsListener extends TestListenerAdapter
{
	private static final Logger LOGGER = Logger.getLogger(TestArgsListener.class);

	private static Pattern GENERATE_PATTERN = Pattern.compile(SpecialKeywords.GENERATE);
	private static Pattern TESTDATA_PATTERN = Pattern.compile(SpecialKeywords.TESTDATA);
	private static Pattern ENV_PATTERN = Pattern.compile(SpecialKeywords.ENV);
	private static Pattern L18N_PATTERN = Pattern.compile(SpecialKeywords.L18N);
	private static Pattern EXCEL_PATTERN = Pattern.compile(SpecialKeywords.EXCEL);
	private static String NULL = "NULL";

	private static Matcher matcher;

	@Override
	public void onTestStart(ITestResult result)
	{
		if (result != null && result.getParameters() != null)
		{
			for (int i = 0; i < result.getParameters().length; i++)
			{
				result.getParameters()[i] = processParameter((String) result.getParameters()[i]);
			}
		}
	}

	public static Object processParameter(String param)
	{
		try
		{
			if (param == null || NULL.equalsIgnoreCase(param))
			{
				return null;
			}

			matcher = GENERATE_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(":") + 1;
				int end = param.indexOf("}");
				int size = Integer.valueOf(param.substring(start, end));
				return StringUtils.replace(param, matcher.group(), StringGenerator.generateWord(size));
			}
			
			matcher = ENV_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(":") + 1;
				int end = param.indexOf("}");
				String key = param.substring(start, end);
				return StringUtils.replace(param, matcher.group(), Configuration.getEnvArg(key));
			}

			matcher = TESTDATA_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(":") + 1;
				int end = param.indexOf("}");
				String key = param.substring(start, end);
				return StringUtils.replace(param, matcher.group(), R.TESTDATA.get(key));
			}

			matcher = EXCEL_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(":") + 1;
				int end = param.indexOf("}");
				String key = param.substring(start, end);
				return StringUtils.replace(param, matcher.group(), getValueFromXLS(key));
			}
			
			matcher = L18N_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(":") + 1;
				int end = param.indexOf("}");
				String key = param.substring(start, end);
				return StringUtils.replace(param, matcher.group(), L18n.getText(key));
			}
		}
		catch (Exception e)
		{
			LOGGER.error(e.getMessage());
		}
		return param;
	}

	private static String getValueFromXLS(String xlsSheetKey)
	{
		if (StringUtils.isEmpty(xlsSheetKey))
		{
			throw new InvalidArgsException("Invalid excel key, should be 'xls_file#sheet#key'.");
		}

		String xls = xlsSheetKey.split("#")[0];
		String sheet = xlsSheetKey.split("#")[1];
		String key = xlsSheetKey.split("#")[2];

		return XLSParser.parseValue(xls, sheet, key);
	}
}
