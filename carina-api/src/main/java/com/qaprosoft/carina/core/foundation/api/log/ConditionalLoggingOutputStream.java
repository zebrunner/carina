package com.qaprosoft.carina.core.foundation.api.log;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.event.Level;

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

    public void conditionLogging(Response response) {
        if (logCondition.test(response)) {
            super.flush();
        }
    }

    public void setLogCondition(Predicate<Response> logCondition) {
        this.logCondition = logCondition;
    }
}
