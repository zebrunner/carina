/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeviceType {

    enum Type {

        DESKTOP("desktop", "desktop"),
        ANDROID_TABLET("android_tablet", "android"),
        ANDROID_PHONE(
                "android_phone",
                "android"),
        ANDROID_TV("android_tv", "android"),
        IOS_TABLET("ios_tablet", "ios"),
        IOS_PHONE(
                "ios_phone",
                "ios");

        private String type;

        private String family;

        Type(String type, String family) {
            this.type = type;
            this.family = family;
        }

        public String getType() {
            return type;
        }

        public String getFamily() {
            return family;
        }
    }

    Type pageType() default Type.ANDROID_PHONE;

    Class<?> parentClass();

    String[] version() default { "1.0" };

}