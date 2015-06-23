package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeviceType {

	public enum Type {
		DESKTOP, ANDROID_TABLET, ANDROID_PHONE, IOS_TABLET, IOS_PHONE
	}

	Type pageType() default Type.ANDROID_PHONE;

	Class<?> parentClass();

}