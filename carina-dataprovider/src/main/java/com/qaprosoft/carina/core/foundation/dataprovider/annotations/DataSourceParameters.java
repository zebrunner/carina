package com.qaprosoft.carina.core.foundation.dataprovider.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceParameters {
    String path() default "";

    String dsArgs() default "";

    String dsUid() default "";

    String executeColumn() default "Execute";

    String executeValue() default "y";

    String staticArgs() default "";

    String groupColumn() default "";

    String testRailColumn() default "";

    String qTestColumn() default "";

    String testMethodColumn() default "";

    String testMethodOwnerColumn() default "";

    String bugColumn() default "";
}
