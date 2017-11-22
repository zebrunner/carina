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
package com.qaprosoft.carina.core.foundation.utils.resources;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

/*
 * L10Nparser can be used for checking multiple localization values and for creating new localization property file 
 * based on actual localization of public elements.
 * 
 * Usage:
 * First of all you have to set following properties in _config.properties file
 * add_new_localization=false  - should be set to 'true' if you want to create new localization files for required Locale. 
 * 								Otherwise there will be just localization checking 
 * add_new_localization_path=null - path where created localization properties should be saved. 
 * 								If null - they will be added to artifactory folder in report
 * add_new_localization_property_name=new_custom_messages_ - the basic template for property name. 
 * 								There will be locale added at the end of the filename.
 * 
 * 
 * Then in your test you should set locale: 
 * 		L10Nparser.setActualLocale(countryCode); 
 * countryCode can be String or Locale in format 'en' or 'en_US'. You can get it from excel file as DataProvider 
 * if you have multiple locale text checking.
 * 
 * For localization checking and parsing new localization text you can use one of following checkLocalizationText() functions. See JavaDoc below.
 * For example: 
 * 		Assert.assertTrue(L10Nparser.checkLocalizationText(homePage.getEnterNowBtnText(), "enterbutton"),
 *  "Localization wasn't correct: " + L10Nparser.getAssertErrorMsg());
 *
 *
 *  And for Multiple case good solution will be following: in page class make elements as Public, then set ExtendedWebElement[] localizationCheckList
 *  For example: 
 *  public ExtendedWebElement signInTopText;
 *  public ExtendedWebElement[] localizationCheckList = { signInTopText, loginBtn, forgotPasswordLink, createAccountBtn };
 *  
 *  And in test use following function: checkMultipleLocalization() 
 *  
 *  In this case localization tag in property file will have same name as element name.
 *  
 *  For example: 
 *  Assert.assertTrue(L10Nparser.checkMultipleLocalization(loginPage.localizationCheckList), "Localization issue: "
 * + L10Nparser.getAssertErrorMsg());
 * 
 *  And the MOST IMPORTANT thing - at the end of your test use following: 
 * 		L10Nparser.saveLocalization();
 *  to save your localization property file. 
 *  
 *  FYI: if you create new localization (add_new_localization=true) localization checking will always Pass. 
 *  Do not forget to switch property to false after adding new localization in your localization files.
 *  				
 */

public class L10Nparser {
	protected static final Logger LOGGER = Logger.getLogger(L10Nparser.class);

	public static Locale actualLocale;

	public static String assertErrorMsg = "";

	public static boolean newLocalization = false;

	public static LinkedList<String> newLocList = new LinkedList<String>();

	public static Properties prop = new Properties();

	public static String propFileName = "";

	private static String encoding = "ISO-8859-1";

	protected static final int BASIC_WAIT_SHORT_TIMEOUT = 5;

	public static void init() {
		// Just init
		// setActualLocale(L10N.getDefaultLocale());
	}

	/**
	 * get Actual Locale
	 * 
	 * @return Locale
	 */
	public static Locale getActualLocale() {
		return actualLocale;
	}

	/**
	 * get AssertErrorMsg
	 * 
	 * @return String
	 */
	public static String getAssertErrorMsg() {
		return assertErrorMsg;
	}

	/**
	 * should we add New Localization - true or false
	 * 
	 * @return boolean
	 */
	public static boolean getNewLocalization() {
		return newLocalization;
	}

	/**
	 * set Actual Locale
	 * 
	 * @param countryCode
	 *            String
	 */
	public static void setActualLocale(String countryCode) {
		List<Locale> locales = LocaleReader.init(Configuration.get(Parameter.LOCALE));
		Locale locale = locales.get(0);
		try {
			String[] localeSetttings = countryCode.split("_");
			String lang, country = "";
			lang = localeSetttings[0];
			if (localeSetttings.length > 1) {
				country = localeSetttings[1];
			}
			locale = new Locale(lang, country);
		} catch (Exception e) {
			LOGGER.error(e);
		}
		setActualLocale(locale);
	}

	/**
	 * set Actual Locale
	 * 
	 * @param locale
	 *            - Locale
	 */
	public static void setActualLocale(Locale locale) {
		setActualLocale(locale, false);
	}

