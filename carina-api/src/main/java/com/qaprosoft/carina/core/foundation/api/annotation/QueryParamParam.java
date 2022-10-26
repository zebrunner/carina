package com.qaprosoft.carina.core.foundation.api.annotation;

import com.qaprosoft.apitools.annotation.processor.RelatedTo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.PARAMETER })
@Retention(value = RetentionPolicy.RUNTIME)
@QueryParam(key = "", value = "")
public @interface QueryParamParam {

    @RelatedTo(annotationClass = QueryParam.class, field = "key")
    String key();

}
