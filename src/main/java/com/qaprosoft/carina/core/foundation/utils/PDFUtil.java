package com.qaprosoft.carina.core.foundation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFUtil
{

	public static String readTxtFromPDF(String resourcePath, int startPage, int endPage)
	{
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		InputStream is = PDFUtil.class.getClassLoader().getResourceAsStream(resourcePath);
		if (is == null)
		{
			throw new RuntimeException("Input stream not opened");
		}
		try
		{
			PDFParser parser = new PDFParser(is);
			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);
			pdfStripper.setSortByPosition(true);
			pdfStripper.setStartPage(startPage);
			pdfStripper.setEndPage(endPage);
			return pdfStripper.getText(pdDoc);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		} finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public static String getFirstMatch(String pdfText, String regex)
	{
		Matcher m = Pattern.compile(regex).matcher(pdfText);
		if (m.find())
		{
			return m.group();
		} else
		{
			return null;
		}
	}

	// public static void main(String[] args)
	// {
	// String txt = readTxtFromPDF("pdf/2014-10.pdf", 1, 1);
	// System.out.println("Total start = "
	// + getFirstMatch(txt, "(?<=Total \\$)(([0-9][0-9]{0,2}(,[0-9]{3})*)(\\.[0-9]{2}))"));
	// }
}
