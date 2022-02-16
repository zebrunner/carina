package com.qaprosoft.carina.core.foundation.api.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ConditionLoggingOutputStream extends LoggingOutputStream {

    // TODO: 16.02.22 docs
    private String str;

    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param log   the Logger to write to
     * @param level the log level
     * @throws IllegalArgumentException in case if one of arguments is null.
     */
    public ConditionLoggingOutputStream(Logger log, Level level) throws IllegalArgumentException {
        super(log, level);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out.
     */
    public void flush() {
        if (getCount() == 0) {
            return;
        }
        final byte[] bytes = new byte[getCount()];
        System.arraycopy(getBuf(), 0, bytes, 0, getCount());
        str = new String(bytes);
        setCount(0);
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream.
     */
    public void close() {
        flush();
        setHasBeenClosed(true);
    }

    public void logging() {
        getLog().log(getLevel(), str);
    }

}
