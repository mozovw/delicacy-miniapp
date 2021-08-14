
package com.delicacy.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

public class JsonUtils {

    private JsonUtils() {
    }

    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException var2) {
            return "";
        }
    }

    public static <X> X fromJson(String jsonStr, Class<X> x) {
        try {
            return ObjectUtil.isEmpty(jsonStr) ? null : mapper.readValue(jsonStr, x);
        } catch (IOException var3) {
            return null;
        }
    }

    public static <X> X fromJson(String jsonStr, TypeReference<X> valueTypeRef) {
        try {
            return ObjectUtil.isEmpty(jsonStr) ? null : mapper.readValue(jsonStr, valueTypeRef);
        } catch (IOException var3) {
            return null;
        }
    }

    public static boolean canSerialize(Class<?> type) {
        return mapper.canSerialize(type);
    }

    public static boolean canDeserialize(JavaType type) {
        return mapper.canDeserialize(type);
    }

    static {
        SerializationConfig serializationConfig = mapper.getSerializationConfig();
        serializationConfig = serializationConfig.with(Feature.WRITE_BIGDECIMAL_AS_PLAIN).without(SerializationFeature.WRITE_NULL_MAP_VALUES);
        mapper.setConfig(serializationConfig);
        mapper.setSerializationInclusion(Include.NON_NULL);
        DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
        deserializationConfig = deserializationConfig.with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setConfig(deserializationConfig);
    }
}
