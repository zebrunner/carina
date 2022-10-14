package com.qaprosoft.carina.core.foundation.api.annotation;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethod;
import com.qaprosoft.carina.core.foundation.api.interceptor.ApiMethodInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LinkedInterceptors {

    Class<? extends ApiMethodInterceptor<? extends AbstractApiMethod>>[] classes() default {};

}
