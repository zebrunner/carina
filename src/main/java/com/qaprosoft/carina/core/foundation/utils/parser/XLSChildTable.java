package com.qaprosoft.carina.core.foundation.utils.parser;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;

public class XLSChildTable extends XLSTable
{
	public void addDataRow(Row row)
	{		
		Map<String, String> dataMap = new HashMap<String, String>();
		for (int i = 0; i < super.getHeaders().size(); i++)
		{
			synchronized (dataMap){ 
				dataMap.put(super.getHeaders().get(i), XLSParser.getCellValue(row.getCell(i)));
			}
		}
		super.getDataRows().add(dataMap);
	}
}
