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
package com.qaprosoft.apitools.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

public class JsonKeywordsComparator extends DefaultComparator {

    private final String[] validationFlags;
    private final List<JsonKeywordComparator> comparators;
    private final JsonComparatorContext context;

    public JsonKeywordsComparator(JSONCompareMode mode, String... validationFlags) {
        this(mode, null, validationFlags);
    }

    public JsonKeywordsComparator(JSONCompareMode mode, JsonComparatorContext context, String... validationFlags) {
        super(mode);
        this.validationFlags = validationFlags;
        this.context = context;
        this.comparators = new ArrayList<>();

        initializeComparators();
    }

    private void initializeComparators() {
        this.comparators.add(new SkipKeywordComparator());
        this.comparators.add(new TypeKeywordComparator());
        this.comparators.add(new RegexKeywordComparator());
        this.comparators.add(new OgnlKeywordsComparator());

        ServiceLoader.load(JsonKeywordComparator.class)
                .forEach(this.comparators::add);

        if (context != null) {
            this.comparators.add(new PredicateKeywordComparator(context.getNamedPredicates()));
            this.comparators.addAll(context.getComparators());
        }
    }

    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
        comparators.stream()
                .filter(comparator -> comparator.isMatch(expectedValue))
                .findFirst()
                .ifPresentOrElse(comparator ->
                                comparator.compare(prefix, expectedValue, actualValue, new JsonCompareResultWrapper(this, result)),
                        () -> compareByDefault(prefix, expectedValue, actualValue, result)
                );
    }

    void compareByDefault(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) {
        super.compareValues(prefix, expectedValue, actualValue, result);
    }

    @Override
    public void compareJSONArray(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        if ((validationFlags != null && validationFlags.length > 0)
                && (ArrayUtils.contains(validationFlags, JsonCompareKeywords.ARRAY_CONTAINS.getKey() + prefix))) {
            // do not validate sizes for arrays
        } else {
            if (expected.length() != actual.length()) {
                result.fail(String.format("%s[]\nArrays length differs. Expected length=%d but actual length=%d\n", prefix,
                        expected.length(), actual.length()));
                return;
            }
        }

        JSONArray actualTmp = new JSONArray();
        for (int i1 = 0; i1 < actual.length(); i1++) {
            actualTmp.put(actual.get(i1));
        }
        for (int i = 0; i < expected.length(); ++i) {
            boolean isEquals = false;
            if (!JSONObject.class.equals(expected.get(i).getClass())) {
                compareJSONArrayForSimpleTypeWContains(prefix, expected, actual, result);
                break;
            }
            JSONObject expectedValue = (JSONObject) expected.get(i);

            JSONObject actValueMostlySimilar = (JSONObject) actualTmp.get(0);
            int actValueMostlySimilarIndex = 0;
            int minErrorsCount = Integer.MAX_VALUE;

            for (int j = 0; j < actualTmp.length(); ++j) {
                JSONObject actualValue = (JSONObject) actualTmp.get(j);
                JSONCompareResult tmpResult = new JSONCompareResult();
                compareValues(prefix + "[" + i + "]", expectedValue, actualValue, tmpResult);
                if (tmpResult.passed()) {
                    isEquals = true;
                    actValueMostlySimilarIndex = j;
                    break;
                }
                if (tmpResult.getFieldFailures().size() < minErrorsCount) {
                    minErrorsCount = tmpResult.getFieldFailures().size();
                    actValueMostlySimilar = actualValue;
                    actValueMostlySimilarIndex = j;
                }
            }
            if (!isEquals) {
                JSONCompareResult tmpResult = new JSONCompareResult();
                super.compareJSON(prefix + "[" + i + "]", expectedValue, actValueMostlySimilar, tmpResult);
                result.fail(tmpResult.getMessage());
            }

            JSONArray arrayAfterRemove = new JSONArray();
            for (int i1 = 0; i1 < actualTmp.length(); i1++) {
                if (i1 != actValueMostlySimilarIndex) {
                    arrayAfterRemove.put(actualTmp.get(i1));
                }
            }
            actualTmp = arrayAfterRemove;
        }
    }

    private void compareJSONArrayForSimpleTypeWContains(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result)
            throws JSONException {
        if (expected.length() == 1 && JsonCompareKeywords.SKIP.getKey().equals(expected.get(0).toString())) {
            return;
        }
        for (int i = 0; i < expected.length(); ++i) {
            boolean isEquals = false;

            for (int j = 0; j < actual.length(); ++j) {
                if (expected.get(i).equals(actual.get(j))) {
                    isEquals = true;
                    break;
                }
            }

            if (!isEquals) {
                result.fail(String.format("%s\nExpected array item '" + expected.get(i) + "' is missed in actual array\n", prefix));
            }
        }
    }
}