	/**
	 * set Actual Locale
	 * 
	 * @param locale
	 *            - Locale
	 * @param updateConfig
	 *            - can be useful for updating config file just with actual
	 *            locale and be used separately on PageObject methods not only
	 *            in test classes.
	 */
	public static void setActualLocale(Locale locale, boolean updateConfig) {
		LOGGER.info("Set actual Locale to " + locale);
		actualLocale = locale;

		if (updateConfig) {
			R.CONFIG.put(Parameter.LOCALE.getKey(), actualLocale.toString());

			String savedLocale = R.CONFIG.get(Parameter.LOCALE.getKey());
			LOGGER.debug("Saved actualLocale in config =" + savedLocale);
			List<Locale> locales = LocaleReader.init(Configuration.get(Parameter.LOCALE));
			LOGGER.debug("First locale from LocaleReader=" + locales.get(0).toString());

		}
		// Parser prop init
		if (getAddNewLocalization()) {
			try {
				propFileName = getPropertyFileName(actualLocale.toString());
				LOGGER.info("propFileName:=" + propFileName);
				FileInputStream in = new FileInputStream(propFileName);
				prop.load(in);
				in.close();

			} catch (Exception e) {
				LOGGER.debug(e);
			}
		}

	}

	/**
	 * check should we add New Localization or not
	 * 
	 * @return boolean
	 */
	private static boolean getAddNewLocalization() {
		boolean ret = false;
		if (!newLocalization) {
			try {
				String add_new = Configuration.get(Parameter.ADD_NEW_LOCALIZATION);
				if (add_new.toLowerCase().contains("true")) {
					LOGGER.info("New localization will be added.");
					newLocalization = true;
					return true;
				}
			} catch (Exception e) {
				LOGGER.debug(e);
			}
		} else {
			ret = true;
		}

		return ret;
	}

	/**
	 * ge tProperty FileName
	 * 
	 * @param localName
	 *            - String
	 * @return String with path + PropertyFileName
	 */
	private static String getPropertyFileName(String localName) {
		String ret;
		String add_new_loc_path = "null";
		String add_new_loc_name = "null";
		try {
			add_new_loc_path = Configuration.get(Parameter.ADD_NEW_LOCALIZATION_PATH);
			add_new_loc_name = Configuration.get(Parameter.ADD_NEW_LOCALIZATION_PROPERTY_NAME);
		} catch (Exception e) {
			LOGGER.debug("Using default parameters because of error: " + e);
		}
		if (add_new_loc_path.toLowerCase().contains("null")
				|| add_new_loc_path.toLowerCase().contains("{must_override}") | add_new_loc_path.isEmpty()) {
			add_new_loc_path = ReportContext.getArtifactsFolder().getAbsolutePath();
		}

		if (add_new_loc_name.toLowerCase().contains("null")
				|| add_new_loc_name.toLowerCase().contains("{must_override}") || add_new_loc_name.isEmpty()) {
			add_new_loc_name = "new_localization_";
		}

		ret = add_new_loc_path + "\\" + add_new_loc_name + localName + ".properties";

		return ret;
	}

	/**
	 * check Localization Text. Will work ONLY if locKey is equal to element
	 * Name and element is Public
	 * 
	 * @param elem
	 *            ExtendedWebElement
	 * @return boolean
	 */
	public static boolean checkLocalizationText(ExtendedWebElement elem) {
		return checkLocalizationText(elem, true);
	}

	/**
	 * check Localization Text. Will work ONLY if locKey is equal to element
	 * Name and element is Public
	 *
	 * @param elem
	 *            ExtendedWebElement
	 * @param skipMissed
	 *            - boolean - if true - will ignore missed elements.
	 * @return boolean
	 */
	public static boolean checkLocalizationText(ExtendedWebElement elem, boolean skipMissed) {
		return checkLocalizationText(elem, skipMissed, BASIC_WAIT_SHORT_TIMEOUT, false);
	}

