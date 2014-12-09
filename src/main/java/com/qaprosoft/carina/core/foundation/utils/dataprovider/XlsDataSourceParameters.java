package com.qaprosoft.carina.core.foundation.utils.dataprovider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsDataSourceParameters {
	String path() default "";
	String sheet() default "";
	String dsArgs() default "";
	String dsUid() default "";
	String executeColumn() default "Execute";
	String executeValue() default "y";
	String staticArgs() default "";
}
