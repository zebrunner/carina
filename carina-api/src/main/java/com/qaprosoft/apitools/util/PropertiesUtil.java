/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.apitools.util;

import java.util.Properties;

public class PropertiesUtil {

    public static Properties readProperties(String path) {
        Properties prop = new Properties();
        try {
            prop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(path));
        } catch (Exception e) {
            throw new RuntimeException("Can't read properties from file", e);
        }
        return prop;
    }

}
