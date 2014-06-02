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

public class XLSTable
{
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

	public void addDataRow(Row row)
	{
		if(executeColumn != null && executeValue != null && headers.contains(executeColumn))
		{
			if(!executeValue.equalsIgnoreCase(XLSParser.getCellValue(row.getCell(headers.indexOf(executeColumn)))))
			{
				return;
			}
		}
		
		Map<String, String> dataMap = new HashMap<String, String>();
		for (int i = 1; i < headers.size(); i++)
		{
			synchronized (dataMap){ 
				dataMap.put(headers.get(i), XLSParser.getCellValue(row.getCell(i)));
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
