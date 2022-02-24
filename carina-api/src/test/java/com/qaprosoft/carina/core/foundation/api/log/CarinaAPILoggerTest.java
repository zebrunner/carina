package com.qaprosoft.carina.core.foundation.api.log;


import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * 
 * @author Nikolay Zheleznui
 *
 */

public class CarinaAPILoggerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testReportingAppender(){
        LOGGER.info("logger test");
    }
}