	/**
	 * check Localization Text. Will work ONLY if locKey is equal to element
	 * Name and element is Public
	 * 
	 * @param elem
	 *            ExtendedWebElement
	 * @param skipMissed
	 *            - boolean - if true - will ignore missed elements.
	 * @param timeout - timeout for element presence waiting.
	 * @param skipPunctuationAndNumbers - if true - there will be no numbers and tricky punctuation in l10n values
	 * @return boolean
	 */
	public static boolean checkLocalizationText(ExtendedWebElement elem, boolean skipMissed, int timeout, boolean skipPunctuationAndNumbers) {
		if (elem.isElementPresent(timeout)) {
			String elemText = elem.getText();
			String locKey = elem.getName();
			return checkLocalizationText(elemText, locKey, skipPunctuationAndNumbers);
		} else {
			LOGGER.info("Expected element not present. Please check: " + elem);
			if (skipMissed) {
				LOGGER.info("Skip missed element: " + elem);
				return true;
			}
		}
		return false;
	}

	/**
	 * check Localization Text. Will work ONLY if locKey is equal to element
	 * Name and element is Public
	 *
	 * @param elem
	 *            ExtendedWebElement
	 * @param locKey
	 *            String
	 * @return boolean
	 */
	public static boolean checkLocalizationText(ExtendedWebElement elem, String locKey) {
		return checkLocalizationText(elem, locKey, BASIC_WAIT_SHORT_TIMEOUT, false);
	}

	/**
	 * check Localization Text. Will work ONLY if locKey is equal to element
	 * Name and element is Public
	 * 
	 * @param elem
	 *            ExtendedWebElement
	 * @param locKey
	 *            String
	 * @param timeout - timeout for element presence waiting.
	 * @param skipPunctuationAndNumbers - if true - there will be no numbers and tricky punctuation in l10n values
	 * @return boolean
	 */
	public static boolean checkLocalizationText(ExtendedWebElement elem, String locKey, int timeout, boolean skipPunctuationAndNumbers) {
		if (elem.isElementPresent(timeout)) {
			String elemText = elem.getText();
			return checkLocalizationText(elemText, locKey, skipPunctuationAndNumbers);
		} else {
			LOGGER.info("Expected element not present. Please check:" + elem);
		}
		return false;
	}

	/**
	 * check Localization Text
	 * 
	 * @param expectedText
	 *            String
	 * @param locKey
	 *            String
	 * @param skipPunctuationAndNumbers - if true - there will be no numbers and
	 *                                    tricky punctuation in l10n values and in checking.
	 *                                    As well as validation will be case insensitive.
	 * @return boolean
	 */
	public static boolean checkLocalizationText(String expectedText, String locKey, boolean skipPunctuationAndNumbers) {
		String l10n_default = L10N.getText(locKey, actualLocale);
		String l10n_utf = L10N.getUTFText(locKey, actualLocale);
		boolean ret,ret_utf;

		if(skipPunctuationAndNumbers) {
			ret = removeNumbersAndPunctuation(expectedText).toLowerCase().contains(removeNumbersAndPunctuation(l10n_default).toLowerCase());
			ret_utf = removeNumbersAndPunctuation(expectedText).toLowerCase().contains(removeNumbersAndPunctuation(l10n_utf).toLowerCase());
		} else {
			ret = expectedText.contains(l10n_default);
			ret_utf = expectedText.contains(l10n_utf);
		}

		if (!ret && !ret_utf) {
			if (!newLocalization) {
				LOGGER.error(
						"Actual text should be localized and be equal to: '" + l10n_default + "'. Or to: '" + l10n_utf + "'. But currently it is '"
								+ expectedText + "'.");
				assertErrorMsg =
						"Expected: '" + l10n_default + "', length=" + l10n_default.length() + ". Or '" + l10n_utf + "', length=" + l10n_utf.length()
								+ ". Actually: '" + expectedText + "', length=" + expectedText.length() + ".";
				return false;
			} else {
				if(skipPunctuationAndNumbers) {
					expectedText = removeNumbersAndPunctuation(expectedText);
				}
				String newItem = locKey + "=" + expectedText;
				LOGGER.info("Making new localization string: " + newItem);
				newLocList.add(newItem);
				prop.setProperty(locKey, expectedText);
				return true;
			}
		} else {
			if (ret) {
				LOGGER.debug("Found localization text '" + expectedText + "' in ISO-8859-1 encoding : " + l10n_default);
			}
			if (ret_utf) {
				LOGGER.info("Found localization text '" + expectedText + "' in UTF-8 encoding : " + l10n_utf);
			}
			return true;
		}
	}

