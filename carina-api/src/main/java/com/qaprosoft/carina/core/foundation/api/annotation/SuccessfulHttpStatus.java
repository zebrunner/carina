package com.qaprosoft.carina.core.foundation.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SuccessfulHttpStatus {
    HttpResponseStatusType status();
}
