package org.endeavourhealth.core.utility;

import com.google.gson.Gson;

public class JsonSerializer {
    public static String serialize(Object source) {
        Gson gson = new Gson();
        String json = gson.toJson(source);
        return json;
    }

    public static byte[] serializeAsBytes(Object source) {
        return serialize(source).getBytes();
    }

    public static <T> T deserialize(String value, Class<T> classOfT) {
        Gson gson = new Gson();
        T r = gson.fromJson(value, classOfT);
        return r;
    }
}