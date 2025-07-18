package com.igot.cb.common.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString()); // ISO-8601 String
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return Instant.parse(json.getAsString());
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Invalid Instant format: " + json.getAsString(), e);
        }
    }
}
