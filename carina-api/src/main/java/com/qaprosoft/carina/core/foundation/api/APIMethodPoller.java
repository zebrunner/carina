package com.qaprosoft.carina.core.foundation.api;

import com.qaprosoft.carina.core.foundation.api.log.ConditionalLoggingOutputStream;
import com.qaprosoft.carina.core.foundation.retry.ActionPoller;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.lang.invoke.MethodHandles;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class APIMethodPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    /**
     * Sets the repetition interval for the api calling
     *
     * @param period   repetition interval
     * @param timeUnit time unit
     * @return APIMethodPoller object
     */
    public APIMethodPoller pollEvery(long period, TemporalUnit timeUnit) {
        this.actionPoller.pollEvery(period, timeUnit);
        return this;
    }

    /**
     * Sets the timeout for the api calling
     *
     * @param timeout  timeout
     * @param timeUnit time unit
     * @return APIMethodPoller object
     */
    public APIMethodPoller stopAfter(long timeout, TemporalUnit timeUnit) {
        this.actionPoller.stopAfter(timeout, timeUnit);
        return this;
    }

    /**
     * Sets the logging strategy
     *
     * @param logStrategy logging strategy
     * @return APIMethodPoller object
     */
    public APIMethodPoller withLogStrategy(LogStrategy logStrategy) {
        this.logStrategy = logStrategy;
        return this;
    }

    /**
     * Sets an action that will be executed immediately after the api calling
     *
     * @param peekAction lambda expression
     * @return APIMethodPoller object
     */
    public APIMethodPoller peek(Consumer<Response> peekAction) {
        actionPoller.peek(peekAction);
        return this;
    }

    /**
     * Sets the condition under which the api calling is considered successfully completed and the response is returned
     *
     * @param successCondition lambda expression that that should return true if we consider the api calling completed
     *                         successfully, and false if not
     * @return APIMethodPoller object
     */
    public APIMethodPoller until(Predicate<Response> successCondition) {
        this.actionPoller.until(successCondition);
        return this;
    }

    /**
     * Sets an action that will be executed after an api calling
     *
     * @param afterExecuteAction lambda expression
     * @return APIMethodPoller object
     */
    APIMethodPoller doAfterExecute(Consumer<Response> afterExecuteAction) {
        this.afterExecuteAction = afterExecuteAction;
        return this;
    }

    /**
     * Starts an api calling repetition with a condition. if the condition is met, then the method returns response, otherwise, if
     * the time was elapsed, the method returns null
     *
     * @return response if condition successful, otherwise null
     */
    public Optional<Response> execute() {
        if (logStrategy == null) {
            logStrategy = LogStrategy.ALL;
        }
        Predicate<Response> logCondition = recognizeLogCondition(logStrategy);
        ConditionalLoggingOutputStream outputStream = new ConditionalLoggingOutputStream(LOGGER, Level.INFO);

        outputStream.setLogCondition(logCondition);

        Optional<Response> maybeResponse = actionPoller.task(() -> {
                    method.request.noFilters();
                    outputStream.setBytesOfStreamInvalid();
                    return method.callAPI(outputStream);
                })
                .peek(outputStream::conditionLogging)
                .execute();

        if (LogStrategy.LAST_ONLY.equals(logStrategy) && maybeResponse.isEmpty()) {
            outputStream.flush();
        }
        outputStream.close();

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
