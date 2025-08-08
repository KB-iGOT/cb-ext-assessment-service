package com.igot.cb.common.util;

import com.google.gson.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InstantTypeAdapterTest {

    private final InstantTypeAdapter adapter = new InstantTypeAdapter();

    @Test
    void testSerialize() {
        Instant now = Instant.now();
        JsonElement json = adapter.serialize(now, Instant.class, null);
        assertTrue(json.isJsonPrimitive());
        assertEquals(now.toString(), json.getAsString());
    }

    @Test
    void testDeserialize() {
        Instant now = Instant.now();
        JsonPrimitive json = new JsonPrimitive(now.toString());
        Instant result = adapter.deserialize(json, Instant.class, null);
        assertEquals(now, result);
    }

    @Test
    void testDeserializeInvalidFormat() {
        JsonPrimitive json = new JsonPrimitive("invalid-instant");
        assertThrows(JsonParseException.class, () ->
                adapter.deserialize(json, Instant.class, null)
        );
    }
}
