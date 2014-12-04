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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class XLSTable
{
	private final static String FK_PREFIX = "FK_LINK_";
	
	private List<String> headers;
	private List<Map<String, String>> dataRows;
	private String executeColumn;
	private String executeValue;

	public XLSTable()
	{
		headers = new LinkedList<String>();
		dataRows = Collections.synchronizedList(new LinkedList<Map<String, String>>());
	}
	
	public XLSTable(String executeColumn, String executeValue)
	{
		this();
		this.executeColumn = executeColumn;
		this.executeValue = executeValue;
	}

	public void setHeaders(Row row)
	{
		headers.clear();
		for (int i = 0; i < row.getLastCellNum(); i++)
		{
			headers.add(XLSParser.getCellValue(row.getCell(i)));
		}
	}

	public void addDataRow(Row row, Workbook wb, Sheet sheet)
	{
		if(executeColumn != null && executeValue != null && headers.contains(executeColumn))
		{
			if(!executeValue.equalsIgnoreCase(XLSParser.getCellValue(row.getCell(headers.indexOf(executeColumn)))))
			{
				return;
			}
		}
		
		XLSChildTable childRow = null;
		
		Map<String, String> dataMap = new HashMap<String, String>();
		for (int i = 0; i < headers.size(); i++)
		{
			String header = headers.get(i);
			if(header.startsWith(FK_PREFIX))
				childRow = XLSParser.parseCellLinks(row.getCell(i), wb, sheet);
				
			synchronized (dataMap){ 
				dataMap.put(header, XLSParser.getCellValue(row.getCell(i)));
			}
		}
		
		// If row has hyperlink than merge headers and data		
		if(childRow != null)
		{			
			for(int i = 0; i < childRow.getHeaders().size(); i++)
			{
				String currentHeader = childRow.getHeaders().get(i);			
						
				if(dataMap.get(currentHeader) == null)
				{
					// Merge headers				
					if(!this.headers.contains(currentHeader)) this.headers.add(currentHeader);
					
					//	Merge data				
					synchronized (dataMap)
					{ 
						dataMap.put(currentHeader, childRow.getDataRows().get(0).get(currentHeader));
					}
				}
			}			
		}
		
		dataRows.add(dataMap);
	}
	
	public List<String> getHeaders()
	{
		return headers;
	}

	public List<Map<String, String>> getDataRows()
	{
		return dataRows;
	}

	public String getExecuteColumn()
	{
		return executeColumn;
	}

	public void setExecuteColumn(String executeColumn)
	{
		this.executeColumn = executeColumn;
	}

	public String getExecuteValue()
	{
		return executeValue;
	}

	public void setExecuteValue(String executeValue)
	{
		this.executeValue = executeValue;
	}
}
