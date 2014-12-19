package com.qaprosoft.carina.core.foundation.dataprovider.annotations;

		import java.lang.annotation.ElementType;
		import java.lang.annotation.Retention;
		import java.lang.annotation.RetentionPolicy;
		import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvDataSourceParameters {
	String path();
	//String format() default "EXCEL";
	//char delimeter() default ',';
	//char escape() default '\\';
	String dsArgs() default "";
	String dsUid() default "";
	String executeColumn() default "Execute";
	String executeValue() default "Y";
	String staticArgs() default "";
}