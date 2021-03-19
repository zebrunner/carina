package com.qaprosoft.carina.core.foundation.log;

import com.qaprosoft.carina.core.foundation.api.log.LoggingOutputStream;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;

public class LoggingOutputStreamTest {

    private static final Logger LOGGER = Logger.getLogger(LoggingOutputStreamTest.class);

    @Test
    public void testLoggingOutputStream() {
        LoggingOutputStream loggingOutputStream = new LoggingOutputStream(LOGGER, Level.INFO);
        try {
            String str = "Hello World!";
            for (byte charByte: str.getBytes()) {
                loggingOutputStream.write(charByte);
            }
            loggingOutputStream.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test
    public void testLoggingOutputStreamWithClosedStream() {
        LoggingOutputStream loggingOutputStream = new LoggingOutputStream(LOGGER, Level.INFO);
        try {
            String str = "Hello World!";
            loggingOutputStream.close();
            for (byte charByte: str.getBytes()) {
                loggingOutputStream.write(charByte);
            }
        } catch (IOException e) {
            Assert.assertEquals(e.getMessage(), "The stream has been closed.");
        }
    }
}
