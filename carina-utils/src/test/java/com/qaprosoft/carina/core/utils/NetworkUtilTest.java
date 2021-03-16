package com.qaprosoft.carina.core.utils;

import com.qaprosoft.carina.core.foundation.utils.NetworkUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtilTest {

    private static final String IP_ADDRESS_REGEX = "\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b";

    @Test
    public void testValidIpAddress() {
        String currentIpAddress = NetworkUtil.getIpAddress();

        Matcher matcher = Pattern.compile(IP_ADDRESS_REGEX).matcher(currentIpAddress);

        Assert.assertTrue(matcher.matches(), currentIpAddress + " is not valid");
    }

}
