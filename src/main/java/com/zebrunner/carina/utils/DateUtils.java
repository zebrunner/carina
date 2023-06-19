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
package com.zebrunner.carina.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.zebrunner.carina.core.config.ReportConfiguration;
import com.zebrunner.carina.utils.config.Configuration;

public final class DateUtils {
    private static final String DATE_FORMAT = Configuration.getRequired(ReportConfiguration.Parameter.DATE_FORMAT);
    private static final String TIME_FORMAT = Configuration.getRequired(ReportConfiguration.Parameter.TIME_FORMAT);

    private DateUtils() {
        // hide
    }

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(cal.getTime());
    }

    public static String time() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        return sdf.format(cal.getTime());
    }

    public static String timeDiff(long startDate) {
        long seconds = (new Date().getTime() - startDate) / 1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h, m, s);
    }
    
    public static String timeFormat(long elapsedDate) {
        long seconds = (elapsedDate) / 1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h, m, s);
    }
}
