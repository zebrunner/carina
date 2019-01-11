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

import java.io.File;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Created by yauhenipatotski on 4/13/16.
 */
public final class JsonUtils {

    private static ObjectMapper mapper;

    private JsonUtils() {

    }

    static {
        mapper = new ObjectMapper();
        // mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        // mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return mapper.readValue(json, classOfT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String toJson(Object src) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(src);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T fromJson(File file, Class<T> classOfT) {
        try {
            return mapper.readValue(file, classOfT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T fromJson(File file, Type type) {
        try {
            TypeFactory tf = mapper.getTypeFactory();
            JavaType javaType = tf.constructType(type);
            return mapper.readValue(file, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Type type) {
        try {
            TypeFactory tf = mapper.getTypeFactory();
            JavaType javaType = tf.constructType(type);
            return mapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static JsonNode readTree(String content) {
        try {
            return mapper.readTree(content);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T treeToValue(JsonNode node, Class<? extends T> type) {
        try {
            return mapper.treeToValue(node, type);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
