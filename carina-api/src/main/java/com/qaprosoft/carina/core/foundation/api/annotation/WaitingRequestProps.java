package com.qaprosoft.carina.core.foundation.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface WaitingRequestProps {

    /**
     * @return interval between requests
     */
    int period() default 1;

    /**
     * @return maximum waiting time
     */
    int delay() default 5;

    /**
     * @return information for period and delay time parameters
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
