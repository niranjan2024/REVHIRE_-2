package com.revhire;

import com.revhire.util.LegacyPasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LegacyPasswordUtilTest {

    @Test
    void sha256Hex_IsStableAndDeterministic() {
        String first = LegacyPasswordUtil.sha256Hex("password123");
        String second = LegacyPasswordUtil.sha256Hex("password123");

        assertEquals(first, second);
        assertEquals(64, first.length());
    }

    @Test
    void sha256Hex_DifferentInputsProduceDifferentHashes() {
        assertNotEquals(
                LegacyPasswordUtil.sha256Hex("password123"),
                LegacyPasswordUtil.sha256Hex("password124")
        );
    }
}
