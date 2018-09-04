package com.qaprosoft.carina.core.foundation.utils.messager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * ReportMessage is used for reporting informational and error messages both
 * using Zafira logger.
 *
 * @author brutskov
 */

public enum ZafiraMessager implements IMessager {

    RAW_MESSAGE("%s");

    private static final Logger LOGGER = Logger.getLogger(ZafiraMessager.class);

    private String pattern;

    ZafiraMessager(String pattern) {
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
