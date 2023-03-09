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
package com.zebrunner.carina.api.apitools.builder;

import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zebrunner.carina.api.apitools.util.GenerationUtil;

public class GenerateProcessor implements PropertiesProcessor {

    @Override
    public Properties process(Properties in) {
        Properties out = new Properties();
        for (Entry<Object, Object> entry : in.entrySet()) {
            Matcher wordMatcher = Pattern.compile(PropertiesKeywords.GENERATE_WORD_REGEX.getKey()).matcher(entry.getValue().toString());
            Matcher numberMatcher = Pattern.compile(PropertiesKeywords.GENERATE_NUMBER_REGEX.getKey()).matcher(entry.getValue().toString());
            Matcher dateMatcher = Pattern.compile(PropertiesKeywords.GENERATE_DATE_REGEX.getKey()).matcher(entry.getValue().toString());
            String tmp = entry.getValue().toString();
            boolean generated = false;

            while (wordMatcher.find()) {
                String toReplace = wordMatcher.group();
                Matcher tmpMatcher = Pattern.compile("\\d+").matcher(toReplace);
                tmpMatcher.find();
                String length = tmpMatcher.group();
                tmp = tmp.replace(toReplace, GenerationUtil.generateWord(Integer.parseInt(length)));
                generated = true;
            }

            while (numberMatcher.find()) {
                String toReplace = numberMatcher.group();
                Matcher tmpMatcher = Pattern.compile("\\d+").matcher(toReplace);
                tmpMatcher.find();
                String length = tmpMatcher.group();
                tmp = tmp.replace(toReplace, GenerationUtil.generateNumber(Integer.parseInt(length)));
                generated = true;
            }

            while (dateMatcher.find()) {
                String toReplace = dateMatcher.group();
                // getting offset
                Matcher offsetMatcher = Pattern.compile("-{0,1}\\d+").matcher(toReplace);
                offsetMatcher.find();
                String offset = offsetMatcher.group();
                // getting format
                Matcher formatMatcher = Pattern.compile("(?<=generate_date\\().*?(?=;)").matcher(toReplace);
                formatMatcher.find();
                String format = formatMatcher.group();
                // generating date
                tmp = tmp.replace(toReplace,
                        GenerationUtil.generateTime(format, Integer.parseInt(offset), Calendar.DAY_OF_YEAR));
                generated = true;
            }

            if (generated)
                out.put(entry.getKey(), tmp);
        }
        return out;
    }
}
