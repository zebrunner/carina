package com.qaprosoft.carina.core.foundation.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(QueryParam.List.class)
@Target(value = { ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface QueryParam {

    String key();

    String value();

    @Target(value = { ElementType.TYPE, ElementType.METHOD })
    @Retention(value = RetentionPolicy.RUNTIME)
    @interface List {

        QueryParam[] value();

    }
}
