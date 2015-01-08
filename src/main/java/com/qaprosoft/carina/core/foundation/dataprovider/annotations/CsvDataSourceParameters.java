package com.qaprosoft.carina.core.foundation.dataprovider.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvDataSourceParameters {
    String className() default "com.qaprosoft.carina.core.foundation.dataprovider.core.impl.CsvDataProvider";

    String path();

    char separator() default ',';

    char quote() default '"';

    String dsArgs() default "";

    String dsUid() default "";

    String executeColumn() default "Execute";

    String executeValue() default "y";

    String staticArgs() default "";

    String groupColumn() default "";
}