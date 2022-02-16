package com.qaprosoft.carina.core.foundation.api;

import com.qaprosoft.carina.core.foundation.api.log.ControlLoggingOutputStream;
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

    private static final Logger LOGGER = Logger.getLogger(APIMethodBuilderV2.class);

    private AbstractApiMethodV2 apiMethod;
    private Duration timeout;
    private Duration pollingInterval;
    private Predicate<Response> successCondition;

    private APIMethodBuilderV2() {
    }

    public static APIMethodBuilderV2 builder() {
        return new APIMethodBuilderV2();
    }

    /**
     * Sets the time to repeat the calling of the callAPI function
     *
     * @param period   The interval through which calling will be repeated
     * @param timeUnit unit of time in which the repetition will occur
     * @return object of APIMethodBuilderV2 class
     */
    public APIMethodBuilderV2 pollEvery(long period, TemporalUnit timeUnit) {
        this.pollingInterval = Duration.of(period, timeUnit);
        return this;
    }

    /**
     * Sets the api method builder
     *
     * @param method object of AbstractApiMethodV2 class
     * @return object of APIMethodBuilderV2 class
     */
    public APIMethodBuilderV2 apiMethod(AbstractApiMethodV2 method) {
        this.apiMethod = method;
        return this;
    }

    /**
     * Sets the time after which the repetition of calling the callAPI function will stop
     *
     * @param timeout  timeout to successfully complete a recurring calling
     * @param timeUnit unit of time in which the timeout will be indicated
     * @return object of APIMethodBuilderV2 class
     */
    public APIMethodBuilderV2 stopAfter(long timeout, TemporalUnit timeUnit) {
        this.timeout = Duration.of(timeout, timeUnit);
        return this;
    }

    /**
     * Sets a condition. If it is successful the repetition of the callAPI function will stop
     *
     * @param successCondition lambda expression that returns true if the received value from calling callAPI function suits
     *                         us and we want to stop repetition and false otherwise
     * @return object of APIMethodBuilderV2 class
     */
    public APIMethodBuilderV2 until(Predicate<Response> successCondition) {
        this.successCondition = successCondition;
        return this;
    }

    /**
     * A function that start repeating the callAPI function
     *
     * @return the value of the action expression if the until method succeeds, and null otherwise
     */
    public Response execute() {
        AtomicBoolean stopExecution = setupTerminateTask(timeout.toMillis());
        Response result = null;
        ControlLoggingOutputStream loggingOutputStream = new ControlLoggingOutputStream(LOGGER, Level.INFO);
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
