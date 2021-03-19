package com.qaprosoft.carina.core.utils;

import com.qaprosoft.carina.core.foundation.utils.messager.ZebrunnerMessager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ZebrunnerMessagerTest {

    private static final String MESSAGE = "Custom param";

    @Test
    public void testMessageInfoWithSingleParam() {
        ZebrunnerMessager messager = ZebrunnerMessager.RAW_MESSAGE;
        String printedMassage = messager.info(MESSAGE);

        Assert.assertEquals(printedMassage, MESSAGE, printedMassage + " doesn't equal to " + MESSAGE);
    }

    @Test
    public void testMessageErrorWithSingleParam() {
        ZebrunnerMessager messager = ZebrunnerMessager.RAW_MESSAGE;
        String printedMessage = messager.error(MESSAGE);

        Assert.assertEquals(printedMessage, MESSAGE, printedMessage + " doesn't equal to " + MESSAGE);
    }

    @Test
    public void testMessageWarnWithSingleParam() {
        ZebrunnerMessager messager = ZebrunnerMessager.RAW_MESSAGE;
        String printedMassage = messager.warn(MESSAGE);

        Assert.assertEquals(printedMassage, MESSAGE, printedMassage + " doesn't equal to " + MESSAGE);
    }
}
