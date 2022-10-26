package com.qaprosoft.carina.core.foundation.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface QueryParam {

    String key();

    String value();

}
