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
package com.zebrunner.carina.api;

import java.io.File;
import java.io.PrintStream;
import java.util.UUID;

import com.zebrunner.carina.utils.report.ReportContext;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

public class APIMethodBuilder {

    private final File temp;
    private final PrintStream ps;

    public APIMethodBuilder() {
        temp = new File(String.format("%s/%s.tmp", ReportContext.getTempDir().getAbsolutePath(), UUID.randomUUID()));
        try {
            ps = new PrintStream(temp);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public <T extends AbstractApiMethod> T build(T method) {
        method.getRequest().filter(new RequestLoggingFilter(ps)).filter(new ResponseLoggingFilter(ps));
        return method;
    }

    public File getTempFile() {
        return temp;
    }

    public void close() {
        try {
            ps.close();
            temp.delete();
        } catch (Exception e) {
        }
    }
}
