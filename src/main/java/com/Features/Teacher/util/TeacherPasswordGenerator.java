package com.Features.Teacher.util;

import java.util.UUID;

/**
 * Generates the default plain-text password for a newly created teacher account.
 *
 * Rule:   first4(firstName, lowercase) + "@" + last4(phone digits only)
 *
 * Examples:
 *   "John",  "9876543210"  →  "john@3210"
 *   "Ali",   "03001234567" →  "ali@4567"
 *   "Jo",    "9876543210"  →  "jo@3210"   (short name — uses all chars)
 *
 * Fallback (phone null or < 4 digits):
 *   first4(firstName) + "@" + first6(UUID, no dashes)
 */
public final class TeacherPasswordGenerator {

    private TeacherPasswordGenerator() {}

    public static String generate(String firstName, String phone, UUID id) {
        String prefix = firstName.toLowerCase()
                .substring(0, Math.min(4, firstName.length()));

        String suffix;
        if (phone != null && !phone.isBlank()) {
            String digits = phone.replaceAll("\\D", "");
            if (digits.length() >= 4) {
                suffix = digits.substring(digits.length() - 4);
            } else {
                suffix = id.toString().replace("-", "").substring(0, 6);
            }
        } else {
            suffix = id.toString().replace("-", "").substring(0, 6);
        }

        return prefix + "@" + suffix;
    }
}
