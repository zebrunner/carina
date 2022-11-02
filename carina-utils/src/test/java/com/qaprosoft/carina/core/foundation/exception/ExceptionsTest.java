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
package com.qaprosoft.carina.core.foundation.exception;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ExceptionsTest {

    @Test
    public void testInvalidConfigurationExceptionWithText() {
        try {
            throw new InvalidConfigurationException("test");
        } catch (InvalidConfigurationException e) {
            Assert.assertEquals(e.getMessage(), "test", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testInvalidConfigurationExceptionWithoutText() {
        try {
            Exception e = new Exception("test");
            throw new InvalidConfigurationException(e);
        } catch (InvalidConfigurationException e) {
            Assert.assertEquals(e.getMessage(), "java.lang.Exception: test", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testNotSupportedOperationExceptionWithText() {
        try {
            throw new NotSupportedOperationException("test");
        } catch (NotSupportedOperationException e) {
            Assert.assertEquals(e.getMessage(), "Not supported operation: test!", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testNotSupportedOperationExceptionWithoutText() {
        try {
            throw new NotSupportedOperationException();
        } catch (NotSupportedOperationException e) {
            Assert.assertEquals(e.getMessage(), "Not supported operation!", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testPlaceholderResolverExceptionWithText() {
        try {
            throw new PlaceholderResolverException("test");
        } catch (PlaceholderResolverException e) {
            Assert.assertEquals(e.getMessage(), "Value not found by key 'test'", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testPlaceholderResolverExceptionWithoutText() {
        try {
            throw new PlaceholderResolverException();
        } catch (PlaceholderResolverException e) {
            Assert.assertNull(e.getMessage(), "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testRequiredCtorNotFoundExceptionWithText() {
        try {
            throw new RequiredCtorNotFoundException("test");
        } catch (RequiredCtorNotFoundException e) {
            Assert.assertEquals(e.getMessage(), "Required constructor isn't found: test", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testRequiredCtorNotFoundExceptionWithoutText() {
        try {
            throw new RequiredCtorNotFoundException();
        } catch (RequiredCtorNotFoundException e) {
            Assert.assertEquals(e.getMessage(), "Required constructor isn't found.", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testTestCreationExceptionWithText() {
        try {
            throw new TestCreationException("test");
        } catch (TestCreationException e) {
            Assert.assertEquals(e.getMessage(), "Test creation exception: test", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

    @Test
    public void testTestCreationExceptionWithoutText() {
        try {
            throw new TestCreationException();
        } catch (TestCreationException e) {
            Assert.assertEquals(e.getMessage(), "Test creation exception", "Message wasn't overridden in " + e.getClass().getName());
        }
    }

/*
    @Test
    public void testNotImplementedException() {
        try {
            R.CONFIG.put(SpecialKeywords.PLATFORM, "iOS", true);
            throw new NotImplementedException();
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Method [testNotImplementedException] isn't implemented for iOS!");
        } finally {
            R.CONFIG.put(SpecialKeywords.PLATFORM, "", true);
        }
    }
*/

}
