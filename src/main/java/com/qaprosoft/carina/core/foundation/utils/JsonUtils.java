package com.qaprosoft.carina.core.foundation.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Created by yauhenipatotski on 4/13/16.
 */
public final class JsonUtils {

    private static ObjectMapper mapper;

    private JsonUtils() {

    }

    static {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
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
            return mapper.writeValueAsString(src);
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
