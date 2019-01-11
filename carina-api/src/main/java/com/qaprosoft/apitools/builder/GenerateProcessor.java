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
package com.qaprosoft.apitools.builder;

import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qaprosoft.apitools.util.GenerationUtil;

public class GenerateProcessor implements PropertiesProcessor {

    @Override
    public Properties process(Properties in) {
        Properties out = new Properties();
        for (Entry<Object, Object> entry : in.entrySet()) {
            Matcher wordMatcher = Pattern.compile(PropertiesKeywords.GENERATE_WORD_REGEX.getKey()).matcher(entry.getValue().toString());
            Matcher numberMatcher = Pattern.compile(PropertiesKeywords.GENERATE_NUMBER_REGEX.getKey()).matcher(entry.getValue().toString());
            Matcher dateMatcher = Pattern.compile(PropertiesKeywords.GENERATE_DATE_REGEX.getKey()).matcher(entry.getValue().toString());
            {
                if (wordMatcher.find()) {
                    String toReplace = wordMatcher.group();
                    Matcher tmpMatcher = Pattern.compile("\\d+").matcher(toReplace);
                    tmpMatcher.find();
                    String length = tmpMatcher.group();
                    out.put(entry.getKey(), entry.getValue().toString().replace(toReplace, GenerationUtil.generateWord(Integer.parseInt(length))));
                } else if (numberMatcher.find()) {
                    String toReplace = numberMatcher.group();
                    Matcher tmpMatcher = Pattern.compile("\\d+").matcher(toReplace);
                    tmpMatcher.find();
                    String length = tmpMatcher.group();
                    out.put(entry.getKey(), entry.getValue().toString().replace(toReplace, GenerationUtil.generateNumber(Integer.parseInt(length))));
                } else if (dateMatcher.find()) {
                    String toReplace = dateMatcher.group();
                    // getting offset
                    Matcher offsetMatcher = Pattern.compile("-{0,1}\\d+").matcher(entry.getValue().toString());
                    offsetMatcher.find();
                    String offset = offsetMatcher.group();
                    // getting format
                    Matcher formatMatcher = Pattern.compile("(?<=generate_date\\().*(?=;)").matcher(entry.getValue().toString());
                    formatMatcher.find();
                    String format = formatMatcher.group();
                    // generating date
                    out.put(entry.getKey(), entry.getValue().toString().replace(toReplace,
                            GenerationUtil.generateTime(format, Integer.parseInt(offset), Calendar.DAY_OF_YEAR)));
                } else {
                    out.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return out;
    }
    // public static void main(String[] args) {
    // Properties p = new Properties();
    // p.put("word", "generate_word(7)");
    // p.put("date", "generate_date(yyyy-MM-dd;5)");
    // p = new GenerateProcessor().process(p);
    // System.out.println(p.getProperty("word"));
    // System.out.println(p.getProperty("date"));
    // }
}
