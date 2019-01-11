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
package com.qaprosoft.carina.grid;

import java.util.Map;

/**
 * Platforms available in Selenium Grid.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public enum Platform {
    ANY,
    ANDROID,
    IOS,
    WINDOWS,
    MAC,
    LINUX;

    /**
     * Retrieves platform type from capabilities.
     * 
     * @param cap - desired capabilities
     * @return platform
     */
    public static Platform fromCapabilities(Map<String, Object> cap) {
        Platform platform = Platform.ANY;

        if (cap != null && cap.containsKey("platform") && cap.get("platform") != null) {
            platform = Platform.valueOf(cap.get("platform").toString().toUpperCase());
        }
        if (cap != null && cap.containsKey("platformName") && cap.get("platformName") != null) {
            platform = Platform.valueOf(cap.get("platformName").toString().toUpperCase());
        }

        return platform;
    }
}
