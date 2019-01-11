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
package com.qaprosoft.carina.core.foundation.exception;

/*
 * Exception that may be thrown when creating new test threads for performance testing.
 * 
 * @author Alex Khursevich
 */
public class TestCreationException extends Exception {
    private static final long serialVersionUID = 1204359727358878609L;

    public TestCreationException() {
        super("Test creation exception");
    }

    public TestCreationException(String msg) {
        super("Test creation exception: " + msg);
    }
}
