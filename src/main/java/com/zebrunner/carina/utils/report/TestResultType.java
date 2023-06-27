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
package com.zebrunner.carina.utils.report;

/**
 * Test result type.
 * 
 * @author Aliaksei_Khursevich (hursevich@gmail.com)
 */
public enum TestResultType {
    PASS("PASSED"),
    PASS_WITH_KNOWN_ISSUES("PASSED (known issues)"),
    FAIL("FAILED"),
    SKIP("SKIPPED");

    private final String result;

    TestResultType(String result) {
        this.result = result;
    }

    public String getName() {
        return result;
    }
}
