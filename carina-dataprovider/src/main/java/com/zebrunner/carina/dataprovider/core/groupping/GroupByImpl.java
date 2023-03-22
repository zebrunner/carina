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
package com.zebrunner.carina.dataprovider.core.groupping;

import java.util.*;

/**
 * Created by Patotsky on 29.12.2014.
 */

@Deprecated(forRemoval = true, since = "1.0.0")
public class GroupByImpl {

    public static Object[][] getGroupedDataProviderArgs(Object[][] objects, int fieldNumber) {
        //add unique group values
        Set<String> groupValues = new LinkedHashSet<>();
        for (Object[] item : objects) {
            String value = (String) item[fieldNumber];
            groupValues.add(value);
        }

        //group maps into lists, that has the same unique group value
        List<List<Object[]>> groupedList = new ArrayList<>();
        for (String groupBy: groupValues) {
            List<Object[]> groupOfRows = new ArrayList<>();
            for (Object[] object : objects) {
                String value = (String) object[fieldNumber];
                if (value.equals(groupBy)) {
                    groupOfRows.add(object);
                }
            }
            groupedList.add(groupOfRows);
        }

        //cast List<List<>> to Object[][]
        Object[][] finalObject = new Object[groupedList.size()][1];
        for (int i = 0; i < groupedList.size(); i++) {
            finalObject[i][0] = groupedList.get(i);
        }

        return finalObject;
    }

    public static Object[][] getGroupedDataProviderMap(Object[][] objects, String fieldName) {
        //add unique group values
        Set<String> groupValues = new LinkedHashSet<>();
        for (Object[] item : objects) {
            String value = ((Map<String,String>) item[0]).get(fieldName);
            groupValues.add(value);
        }

        //group maps into lists, that has the same unique group value
        List<List<Map<String, String>>> groupedList = new ArrayList<>();
        for (String groupBy: groupValues) {
            List<Map<String, String>> groupOfRows = new ArrayList<>();
            for (Object[] item : objects) {
                String value = ((Map<String,String>) item[0]).get(fieldName);
                if (value.equals(groupBy)) {
                    groupOfRows.add((Map<String, String>) item[0]);
                }
            }
            groupedList.add(groupOfRows);
        }

        //cast List<List<>> to Object[][]
        Object[][] finalObject = new Object[groupedList.size()][1];
        for (int i = 0; i < groupedList.size(); i++) {
            finalObject[i][0] = groupedList.get(i);
        }

        return finalObject;
    }
}