	/**
	 * removeNumbersAndPunctuation from L10n string
	 * @param str String
	 * @return String
	 */
	private static String removeNumbersAndPunctuation(String str) {
		try {
			str = str.replaceAll("[0-9]", "");
			str = str.replace("!", "").replace("\u0085", "").replace("…", "");
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return str;
	}

	/**
	 * check MultipleLocalization
	 *
	 * @param localizationCheckList
	 *            - ExtendedWebElement[] should be set on required page with all
	 *            needed public elements
	 * @return boolean
	 */
	public static boolean checkMultipleLocalization(ExtendedWebElement[] localizationCheckList) {
		return checkMultipleLocalization(localizationCheckList, BASIC_WAIT_SHORT_TIMEOUT, false);
	}

	/**
	 * check MultipleLocalization
	 *
	 * @param localizationCheckList
	 *            - ExtendedWebElement[] should be set on required page with all
	 *            needed public elements
	 * @param skipPunctuationAndNumbers - if true - there will be no numbers and tricky punctuation in l10n values
	 * @return boolean
	 */
	public static boolean checkMultipleLocalization(ExtendedWebElement[] localizationCheckList,  boolean skipPunctuationAndNumbers) {
		return checkMultipleLocalization(localizationCheckList, BASIC_WAIT_SHORT_TIMEOUT, false);
	}

	/**
	 * check MultipleLocalization
	 * 
	 * @param localizationCheckList
	 *            - ExtendedWebElement[] should be set on required page with all
	 *            needed public elements
	 * @param timeout - timeout for element presence waiting.
	 * @param skipPunctuationAndNumbers - if true - there will be no numbers and tricky punctuation in l10n values
	 * @return boolean
	 */
	public static boolean checkMultipleLocalization(ExtendedWebElement[] localizationCheckList, int timeout, boolean skipPunctuationAndNumbers) {
		boolean ret = true;
		String returnAssertErrorMsg = "";
		assertErrorMsg = "";
		for (ExtendedWebElement elem : localizationCheckList) {
			if (!checkLocalizationText(elem,true, timeout, skipPunctuationAndNumbers)) {
				ret = false;
				returnAssertErrorMsg = returnAssertErrorMsg + " \n" + assertErrorMsg;
			}
		}
		assertErrorMsg = returnAssertErrorMsg;
		return ret;
	}

	/**
	 * Save Localization to property file
	 */
	public static void saveLocalization() {
		if (getAddNewLocalization()) {
			try {
				if (prop.size() == 0) {
					LOGGER.info("There are no new localization properties.");
					return;
				}
			} catch (Exception e) {
				LOGGER.error(e);
			}

			LOGGER.info("New localization for '" + actualLocale + "'");
			LOGGER.info(newLocList.toString());
			LOGGER.info("Properties: " + prop.toString());
			newLocList.clear();
			try {
				if (propFileName.isEmpty()) {
					propFileName = getPropertyFileName(actualLocale.toString());
					LOGGER.info("propFileName:=" + propFileName);
				}

				String encoding = getLocalizationSaveEncoding();
				if (encoding.contains("UTF")) {
					prop.store(new OutputStreamWriter(
							new FileOutputStream(propFileName), "UTF-8"), null);
				} else {
					OutputStream output = new FileOutputStream(propFileName);
					prop.store(output, null);
					output.close();
				}

			} catch (Exception e) {
				LOGGER.error(e);
			}
			prop.clear();
		} else {
			LOGGER.debug("There is no new localization for saving.");
		}
	}

	/**
	 * get Localization Save Encoding
	 * @return String
	 */
	private static String getLocalizationSaveEncoding() {
		try {
			encoding = Configuration.get(Parameter.ADD_NEW_LOCALIZATION_ENCODING);
		} catch (Exception e) {
			LOGGER.error("There is no localization encoding parameter in config property.");
		}
		LOGGER.info("Will use encoding: " + encoding);
		return encoding.toUpperCase();
	}

}
