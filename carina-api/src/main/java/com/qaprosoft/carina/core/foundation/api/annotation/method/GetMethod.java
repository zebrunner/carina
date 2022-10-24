package com.qaprosoft.carina.core.foundation.api.annotation.method;

import com.qaprosoft.apitools.annotation.processor.RelatedTo;
import com.qaprosoft.carina.core.foundation.api.annotation.EndpointTemplateMethod;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@EndpointTemplateMethod(methodType = HttpMethodType.GET)
public @interface GetMethod {

    @RelatedTo(annotationClass = EndpointTemplateMethod.class, field = "url")
    String url() default "";

}
