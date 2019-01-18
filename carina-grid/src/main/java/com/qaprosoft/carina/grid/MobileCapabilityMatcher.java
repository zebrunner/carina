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

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.grid.internal.utils.DefaultCapabilityMatcher;

/**
 * Custom selenium capability matcher for mobile grid.
 * {@link https://nishantverma.gitbooks.io/appium-for-android/understanding_desired_capabilities.html}
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class MobileCapabilityMatcher extends DefaultCapabilityMatcher {
    private static final String PLATFORM_NAME = "platformName";
    private static final String PLATFORM_VERSION = "platformVersion";
    private static final String DEVICE_NAME = "deviceName";
    private static final String DEVICE_TYPE = "deviceType";
    private static final String DEVICE_POOL = "devicePool";
    private static final String UDID = "udid";

    @Override
    public boolean matches(Map<String, Object> nodeCapability, Map<String, Object> requestedCapability) {
        if (requestedCapability.containsKey(PLATFORM_NAME) || requestedCapability.containsKey(PLATFORM_VERSION)
                || requestedCapability.containsKey(DEVICE_NAME) || requestedCapability.containsKey(UDID)
                || requestedCapability.containsKey(DEVICE_POOL)) {
            // Mobile-based capabilities
            return extensionCapabilityCheck(nodeCapability, requestedCapability);
        } else {
            // Browser-based capabilities
            return super.matches(nodeCapability, requestedCapability);
        }
    }

    /**
     * Verifies matching between requested and actual node capabilities.
     * 
     * @param nodeCapability
     *            - Selenium node capabilities
     * @param requestedCapability
     *            - capabilities requested by Selenium client
     * @return match results
     */
    private boolean extensionCapabilityCheck(Map<String, Object> nodeCapability,
            Map<String, Object> requestedCapability) {
    	
    		// If devicePool is found in requested capabilities then more likely deviceName=ANY for QPS_GGR usage
    		if(requestedCapability.containsKey(DEVICE_POOL)) {
    			requestedCapability.put(DEVICE_NAME, requestedCapability.get(DEVICE_POOL));
		}
    	
        for (String key : requestedCapability.keySet()) {
            String expectedValue = requestedCapability.get(key) != null ? requestedCapability.get(key).toString()
                    : null;

            String actualValue = (nodeCapability.containsKey(key) && nodeCapability.get(key) != null)
                    ? nodeCapability.get(key).toString()
                    : null;

            if (!("ANY".equalsIgnoreCase(expectedValue) || "".equals(expectedValue) || "*".equals(expectedValue))) {
                switch (key) {
                case PLATFORM_NAME:
                    if (actualValue != null && !StringUtils.equalsIgnoreCase(actualValue, expectedValue)) {
                        return false;
                    }
                    break;
                case PLATFORM_VERSION:
                    if (actualValue != null) {
                        // Limited interval: 6.1.1-7.0
                        if (expectedValue.matches("(\\d+\\.){0,}(\\d+)-(\\d+\\.){0,}(\\d+)$")) {
                            PlatformVersion actPV = new PlatformVersion(actualValue);
                            PlatformVersion minPV = new PlatformVersion(expectedValue.split("-")[0]);
                            PlatformVersion maxPV = new PlatformVersion(expectedValue.split("-")[1]);

                            if (actPV.compareTo(minPV) < 0 || actPV.compareTo(maxPV) > 0) {
                                return false;
                            }
                        }
                        // Unlimited interval: 6.0+
                        else if (expectedValue.matches("(\\d+\\.){0,}(\\d+)\\+$")) {
                            PlatformVersion actPV = new PlatformVersion(actualValue);
                            PlatformVersion minPV = new PlatformVersion(expectedValue.replace("+", ""));

                            if (actPV.compareTo(minPV) < 0) {
                                return false;
                            }
                        }
                        // Multiple versions: 6.1,7.0
                        else if (expectedValue.matches("(\\d+\\.){0,}(\\d+,)+(\\d+\\.){0,}(\\d+)$")) {
                            boolean matches = false;
                            for (String version : expectedValue.split(",")) {
                                if (new PlatformVersion(version).compareTo(new PlatformVersion(actualValue)) == 0) {
                                    matches = true;
                                    break;
                                }
                            }
                            if (!matches) {
                                return false;
                            }
                        }
                        // Exact version: 7.0
                        else if (expectedValue.matches("(\\d+\\.){0,}(\\d+)$")) {
                            if (new PlatformVersion(expectedValue).compareTo(new PlatformVersion(actualValue)) != 0) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    break;
                case DEVICE_NAME:
                    if (actualValue != null && !Arrays.asList(expectedValue.split(",")).contains(actualValue)) {
                        return false;
                    }
                    break;
                case DEVICE_TYPE:
                    if (actualValue != null && !StringUtils.equalsIgnoreCase(actualValue, expectedValue)) {
                        return false;
                    }
                    break;
                case UDID:
                    if (actualValue != null && !Arrays.asList(expectedValue.split(",")).contains(actualValue)) {
                        return false;
                    }
                    break;
                default:
                    break;
                }
            }
        }

        return true;
    }

    public class PlatformVersion implements Comparable<PlatformVersion> {
        private int[] version;

        public PlatformVersion(String v) {
            if (v != null && v.matches("(\\d+\\.){0,}(\\d+)$")) {
                String[] digits = v.split("\\.");
                this.version = new int[digits.length];
                for (int i = 0; i < digits.length; i++) {
                    this.version[i] = Integer.valueOf(digits[i]);
                }
            }
        }

        public int[] getVersion() {
            return version;
        }

        public void setVersion(int[] version) {
            this.version = version;
        }

        @Override
        public int compareTo(PlatformVersion pv) {
            int result = 0;
            if (pv != null && pv.getVersion() != null && this.version != null) {
                int minL = Math.min(this.version.length, pv.getVersion().length);
                int maxL = Math.max(this.version.length, pv.getVersion().length);

                for (int i = 0; i < minL; i++) {
                    result = this.version[i] - pv.getVersion()[i];
                    if (result != 0) {
                        break;
                    }
                }

                if (result == 0 && this.version.length == minL && minL != maxL) {
                    result = -1;
                } else if (result == 0 && this.version.length == maxL && minL != maxL) {
                    result = 1;
                }
            }
            return result;
        }
    }
}