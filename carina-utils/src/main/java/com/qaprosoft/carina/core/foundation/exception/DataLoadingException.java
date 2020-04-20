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
package com.qaprosoft.carina.core.foundation.exception;

/*
 * Exception may be thrown when exception in data loading occurred.
 * 
 * @author Alex Khursevich
 */
public class DataLoadingException extends RuntimeException {
    private static final long serialVersionUID = -6264855148555485530L;

    public DataLoadingException() {
        super("Can't load data.");
    }

    public DataLoadingException(String msg) {
        super("Can't load data: " + msg);
    }
}