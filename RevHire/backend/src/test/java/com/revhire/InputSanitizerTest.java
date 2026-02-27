package com.revhire;

import com.revhire.util.InputSanitizer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import com.revhire.exception.ApiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputSanitizerTest {

    @Test
    void sanitize_TrimmedValueReturned() {
        assertEquals("hello", InputSanitizer.sanitize("  hello  ", "field"));
    }

    @Test
    void sanitize_BlankReturnsNull() {
        assertNull(InputSanitizer.sanitize("   ", "field"));
    }

    @Test
    void sanitize_UnsafeContentRejected() {
        ApiException ex = assertThrows(ApiException.class,
                () -> InputSanitizer.sanitize("<script>alert(1)</script>", "field"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void require_ThrowsForMissingValue() {
        ApiException ex = assertThrows(ApiException.class,
                () -> InputSanitizer.require(" ", "username"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
