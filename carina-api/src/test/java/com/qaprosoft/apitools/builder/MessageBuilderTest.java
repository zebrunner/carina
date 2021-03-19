package com.qaprosoft.apitools.builder;

import com.qaprosoft.carina.core.foundation.utils.R;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class MessageBuilderTest {


    @Test
    public void testBuildStringMessage() {
        String expectedStringMessage = getStringProperties(R.TESTDATA.getProperties());
        String actualStringMessage = MessageBuilder.buildStringMessage("testdata.properties");

        Assert.assertEquals(actualStringMessage, expectedStringMessage, "String message wasn't generated properly");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testBuildStringMessageWithWrongProperties() {
        MessageBuilder.buildStringMessage("nonexistent.properties");
    }

    private String getStringProperties(Properties properties) {
        StringBuilder sb = new StringBuilder();

        properties.forEach((key, value) -> sb.append(key).append("=").append(value).append(System.lineSeparator()));

        return sb.toString();
    }
}
