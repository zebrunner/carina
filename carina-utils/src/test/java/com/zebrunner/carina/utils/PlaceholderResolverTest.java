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

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link PlaceholderResolver}
 */
public class PlaceholderResolverTest {
    private static final Properties loopedProperties = new Properties() {
        private static final long serialVersionUID = 1L;
        {
            put("key1", "${key2}");
            put("key2", "${key3}");
            put("key3", "${key1}");
        }
    };

    private static final Properties noPlaceholderDefProperties = new Properties() {
        private static final long serialVersionUID = 1L;
        {
            put("key1", "${key2}");
            put("key2", "${key3}");
        }
    };

    private static final Properties validProperties = new Properties() {
        private static final long serialVersionUID = 1L;
        {
            put("greeting", "We wish you a ${holiday1} and happy ${holiday2}!");
            put("holiday1", "Merry Cristmas");
            put("holiday2", "New Year ${year}");
            put("year", "2014");
        }
    };

    @Test
    public void testValidtion() {
        Assert.assertFalse(PlaceholderResolver.isValid(loopedProperties));
        // Assert.assertFalse(PlaceholderResolver.isValid(noPlaceholderDefProperties));
        Assert.assertTrue(PlaceholderResolver.isValid(validProperties));
    }

    @Test
    public void testResolve() {
        Assert.assertEquals(PlaceholderResolver.resolve(validProperties, "holiday2"), "New Year 2014");
        Assert.assertEquals(PlaceholderResolver.resolve(validProperties, "greeting"), "We wish you a Merry Cristmas and happy New Year 2014!");
        Assert.assertEquals(PlaceholderResolver.resolve(noPlaceholderDefProperties, "key1"), "${key3}");
    }
}
