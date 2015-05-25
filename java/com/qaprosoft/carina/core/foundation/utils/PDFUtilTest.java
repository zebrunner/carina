package com.qaprosoft.carina.core.foundation.utils;

import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link PDFUtil}
 */
public class PDFUtilTest
{
	@Test
	public void testReadTxtFromPDF()
	{
		InputStream is = PDFUtilTest.class.getClassLoader().getResourceAsStream("test.pdf");
		String text = PDFUtil.readTxtFromPDF(is, 1, 1);
		Assert.assertNotNull(text);
		Assert.assertTrue(text.contains("This is Carina PDF test!"));
	}
}
