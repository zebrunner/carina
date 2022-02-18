package com.qaprosoft.carina.core.foundation.api;

import com.qaprosoft.carina.core.foundation.api.log.ConditionalLoggingOutputStream;
import io.restassured.response.Response;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class APIMethodPoller {

    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private final ActionPoller<Response> actionPoller;
    private final AbstractApiMethodV2 method;
    private LogStrategy logStrategy;
    private Consumer<Response> afterExecuteAction;

    public enum LogStrategy {
        ALL, LAST_ONLY, NONE
    }

    private APIMethodPoller(AbstractApiMethodV2 method) {
        this.method = method;
        this.actionPoller = ActionPoller.builder();
    }

    public static APIMethodPoller builder(AbstractApiMethodV2 method) {
        return new APIMethodPoller(method);
    }

    // TODO: 18.02.22 doc 
    public APIMethodPoller pollEvery(long period, TemporalUnit timeUnit) {
        this.actionPoller.pollEvery(period, timeUnit);
        return this;
    }

    // TODO: 18.02.22 doc 
    public APIMethodPoller stopAfter(long timeout, TemporalUnit timeUnit) {
        this.actionPoller.stopAfter(timeout, timeUnit);
        return this;
    }

    // TODO: 18.02.22 doc 
    public APIMethodPoller withLogStrategy(LogStrategy logStrategy) {
        this.logStrategy = logStrategy;
        return this;
    }

    // TODO: 18.02.22 doc 
    public APIMethodPoller peek(Consumer<Response> peekAction) {
        actionPoller.peek(peekAction);
        return this;
    }

    // TODO: 18.02.22 doc 
    public APIMethodPoller until(Predicate<Response> successCondition) {
        this.actionPoller.until(successCondition);
        return this;
    }

    APIMethodPoller doAfterExecute(Consumer<Response> afterExecuteAction) {
        this.afterExecuteAction = afterExecuteAction;
        return this;
    }

    // TODO: 18.02.22 doc 
    public Optional<Response> execute() {
        if (logStrategy == null) {
            logStrategy = LogStrategy.ALL;
        }
        Predicate<Response> logCondition = recognizeLogCondition(logStrategy);
        ConditionalLoggingOutputStream outputStream = new ConditionalLoggingOutputStream(LOGGER, Level.INFO);

        outputStream.setLogCondition(logCondition);

        Optional<Response> maybeResponse = actionPoller.task(() -> method.callAPI(outputStream))
                .peek(outputStream::conditionalClose)
                .execute();

        if (!outputStream.hasBeenClosed() || !LogStrategy.NONE.equals(logStrategy)) {
            outputStream.doClose();
        }

        if (afterExecuteAction != null && maybeResponse.isPresent()) {
            afterExecuteAction.accept(maybeResponse.get());
        }
        return maybeResponse;
    }

    private Predicate<Response> recognizeLogCondition(LogStrategy logStrategy) {
        Predicate<Response> result;
        switch (logStrategy) {
        case ALL:
            result = rs -> true;
            break;
        case LAST_ONLY:
            result = actionPoller.getSuccessCondition();
            break;
        case NONE:
            result = rs -> false;
            break;
        default:
            throw new UnsupportedOperationException(String.format("Log strategy with name %s is not supported", logStrategy.name()));
        }
        return result;
    }
}
