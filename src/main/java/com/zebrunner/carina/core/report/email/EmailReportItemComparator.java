/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.report.email;

import java.util.Comparator;

import com.zebrunner.carina.utils.report.TestResultItem;

public class EmailReportItemComparator implements Comparator<TestResultItem> {

    @Override
    public int compare(TestResultItem item1, TestResultItem item2) {
        if (!item1.getPack().equals(item2.getPack())) {
            return item1.getPack().compareTo(item2.getPack());
        } else {
            return item1.getTest().compareTo(item2.getTest());
        }
    }
}
