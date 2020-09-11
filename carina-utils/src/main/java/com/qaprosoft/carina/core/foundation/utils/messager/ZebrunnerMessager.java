package com.qaprosoft.carina.core.foundation.utils.messager;

import java.lang.invoke.MethodHandles;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * ReportMessage is used for reporting informational and error messages both
 * using Zebrunner logger.
 *
 * @author brutskov
 */

public enum ZebrunnerMessager implements IMessager {

    RAW_MESSAGE("%s");

    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private String pattern;

    ZebrunnerMessager(String pattern) {
        this.pattern = pattern;
    }

    public static <T> T custom(Level level, T object) {
        LOGGER.log(level, object);
        return object;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
