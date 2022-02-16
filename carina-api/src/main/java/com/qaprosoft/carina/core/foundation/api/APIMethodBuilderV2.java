package com.qaprosoft.carina.core.foundation.api;

import com.qaprosoft.carina.core.foundation.api.log.ConditionLoggingOutputStream;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import io.restassured.response.Response;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class APIMethodBuilderV2 {

    private static final Logger LOGGER = Logger.getLogger(AbstractApiMethod.class);

    private AbstractApiMethodV2 apiMethod;
    private Duration timeout;
    private Duration pollingInterval;
    private Predicate<Response> successCondition;

    private APIMethodBuilderV2() {
    }

    public static APIMethodBuilderV2 builder() {
        return new APIMethodBuilderV2();
    }

    public APIMethodBuilderV2 pollEvery(long period, TemporalUnit timeUnit) {
        this.pollingInterval = Duration.of(period, timeUnit);
        return this;
    }

    public APIMethodBuilderV2 apiMethod(AbstractApiMethodV2 method) {
        this.apiMethod = method;
        return this;
    }

    public APIMethodBuilderV2 stopAfter(long timeout, TemporalUnit timeUnit) {
        this.timeout = Duration.of(timeout, timeUnit);
        return this;
    }

    public APIMethodBuilderV2 until(Predicate<Response> successCondition) {
        this.successCondition = successCondition;
        return this;
    }

    public Response execute() {
        AtomicBoolean stopExecution = setupTerminateTask(timeout.toMillis());
        Response result = null;
        ConditionLoggingOutputStream loggingOutputStream = new ConditionLoggingOutputStream(LOGGER, Level.INFO);
        while (!stopExecution.get()) {
            Response tempResult;
            tempResult = apiMethod.callAPI(loggingOutputStream);
            if (successCondition.test(tempResult)) {
                result = tempResult;
                break;
            }
            CommonUtils.pause(pollingInterval.getSeconds());
        }
        loggingOutputStream.logging();
        return result;
    }

    private AtomicBoolean setupTerminateTask(Long timeoutInMillis) {
        AtomicBoolean stopExecution = new AtomicBoolean();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopExecution.set(true);
                timer.cancel();
                timer.purge();
            }
        }, timeoutInMillis);
        return stopExecution;
    }
}
