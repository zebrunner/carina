package com.qaprosoft.apitools.util;

import com.qaprosoft.carina.core.foundation.utils.R;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class PropertiesUtilTest {

    @Test
    public void testReadProperties() {
        Properties expectedProperties = R.TESTDATA.getProperties();
        Properties actualProperties = PropertiesUtil.readProperties("testdata.properties");

        Assert.assertEquals(actualProperties, expectedProperties, "Properties wasn't read properly");
    }

    @Test
    public void testReadPropertiesWithWrongProperties() {
        try {
            PropertiesUtil.readProperties("nonexistent.properties");
        } catch (RuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Can't read properties from file", "Exception not thrown in readProperties()");
        }
    }
}
