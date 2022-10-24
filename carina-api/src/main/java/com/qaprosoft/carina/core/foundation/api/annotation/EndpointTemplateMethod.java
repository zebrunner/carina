package com.qaprosoft.carina.core.foundation.api.annotation;

import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EndpointTemplateMethod {

    String url() default "";

    HttpMethodType methodType();

}
