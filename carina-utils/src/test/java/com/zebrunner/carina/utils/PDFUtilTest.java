/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.utils;

import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link PDFUtil}
 */
public class PDFUtilTest {
    @Test
    public void testReadTxtFromPDF() {
        InputStream is = PDFUtilTest.class.getClassLoader().getResourceAsStream("test.pdf");
        String text = PDFUtil.readTxtFromPDF(is, 1, 1);
        Assert.assertNotNull(text);
        Assert.assertTrue(text.contains("This is Carina PDF test!"));
    }
}
