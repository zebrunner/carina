package com.qaprosoft.carina.core.foundation.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.utils.parser.XLSTable;

public class CellLinksTest
{
	private List<String> headers;
	private List<String> firstRow;
	private List<String> secondRow;
	private List<String> thirdRow;
	
	@BeforeTest
	public void setUp()
	{
		headers = new ArrayList<String>(); headers.add("Header1");
		headers.add("Header2");	headers.add("Header3");
		headers.add("Header4");	headers.add("Header5");
		headers.add("Header6");	headers.add("Header7");
		
		firstRow = new ArrayList<String>();	firstRow.add("Data1"); 
		firstRow.add("User1"); firstRow.add("Data6"); 
		firstRow.add("Data4"); firstRow.add(null);
		firstRow.add(null); firstRow.add(null);
		
		secondRow = new ArrayList<String>(); secondRow.add("Name1");	
		secondRow.add("Data5");	secondRow.add("Name3");	
		secondRow.add("Name4");	secondRow.add("Data5");	
		secondRow.add("Data6"); secondRow.add("Data7");
		
		thirdRow = new ArrayList<String>();	thirdRow.add("User1");
		thirdRow.add("User2"); thirdRow.add("User3");
		thirdRow.add("User4"); thirdRow.add("");
		thirdRow.add(""); thirdRow.add("");
	}
	
	
	@Test
	public void testCellLinksForCurrentWB()
	{
		XLSTable table = XLSParser.parseSpreadSheet("Test.xlsx", "Sheet1");		
		verifyHeaders(table.getHeaders());
		verifyDataRow(firstRow, table.getHeaders(), table.getDataRows().get(0));
		verifyDataRow(secondRow, table.getHeaders(), table.getDataRows().get(1));
		verifyDataRow(thirdRow, table.getHeaders(), table.getDataRows().get(2));
	}
	
	private void verifyHeaders(List<String> headers)
	{
		for(int i = 0; i < headers.size(); i++)
		{
			Assert.assertEquals(headers.get(i), headers.get(i));
		}
	}
	
	private void verifyDataRow(List<String> expected, List<String> headers, Map<String, String> dataRow)
	{
		for(int i = 0; i < headers.size(); i++)
		{
			Assert.assertEquals(expected.get(i), dataRow.get(headers.get(i)));
		}
	}
}
