package com.qaprosoft.carina.core.foundation.api;

import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ActionPoller<T> {

    private Duration timeout;
    private Duration pollingInterval;
    private Supplier<T> task;
    private Predicate<T> successCondition;
    private final List<Consumer<T>> peekActions;

    private ActionPoller() {
        this.timeout = Duration.ofSeconds(60);
        this.pollingInterval = Duration.ofSeconds(5);
        this.peekActions = new ArrayList<>();
    }

    public static <T> ActionPoller<T> builder() {
        return new ActionPoller<>();
    }

    /** TODO
     * Sets the repetition interval for lambda expression that should be passed to the task function
     *
     * @param period   repetition interval
     * @param timeUnit time unit
     * @return object of ActionPoller class for setting other parameters for builder or calling execute method for
     * getting the final result
     */
    public ActionPoller<T> pollEvery(long period, TemporalUnit timeUnit) {
        this.pollingInterval = Duration.of(period, timeUnit);
        return this;
    }

    /** TODO
     * Sets a timeout for executing the lambda expression that should be passed to the task function
     *
     * @param timeout  timeout for task
     * @param timeUnit time unit
     * @return object of ActionPoller class for setting other parameters for builder or calling execute method for
     * getting the final result
     */
    public ActionPoller<T> stopAfter(long timeout, TemporalUnit timeUnit) {
        this.timeout = Duration.of(timeout, timeUnit);
        return this;
    }

    // TODO: 18.02.22 DOC
    public ActionPoller<T> peek(Consumer<T> peekAction) {
        this.peekActions.add(peekAction);
        return this;
    }

    /** TODO
     * Accepts a lambda expression that will repeat
     *
     * @param task lambda expression to re-execute
     * @return object of ActionPoller class for setting other parameters for builder or calling execute method for
     * * getting the final result
     */
    public ActionPoller<T> task(Supplier<T> task) {
        this.task = task;
        return this;
    }

    /** TODO
     * Sets the condition under which the task is considered successfully completed and the result is returned
     *
     * @param successCondition lambda expression that that should return true if we consider the task completed
     *                         successfully, and false if not
     * @return object of ActionPoller class for setting other parameters for builder or calling execute method for
     * getting the final result
     */
    public ActionPoller<T> until(Predicate<T> successCondition) {
        this.successCondition = successCondition;
        return this;
    }

    /** TODO
     * Execute  task in intervals with timeout. If condition, that should be set in until function returns true, this
     * method returns result of task, otherwise returns null
     *
     * @return result of task method
     */
    public Optional<T> execute() {
        validateParameters();

        AtomicBoolean stopExecution = setupTerminateTask();
        T result = null;
        while (!stopExecution.get()) {
            T tempResult = task.get();
            peekActions.forEach(peekAction -> peekAction.accept(tempResult));
            if (successCondition.test(tempResult)) {
                result = tempResult;
                break;
            }
            CommonUtils.pause(pollingInterval.getSeconds());
        }
        return Optional.ofNullable(result);
    }

    private AtomicBoolean setupTerminateTask() {
        AtomicBoolean stopExecution = new AtomicBoolean();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopExecution.set(true);
                timer.cancel();
                timer.purge();
            }
        }, timeout.toMillis());
        return stopExecution;
    }

    private void validateParameters() {
        if (task == null) {
            throw new IllegalArgumentException("Unable to execute without task.");
        }

        if (successCondition == null) {
            throw new IllegalArgumentException("Unable to execute without success condition.");
        }

        if (timeout.toMillis() < pollingInterval.toMillis()) {
            throw new IllegalArgumentException("Timeout cannot be less than polling interval");
        }

        if (timeout.isNegative() || pollingInterval.isNegative()) {
            throw new IllegalArgumentException("Timeout or polling interval can't be negative");
        }
    }

    Predicate<T> getSuccessCondition() {
        return successCondition;
    }
}
