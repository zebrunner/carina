package com.qaprosoft.carina.core.foundation.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.exception.InvalidArgsException;
import com.qaprosoft.carina.core.foundation.utils.resources.I18N;
import com.qaprosoft.carina.core.foundation.utils.resources.L10N;

public class ParameterGenerator {

	private static final Logger LOGGER = Logger.getLogger(ParameterGenerator.class);

	private static Pattern GENERATE_UUID_PATTERN = Pattern.compile(SpecialKeywords.GENERATE_UUID);
	private static Pattern GENERATE_PATTERN = Pattern.compile(SpecialKeywords.GENERATE);
	private static Pattern GENERATEAN_PATTERN = Pattern.compile(SpecialKeywords.GENERATEAN);
	private static Pattern GENERATEN_PATTERN = Pattern.compile(SpecialKeywords.GENERATEN);
	private static Pattern TESTDATA_PATTERN = Pattern.compile(SpecialKeywords.TESTDATA);
	private static Pattern ENV_PATTERN = Pattern.compile(SpecialKeywords.ENV);
	private static Pattern L10N_PATTERN = Pattern.compile(SpecialKeywords.L10N_PATTERN);
	private static Pattern I18N_PATTERN = Pattern.compile(SpecialKeywords.I18N_PATTERN);
	private static Pattern EXCEL_PATTERN = Pattern.compile(SpecialKeywords.EXCEL);

	private static Matcher matcher;
	
	private static String UUID;

	public static Object process(String param)
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
			
			matcher = GENERATEN_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(":") + 1;
				int end = param.indexOf("}");
				int size = Integer.valueOf(param.substring(start, end));
				return StringUtils.replace(param, matcher.group(), StringGenerator.generateNumeric(size));
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
			
			matcher = I18N_PATTERN.matcher(param);
			if (matcher.find())
			{
				int start = param.indexOf(SpecialKeywords.I18N + ":") + 5;
				int end = param.indexOf("}");
				String key = param.substring(start, end);
				return StringUtils.replace(param, matcher.group(), I18N.getText(key));
			}
			
			matcher = L10N_PATTERN.matcher(param);
			String initStrL10N = param;
			while (matcher.find())
			{
				int start = param.indexOf(SpecialKeywords.L10N + ":") + 5;
				int end = param.indexOf("}");
				String key = param.substring(start, end);
				if(!L10N.isUTF) {
					param = StringUtils.replace(param, matcher.group(), L10N.getText(key));
				} else {
					param = StringUtils.replace(param, matcher.group(), L10N.getUTFText(key));
				}

			}
			// in case if L10N pattern was applied
			if(!initStrL10N.equalsIgnoreCase(param)) {
				return param;
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
	
	public static String getUUID() {
		return UUID;
	}

	public static void setUUID(String uUID) {
		UUID = uUID;
	}
}
