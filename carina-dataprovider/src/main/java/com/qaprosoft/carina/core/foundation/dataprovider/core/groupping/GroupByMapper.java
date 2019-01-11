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
package com.qaprosoft.carina.core.foundation.dataprovider.core.groupping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Yauheni_Patotski on 1/7/2015.
 */
public class GroupByMapper {

    private static Set<Integer> instanceInt;

    private static Set<String> instanceString;

    private static boolean hashMapped = false;

    public static boolean isHashMapped() {
        return hashMapped;
    }

    public static void setIsHashMapped(boolean isHashMapped) {
        GroupByMapper.hashMapped = isHashMapped;
    }

    public static Set<Integer> getInstanceInt() {
        if (instanceInt == null) {
            instanceInt = Collections.synchronizedSet(new HashSet<Integer>());
        }
        return instanceInt;
    }

    public static Set<String> getInstanceStrings() {
        if (instanceString == null) {
            instanceString = Collections.synchronizedSet(new HashSet<String>());
        }
        return instanceString;
    }
}