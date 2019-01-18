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
package com.qaprosoft.carina.core.foundation.utils;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

public class StringGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final String GENERATE = "$generate";

    private static final Random RANDOM = new Random();

    public static String generateWord(int keySize) {

        StringBuilder result = new StringBuilder();

        String base = generateBase(keySize);
        int position = RANDOM.nextInt(ALPHABET.length() - 1);
        int sign = -1;

        for (int i = 0; i < keySize; i++) {

            int step = Integer.valueOf(base.substring(i, i + 1)) * sign;
            if (position + step > 0 && position + step < ALPHABET.length() - 1) {
                position += step;
            } else {
                position -= step;
            }
            result.append(ALPHABET.charAt(position));
            sign *= -1;
        }

        return result.toString();
    }

    public static String generateNumeric(int keySize) {
        return RandomStringUtils.randomNumeric(keySize);
    }

    public static String generateWordAN(int keySize) {
        return RandomStringUtils.randomAlphanumeric(keySize);
    }

    private static String generateBase(int keySize) {

        String base = "";

        for (int i = 0; i < keySize; i++) {
            base += String.valueOf(RANDOM.nextInt(9));
        }
        return base;
    }

    public static void generateInputParameters(Object[] params) {

        for (int i = 0; i < params.length; i++) {
            if (params[i].toString().contains(GENERATE)) {
                String newLine = params[i].toString();
                int size = Integer.valueOf(newLine.split(":")[1]);
                newLine = newLine.substring(0, newLine.indexOf(GENERATE));
                newLine += generateWord(size);
                params[i] = newLine;
            }
        }
    }

    public static String getUniqueItemName() {
        return "qatest-" + System.nanoTime();
    }

    public static String generateEmail() {
        return generateWord(10) + "@gmail.com";
    }
}