package com.qaprosoft.carina.core.foundation.api.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ControlLoggingOutputStream extends LoggingOutputStream {

    private String strBuf;

    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param log   the Logger to write to
     * @param level the log level
     * @throws IllegalArgumentException in case if one of arguments is null.
     */
    public ControlLoggingOutputStream(Logger log, Level level) throws IllegalArgumentException {
        super(log, level);
    }

    /**
     * Flushes this output stream and forces any buffered bytes as string in local str buf
     */
    @Override
    public void flush() {
        if (getCount() == 0) {
            return;
        }
        final byte[] bytes = new byte[getCount()];
        System.arraycopy(getBuf(), 0, bytes, 0, getCount());
        strBuf = new String(bytes);
        setCount(0);
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream to local str buf
     */
    @Override
    public void close() {
        flush();
        setHasBeenClosed(true);
    }

    /**
     * Realised all buffered data
     */
    public void log() {
        getLog().log(getLevel(), strBuf);
    }

}
