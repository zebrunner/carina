package com.qaprosoft.carina.core.foundation.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.exception.InvalidArgsException;
import com.qaprosoft.carina.core.foundation.listeners.TestArgsListener;
import com.qaprosoft.carina.core.foundation.utils.parser.XLSParser;

public class ParameterGenerator {

	private static final Logger LOGGER = Logger.getLogger(TestArgsListener.class);

	private static Pattern GENERATE_UUID_PATTERN = Pattern.compile(SpecialKeywords.GENERATE_UUID);
	private static Pattern GENERATE_PATTERN = Pattern.compile(SpecialKeywords.GENERATE);
	private static Pattern GENERATEAN_PATTERN = Pattern.compile(SpecialKeywords.GENERATEAN);
	private static Pattern TESTDATA_PATTERN = Pattern.compile(SpecialKeywords.TESTDATA);
	private static Pattern ENV_PATTERN = Pattern.compile(SpecialKeywords.ENV);
	private static Pattern L18N_PATTERN = Pattern.compile(SpecialKeywords.L18N);
	private static Pattern EXCEL_PATTERN = Pattern.compile(SpecialKeywords.EXCEL);
	//private static String NULL = "NULL";

	private static Matcher matcher;

	public static Object process(String param, String UUID)
	{
		try
		{
			if (param == null || param.toLowerCase().equals("nil"))
			{
				return null;
			}

			matcher = GENERATE_UUID_PATTERN.matcher(param);
			if (matcher.find())
			{
				return StringUtils.replace(param, matcher.group(), UUID);
			}
			matcher = GENERATE_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(":") + 1;
				int end = param.indexOf("}");
				int size = Integer.valueOf(param.substring(start, end));
				return StringUtils.replace(param, matcher.group(), StringGenerator.generateWord(size));
			}
			
			matcher = GENERATEAN_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(":") + 1;
				int end = param.indexOf("}");
				int size = Integer.valueOf(param.substring(start, end));
				return StringUtils.replace(param, matcher.group(), StringGenerator.generateWordAN(size));
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
