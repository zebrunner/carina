package com.qaprosoft.carina.core.foundation.api.annotation;

import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.PARAMETER })
@Retention(value = RetentionPolicy.RUNTIME)
@SuccessfulHttpStatus(status = HttpResponseStatusType.OK_200)
public @interface SuccessfulHttpStatusParam {
}
