/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * PDFUtil - utility for PDF file parsing.
 * 
 * @author Sergey Zagriychuk
 *         <a href="mailto:szagriychuk@gmail.com">Sergey Zagriychuk</a>
 *
 */
public class PDFUtil {
    /**
     * Reads PDF content in specified page range.
     * 
     * @param inputStream InputStream
     * @param startPage Start Page
     * @param endPage End Page
     * @return PDF content
     */
    public static String readTxtFromPDF(InputStream inputStream, int startPage, int endPage) {
        PDFTextStripper pdfStripper = null;
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        RandomAccessBufferedFileInputStream randomAccessBufferedFileInputStream = null;
        if (inputStream == null) {
            throw new RuntimeException("Input stream not opened");
        }
        try {
        	randomAccessBufferedFileInputStream = new RandomAccessBufferedFileInputStream(inputStream);
            PDFParser parser = new PDFParser(randomAccessBufferedFileInputStream);
            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper();
            pdDoc = new PDDocument(cosDoc);
            pdfStripper.setSortByPosition(true);
            pdfStripper.setStartPage(startPage);
            pdfStripper.setEndPage(endPage);
            return pdfStripper.getText(pdDoc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (cosDoc != null) {
                    cosDoc.close();
                }
                if (pdDoc != null) {
                    pdDoc.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (randomAccessBufferedFileInputStream != null) {
                	randomAccessBufferedFileInputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
