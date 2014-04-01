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
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.util.Log;

import com.qaprosoft.carina.core.foundation.exception.InvalidArgsException;

public class XLSParser
{
	private static DataFormatter df;
	private static FormulaEvaluator evaluator;
	
	static 
	{
		df = new DataFormatter();
	}
	
	public static String parseValue(String locatorKey, String xlsPath, Locale locale)
	{
		String value = null;

		Workbook wb = XLSCache.getWorkbook(xlsPath);
		Sheet sheet = wb.getSheetAt(0);

		List<String> locales = getLocales(sheet);
		if (!locales.contains(locale.getCountry()))
		{
			throw new RuntimeException("Can't find locale '" + locale.getCountry() + "' in xls '" + xlsPath + "'!");
		}
		int cellN = locales.indexOf(locale.getCountry()) + 1;

		List<String> locatorKeys = getLoctorKeys(sheet);
		if (!locatorKeys.contains(locatorKey))
		{
			throw new RuntimeException("Can't find locatorKey '" + locatorKey + "' in xls '" + xlsPath + "'!");
		}
		int rowN = locatorKeys.indexOf(locatorKey) + 1;

		try
		{
			value = getCellValue(sheet.getRow(rowN).getCell(cellN));
		} catch (Exception e)
		{
			throw new RuntimeException("Can't find value for locatorKey '" + locatorKey + "' with locale '" + locale.getCountry()
					+ "' in xls '" + xlsPath + "'!");
		}

		return value;
	}
	

	private static List<String> getLocales(Sheet sheet)
	{
		List<String> locales = new ArrayList<String>();
		int lastCell = sheet.getRow(0).getLastCellNum();
		for (int i = 1; i < lastCell; i++)
		{
			locales.add(getCellValue(sheet.getRow(0).getCell(i)));
		}
		return locales;
	}

	private static List<String> getLoctorKeys(Sheet sheet)
	{
		List<String> locatorKeys = new ArrayList<String>();
		int lastRow = sheet.getLastRowNum();
		for (int i = 1; i <= lastRow; i++)
		{
			locatorKeys.add(getCellValue(sheet.getRow(i).getCell(0)));
		}
		return locatorKeys;
	}
	
	public static String parseValue(String xls, String sheetName, String key)
	{
		String value = null;
		
		Workbook wb = XLSCache.getWorkbook(xls);
		
		Sheet sheet = wb.getSheet(sheetName);
		if(sheet == null)
		{
			throw new InvalidArgsException(String.format("No sheet: '%s' in excel file: '%s'!", sheetName, xls));
		}
		
		boolean isKeyFound = false;
		for(int i = 1; i <= sheet.getLastRowNum(); i++)
		{
			if(key.equals(getCellValue(sheet.getRow(i).getCell(0))))
			{
				value = getCellValue(sheet.getRow(i).getCell(1));
				isKeyFound = true;
				break;
			}
		}
		
		if(!isKeyFound)
		{
			throw new InvalidArgsException(String.format("No key: '%s' on sheet '%s' in excel file: '%s'!", key, sheetName, xls));
		}
		
		return value;
	}
	
	public static XLSTable parseSpreadSheet(String xls, String sheetName)
	{
		return parseSpreadSheet(xls, sheetName, null, null);
	}
	
	public static XLSTable parseSpreadSheet(String xls, String sheetName, String executeColumn, String executeValue)
	{
		XLSTable dataTable;
		if(executeColumn != null && executeValue != null)
		{
			dataTable = new XLSTable(executeColumn, executeValue);
		}
		else
		{
			dataTable = new XLSTable();
		}
		
		Workbook wb = XLSCache.getWorkbook(xls);
		evaluator = wb.getCreationHelper().createFormulaEvaluator();		
		
		Sheet sheet = wb.getSheet(sheetName);
		if(sheet == null)
		{
			throw new InvalidArgsException(String.format("No sheet: '%s' in excel file: '%s'!", sheetName, xls));
		}
		
		try{
			for(int i = 0; i <= sheet.getLastRowNum(); i++)
			{
				if(i == 0)
				{
					dataTable.setHeaders(sheet.getRow(i));
				}
				else
				{
					dataTable.addDataRow(sheet.getRow(i));
				}
			}
		}
		catch (Exception e) {
			Log.error(e.getMessage());
		    e.printStackTrace();			
		}
		return dataTable;
	}
	
	public static String getCellValue(Cell cell)
	{
		if(cell == null) return "";
		switch (cell.getCellType())
		{
		case Cell.CELL_TYPE_STRING:
			return df.formatCellValue(cell).trim();
		case Cell.CELL_TYPE_NUMERIC:
			return df.formatCellValue(cell).trim();
		case Cell.CELL_TYPE_BOOLEAN:
			return df.formatCellValue(cell).trim();
		case Cell.CELL_TYPE_FORMULA:
			return df.formatCellValue(cell, evaluator).trim();
		case Cell.CELL_TYPE_BLANK:
			return "";
		default:
			return null;
		}
	}
}
