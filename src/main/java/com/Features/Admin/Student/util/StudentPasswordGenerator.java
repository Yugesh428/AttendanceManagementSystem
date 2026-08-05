package com.Features.Admin.Student.util;

import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────
 * StudentPasswordGenerator
 * Location: com/Features/Admin/Student/util/StudentPasswordGenerator.java
 * ──────────────────────────────────────────────────────────────────────────────
 *
 * Generates the default plain-text password for a newly created student account.
 *
 * RULE:
 *   password = first4(firstName, lowercase) + "@" + last4(phone)
 *
 * EXAMPLES:
 *   firstName="John",  phone="9876543210"  →  "john@3210"
 *   firstName="Ali",   phone="03001234567" →  "ali@4567"
 *   firstName="Jo",    phone="9876543210"  →  "jo@3210"   (firstName < 4 chars → use all)
 *
 * FALLBACK (phone is null or shorter than 4 digits):
 *   password = first4(firstName) + "@" + first6(uuid, no dashes)
 *   e.g. "john@a1b2c3"
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * NOTE: This class only produces the PLAIN-TEXT password.
 *       BCrypt hashing happens in StudentServiceImpl before persisting.
 *       The plain-text value is returned once in the API response (generatedPassword)
 *       and is NEVER stored in the database.
 * ──────────────────────────────────────────────────────────────────────────────
 */
public final class StudentPasswordGenerator {

    private StudentPasswordGenerator() {
        // utility class — no instantiation
    }

    /**
     * Generate the default password for a student.
     *
     * @param firstName student's first name (must not be null)
     * @param phone     student's phone number (may be null or short)
     * @param id        student's UUID (used as fallback when phone is unavailable)
     * @return plain-text generated password
     */
    public static String generate(String firstName, String phone, UUID id) {
        // ── Prefix: up to 4 chars of firstName, lowercased ────────────────────
        String prefix = firstName.toLowerCase()
                .substring(0, Math.min(4, firstName.length()));

        // ── Suffix: last 4 digits of phone, or UUID fallback ─────────────────
        String suffix;
        if (phone != null && phone.replaceAll("\\D", "").length() >= 4) {
            // strip non-digits, take last 4
            String digitsOnly = phone.replaceAll("\\D", "");
            suffix = digitsOnly.substring(digitsOnly.length() - 4);
        } else {
            // fallback: first 6 chars of UUID (no dashes)
            suffix = id.toString().replace("-", "").substring(0, 6);
        }

        return prefix + "@" + suffix;
    }
}
