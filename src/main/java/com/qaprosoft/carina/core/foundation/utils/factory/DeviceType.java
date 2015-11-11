package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeviceType {

	enum Type {

		DESKTOP("desktop", "desktop"), ANDROID_TABLET("android_tablet",
				"android"), ANDROID_PHONE("android_phone", "android"), IOS_TABLET(
				"ios_tablet", "ios"), IOS_PHONE("ios_phone", "ios");

		private String type;

		private String family;

		Type(String type, String family) {
			this.type = type;
			this.family = family;
		}
		
		public String getType(){
			return type;
		}

		public String getFamily(){
			return family;
		}
	}

	Type pageType() default Type.ANDROID_PHONE;

	Class<?> parentClass();
	
	String[] version() default { "1.0" };

}