package com.qaprosoft.carina.core.foundation.api.log;

import io.restassured.response.Response;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.function.Predicate;

public class ConditionalLoggingOutputStream extends LoggingOutputStream {

    private Predicate<Response> logCondition;

    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param log   the Logger to write to
     * @param level the log level
     * @throws IllegalArgumentException in case if one of arguments is null.
     */
    public ConditionalLoggingOutputStream(Logger log, Level level) throws IllegalArgumentException {
        super(log, level);
    }

    @Override
    public void close() {
        // No operation
    }

    public void conditionalClose(Response response) {
        if (logCondition == null || logCondition.test(response)) {
            doClose();
        }
    }

    public void doClose() {
        super.close();
    }

    public void setLogCondition(Predicate<Response> logCondition) {
        this.logCondition = logCondition;
    }
}
