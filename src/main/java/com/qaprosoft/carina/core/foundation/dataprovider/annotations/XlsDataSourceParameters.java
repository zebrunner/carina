package com.qaprosoft.carina.core.foundation.dataprovider.annotations;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsDataSourceParameters {
    String className() default "com.qaprosoft.carina.core.foundation.dataprovider.core.impl.XlsDataProvider";

    String path() default "";

    String sheet() default "";

    String dsArgs() default "";

    String dsUid() default "";

    String executeColumn() default "Execute";

    String executeValue() default "y";

    String staticArgs() default "";

    String groupColumn() default "";
    
    String jiraColumn() default "";
    
    String spiraColumn() default "";
    
    String testRailColumn() default "";
}
