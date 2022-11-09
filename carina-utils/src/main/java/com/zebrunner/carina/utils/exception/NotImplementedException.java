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
package com.zebrunner.carina.utils.exception;

import java.util.Arrays;
import java.util.List;

import com.zebrunner.carina.utils.Configuration;
import org.apache.commons.lang3.StringUtils;

/*
 * Exception can be thrown when some method is not implemented for platform.
 * 
 * @author Dzmitry Pinchuk
 */
public class NotImplementedException extends RuntimeException {
    private static final long serialVersionUID = 1147240484643530919L;

    public NotImplementedException() {
        super(generateMessage());
    }
    
    private static String generateMessage() {
        String platform = Configuration.getPlatform();
        List<StackTraceElement> elements = Arrays.asList(Thread.currentThread().getStackTrace());
        String currentMethodName = elements.get(1).getClassName();
        currentMethodName = StringUtils.substringAfterLast(currentMethodName, ".");
        int index = 0;
        for (StackTraceElement element : elements) {
            if (element.getClassName().contains(currentMethodName)) {
                index = elements.indexOf(element) + 2;
                break;
            }
        }
        if (index == 0) {
            throw new UnsupportedOperationException("Unable to identificate correct position of method in stackTrace!");
        }
        String methodName = elements.get(index).getMethodName();
        return String.format("Method [%s] isn't implemented for %s!", methodName, platform);
    }

}
