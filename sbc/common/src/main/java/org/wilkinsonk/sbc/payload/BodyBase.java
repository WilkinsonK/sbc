package org.wilkinsonk.sbc.payload;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BodyBase {
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T FromJson(String json, Class<T> clazz) throws Exception {
        return (T) OBJECT_MAPPER.readValue(json, clazz);
    }

    public String IntoJson() throws Exception {
        return OBJECT_MAPPER.writeValueAsString(this);
    }
}
