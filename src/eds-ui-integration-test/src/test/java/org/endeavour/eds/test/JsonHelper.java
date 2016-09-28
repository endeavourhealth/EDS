package org.endeavour.eds.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonHelper {

    public static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public static <T> T toObject(String content, TypeReference<?> typeReference) {
        try {
            return objectMapper().readValue(content, typeReference);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toJson(Object value) {
        try {
            return objectMapper().writeValueAsString(value);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
