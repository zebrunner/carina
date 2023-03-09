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
package com.qaprosoft.carina.core.foundation.dataprovider.core.groupping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.sun.istack.NotNull;

/**
 * Created by Yauheni_Patotski on 1/7/2015.
 */
public class GroupByMapper {

    private static final ThreadLocal<Integer> COLUMN_ID_FOR_GROUPING = new ThreadLocal<>();
    private static final ThreadLocal<String> COLUMN_NAME_FOR_GROUPING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_HASH_MAPPED = ThreadLocal.withInitial(() -> Boolean.FALSE);
    @Deprecated(forRemoval = true, since = "8.0.6")
    private static Set<Integer> instanceInt;
    @Deprecated(forRemoval = true, since = "8.0.6")
    private static Set<String> instanceString;

    public static boolean isHashMapped() {
        return IS_HASH_MAPPED.get() == null ? false : IS_HASH_MAPPED.get();
    }

    public static void setIsHashMapped(boolean isHashMapped) {
        IS_HASH_MAPPED.set(isHashMapped);
    }

    private GroupByMapper() {}
    public static Optional<Integer> getNumberOfColumnForGrouping() {
        Integer columnId = null;
        if (COLUMN_ID_FOR_GROUPING.get() != null) {
            columnId = COLUMN_ID_FOR_GROUPING.get();
        }
        return Optional.ofNullable(columnId);
    }

    public static void setNumberOfColumnForGrouping(@NotNull Integer columnNumber) {
        // todo add checks
        COLUMN_ID_FOR_GROUPING.set(columnNumber);
    }

    /**
     * @deprecated use {@link #getNumberOfColumnForGrouping()}
     * @return
     */
    @Deprecated(forRemoval = true, since = "8.0.6")
    public static Set<Integer> getInstanceInt() {
        if (instanceInt == null) {
            instanceInt = Collections.synchronizedSet(new HashSet<Integer>());
        }
        return instanceInt;
    }

    public static Optional<String> getNameOfColumnForGrouping() {
        String columnName = null;
        if (COLUMN_NAME_FOR_GROUPING.get() != null && !COLUMN_NAME_FOR_GROUPING.get().isEmpty()) {
            columnName = COLUMN_NAME_FOR_GROUPING.get();
        }
        return Optional.ofNullable(columnName);
    }

    public static void setNameOfColumnForGrouping(@NotNull String name) {
        Objects.requireNonNull(name);
        COLUMN_NAME_FOR_GROUPING.set(name);
    }

    /**
     * @deprecated use {@link #getNameOfColumnForGrouping()}
     */
    @Deprecated(forRemoval = true, since = "8.0.6")
    public static Set<String> getInstanceStrings() {

        if (instanceString == null) {
            instanceString = Collections.synchronizedSet(new HashSet<String>());
        }
        return instanceString;
    }

    /**
     * Clear grouping settings in current thread
     */
    public static void clear() {
        COLUMN_ID_FOR_GROUPING.remove();
        COLUMN_NAME_FOR_GROUPING.remove();
        IS_HASH_MAPPED.remove();
    }
}