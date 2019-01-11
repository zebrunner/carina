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
package com.qaprosoft.carina.core.foundation.utils.android.recorder.exception;

/**
 *
 */
public class UnsupportedPlatformException extends Exception {

    private static final long serialVersionUID = 7271195042561257354L;

    public UnsupportedPlatformException() {
        super();
    }

    public UnsupportedPlatformException(String message) {
        super(message);
    }

    public UnsupportedPlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}
