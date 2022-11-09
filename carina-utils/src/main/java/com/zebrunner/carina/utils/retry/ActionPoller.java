package com.zebrunner.carina.utils.retry;

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

import com.zebrunner.carina.utils.common.CommonUtils;

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

    /**
     * Sets the retry time of the task
     *
     * @param period   repetition interval
     * @param timeUnit time unit
     * @return ActionPoller object
     */
    public ActionPoller<T> pollEvery(long period, TemporalUnit timeUnit) {
        this.pollingInterval = Duration.of(period, timeUnit);
        return this;
    }

    /**
     * Sets the timeout for the given task
     *
     * @param timeout  timeout
     * @param timeUnit time unit
     * @return ActionPoller object
     */
    public ActionPoller<T> stopAfter(long timeout, TemporalUnit timeUnit) {
        this.timeout = Duration.of(timeout, timeUnit);
        return this;
    }

    /**
     * Adds an action that will be executed immediately after the task
     *
     * @param peekAction lambda expression
     * @return ActionPoller object
     */
    public ActionPoller<T> peek(Consumer<T> peekAction) {
        this.peekActions.add(peekAction);
        return this;
    }

    /**
     * Sets the task to repeat
     *
     * @param task lambda expression
     * @return ActionPoller object
     */
    public ActionPoller<T> task(Supplier<T> task) {
        this.task = task;
        return this;
    }

    /**
     * Sets the condition under which the task is considered successfully completed and the result is returned
     *
     * @param successCondition lambda expression that that should return true if we consider the task completed
     *                         successfully, and false if not
     * @return ActionPoller object
     */
    public ActionPoller<T> until(Predicate<T> successCondition) {
        this.successCondition = successCondition;
        return this;
    }

    /**
     * Starts a task repetition with a condition. if the condition is met, then the method returns result, otherwise, if
     * the time was elapsed, the method returns null
     *
     * @return result of the task method if condition successful, otherwise returns null
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

    public Predicate<T> getSuccessCondition() {
        return successCondition;
    }
}
