package com.iseekfree.common.sdk.common.assertion;

import com.iseekfree.common.sdk.common.exception.AtlasException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtlasAssertTest {

    @Test
    void isTrueAllowsValidConditionAndRejectsInvalidCondition() {
        AtlasAssert.isTrue(true, 1001, "unused");

        AtlasException exception = assertThrows(AtlasException.class, () -> AtlasAssert.isTrue(false, "invalid"));
        assertEquals(AtlasException.DEFAULT_CODE, exception.getCode());
        assertEquals("invalid", exception.getMessage());
    }

    @Test
    void notNullReturnsOriginalValueAndSupportsExplicitCode() {
        Object value = new Object();
        assertSame(value, AtlasAssert.notNull(value, "unused"));

        AtlasException exception = assertThrows(AtlasException.class, () -> AtlasAssert.notNull(null, -1001, "missing"));
        assertEquals(-1001, exception.getCode());
        assertEquals("missing", exception.getMessage());
    }

    @Test
    void notBlankRejectsNullAndWhitespaceOnlyValues() {
        String value = "  ready  ";
        assertSame(value, AtlasAssert.notBlank(value, "unused"));

        assertThrows(AtlasException.class, () -> AtlasAssert.notBlank(null, "required"));
        assertThrows(AtlasException.class, () -> AtlasAssert.notBlank(" \t\n", "required"));
        assertSame(value, AtlasAssert.hasText(value, "unused"));
    }

    @Test
    void failAlwaysRaisesAtlasException() {
        AtlasException exception = assertThrows(AtlasException.class, () -> AtlasAssert.<Object>fail(-1002, "failed"));
        assertEquals(-1002, exception.getCode());
        assertEquals("failed", exception.getMessage());
    }
}
