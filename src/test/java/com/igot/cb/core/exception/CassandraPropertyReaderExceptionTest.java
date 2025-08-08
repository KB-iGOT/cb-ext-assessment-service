package com.igot.cb.core.exception;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CassandraPropertyReaderExceptionTest {

    @Test
    void testConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        CassandraPropertyReaderException ex =
                new CassandraPropertyReaderException("error message", cause);

        assertEquals("error message", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
