package com.qaprosoft.carina.core.foundation.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.qaprosoft.carina.core.foundation.api.http.Headers.JSON_CONTENT_TYPE;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentType {
    String type() default JSON_CONTENT_TYPE;
}
