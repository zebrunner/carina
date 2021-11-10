/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.qaprosoft.carina.core.foundation.api.http;

public enum ContentTypeEnum {
    JSON(new String[] { "application/json" }),
    XML(new String[] { "application/xml", "text/xml" }),
    NA(new String[] { "n/a" }),;

    public String[] getStringValues() {
        return stringValues;
    }

    private String[] stringValues;

    ContentTypeEnum(String[] stringValues) {
        this.stringValues = stringValues;
    }
}
