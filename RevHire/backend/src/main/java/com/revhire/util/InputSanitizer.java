package com.revhire.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class InputSanitizer {

    private InputSanitizer() {
    }

    public static String sanitize(String value, String fieldName) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String lower = trimmed.toLowerCase();
        if (lower.contains("<script") || lower.contains("<iframe") || lower.contains("onload=")) {
            throw new com.revhire.exception.BadRequestException( fieldName + " contains unsafe content");
        }
        return trimmed;
    }

    public static String require(String value, String fieldName) {
        String sanitized = sanitize(value, fieldName);
        if (sanitized == null) {
            throw new com.revhire.exception.BadRequestException( fieldName + " is required");
        }
        return sanitized;
    }
}
