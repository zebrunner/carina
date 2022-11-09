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
package com.zebrunner.carina.utils.messager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zebrunner.carina.utils.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.testng.Reporter;

/**
 * ReportMessage is used for reporting informational and error messages both
 * using needed loggers.
 *
 * @author brutskov
 */

public interface IMessager {

    Pattern CRYPTO_PATTERN = Pattern.compile(Configuration.get(Configuration.Parameter.CRYPTO_PATTERN));

    String getPattern();

    Logger getLogger();

    default String getMessage(String... args) {
        return create(args);
    }

    /**
     * Logs info message using message pattern and incoming parameters.
     *
     * @param args
     *            for insert into patterns
     * @return generated message
     */
    default String info(String... args) {
        String message = create(args);
        getLogger().info(message);
        return message;
    }

    /**
     * Logs error message and adds message to TestNG report.
     *
     * @param args
     *            for insert into patterns
     * @return generated message
     */
    default String error(String... args) {
        String message = create(args);
        Reporter.log(message);
        getLogger().error(message);
        return message;
    }
    
    /**
     * Logs warn message and adds message to TestNG report.
     *
     * @param args
     *            for insert into patterns
     * @return generated message
     */
    default String warn(String... args) {
        String message = create(args);
        Reporter.log(message);
        getLogger().warn(message);
        return message;
    }    

    /**
     * Generates error message using message pattern and incoming parameters.
     *
     * @param args
     *            for insert into pattern
     * @return generated message
     */
    default String create(String... args) {
        String message = "";
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    Matcher matcher = CRYPTO_PATTERN.matcher(args[i]);
                    if (matcher.find()) {
                        int start = args[i].indexOf(':') + 1;
                        int end = args[i].indexOf('}');
                        args[i] = StringUtils.replace(args[i], matcher.group(), StringUtils.repeat('*', end - start));
                    }
                }
            }
            message = String.format(getPattern(), (Object[]) args);
        } catch (Exception e) {
            getLogger().error("Report message creation error!", e);
        }
        return message;
    }
}
