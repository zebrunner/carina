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

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

import com.zebrunner.carina.utils.commons.SpecialKeywords;

public class LogicUtils {
    private static Random random;

    static {
        random = new Random();
    }

    public static boolean isURLEqual(String url1, String url2) {
        url1 = StringUtils.replace(url1, "https://", "http://");
        url2 = StringUtils.replace(url2, "https://", "http://");

        url1 = StringUtils.removeEnd(url1, "/");
        url2 = StringUtils.removeEnd(url2, "/");

        url1 = url1.contains("?") ? url1.substring(0, url1.indexOf("?")) : url1;
        url2 = url2.contains("?") ? url2.substring(0, url2.indexOf("?")) : url2;

        if (url1.contains(SpecialKeywords.IGNORE) || url2.contains(SpecialKeywords.IGNORE)) {
            String[] urlAr1 = url1.split("/");
            String[] urlAr2 = url2.split("/");
            return compareWithIgnore(urlAr1, urlAr2);
        }
        return url1.equals(url2);
    }

    public static boolean isAllTrue(boolean... cases) {
        for (int i = 0; i < cases.length; i++) {
            if (!cases[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean compareWithIgnore(String[] urlAr1, String[] urlAr2) {
        int count = Math.min(urlAr1.length, urlAr2.length);
        if (urlAr1 != null && urlAr2 != null) {
            for (int i = 0; i < count; i++) {
                // 2 use-cases when different levels are compared using $ignore field
                if ((urlAr1.length - 1) <= i && SpecialKeywords.IGNORE.equals(urlAr2[i])) {
                    //use-case when url1 is finished but url2 contains only $ignore sub-domain
                    continue;
                } else if ((urlAr2.length - 1) <= i && SpecialKeywords.IGNORE.equals(urlAr1[i])) {
                    //use-case when url2 is finished but url1 contains only $ignore sub-domain
                    continue;
                } else if (SpecialKeywords.IGNORE.equals(urlAr1[i]) || SpecialKeywords.IGNORE.equals(urlAr2[i])) {
                    continue;
                } else if (!urlAr1[i].equalsIgnoreCase(urlAr2[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static int getRandomNumber(int max) {
        return max == 0 ? 0 : random.nextInt(max);
    }

    public static WebElement selectRandomElement(List<WebElement> elements) {
        return elements != null ? elements.get(getRandomNumber(elements.size())) : null;
    }
}
