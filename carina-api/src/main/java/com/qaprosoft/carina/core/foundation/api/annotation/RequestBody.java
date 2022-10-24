package com.qaprosoft.carina.core.foundation.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.PARAMETER })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RequestBody {

    boolean json() default false;

}
