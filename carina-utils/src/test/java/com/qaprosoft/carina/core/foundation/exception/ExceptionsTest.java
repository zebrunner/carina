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
package com.qaprosoft.carina.core.foundation.exception;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.R;

public class ExceptionsTest {

    @Test
    public void testDataLoadingExceptionWithText() {
        try {
            throw new DataLoadingException("test");
        } catch (DataLoadingException e) {
            Assert.assertTrue(e.getMessage().equals("Can't load data: test"));
        }
    }

    @Test
    public void testDataLoadingExceptionWithoutText() {
        try {
            throw new DataLoadingException();
        } catch (DataLoadingException e) {
            Assert.assertTrue(e.getMessage().equals("Can't load data."));
        }
    }

    @Test
    public void testInvalidArgsExceptionWithText() {
        try {
            throw new InvalidArgsException("test");
        } catch (InvalidArgsException e) {
            Assert.assertTrue(e.getMessage().equals("Invalid test arguments exception: test"));
        }
    }

    @Test
    public void testInvalidArgsExceptionWithoutText() {
        try {
            throw new InvalidArgsException();
        } catch (InvalidArgsException e) {
            Assert.assertTrue(e.getMessage().equals("Invalid test arguments exception"));
        }
    }

    @Test
    public void testInvalidConfigurationExceptionWithText() {
        try {
            throw new InvalidConfigurationException("test");
        } catch (InvalidConfigurationException e) {
            Assert.assertTrue(e.getMessage().equals("test"));
        }
    }

    @Test
    public void testInvalidConfigurationExceptionWithoutText() {
        try {
            Exception e = new Exception("test");
            throw new InvalidConfigurationException(e);
        } catch (InvalidConfigurationException e) {
            Assert.assertTrue(e.getMessage().equals("java.lang.Exception: test"));
        }
    }

    @Test
    public void testNotSupportedOperationExceptionWithText() {
        try {
            throw new NotSupportedOperationException("test");
        } catch (NotSupportedOperationException e) {
            Assert.assertTrue(e.getMessage().equals("Not supported operation: test!"));
        }
    }

    @Test
    public void testNotSupportedOperationExceptionWithoutText() {
        try {
            throw new NotSupportedOperationException();
        } catch (NotSupportedOperationException e) {
            Assert.assertTrue(e.getMessage().equals("Not supported operation!"));
        }
    }

    @Test
    public void testPlaceholderResolverExceptionWithText() {
        try {
            throw new PlaceholderResolverException("test");
        } catch (PlaceholderResolverException e) {
            Assert.assertTrue(e.getMessage().equals("Value not found by key 'test'"));
        }
    }

    @Test
    public void testPlaceholderResolverExceptionWithoutText() {
        try {
            throw new PlaceholderResolverException();
        } catch (PlaceholderResolverException e) {
            Assert.assertNull(e.getMessage());
        }
    }

    public void testRequiredCtorNotFoundExceptionWithText() {
        try {
            throw new RequiredCtorNotFoundException("test");
        } catch (RequiredCtorNotFoundException e) {
            Assert.assertTrue(e.getMessage().equals("Required constructor isn't found: test"));
        }
    }

    @Test
    public void testRequiredCtorNotFoundExceptionWithoutText() {
        try {
            throw new RequiredCtorNotFoundException();
        } catch (RequiredCtorNotFoundException e) {
            Assert.assertTrue(e.getMessage().equals("Required constructor isn't found."));
        }
    }

    public void testTestCreationExceptionWithText() {
        try {
            throw new TestCreationException("test");
        } catch (TestCreationException e) {
            Assert.assertTrue(e.getMessage().equals("Test creation exception: test"));
        }
    }

    @Test
    public void testTestCreationExceptionWithoutText() {
        try {
            throw new TestCreationException();
        } catch (TestCreationException e) {
            Assert.assertTrue(e.getMessage().equals("Test creation exception"));
        }
    }
    
    @Test
    public void testNotImplementedException() {
        try {
            R.CONFIG.put(SpecialKeywords.PLATFORM, "iOS", true);
            throw new NotImplementedException();
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Method [testNotImplementedException] isn't implemented for iOS!");
        }
    }

}
