package com.gurukul.authservice.user;

import lombok.Getter;

import java.util.Optional;

/**
 * Access represents the hierarchical authorization role of a user in the Gurukul platform.
 * Lower level integer denotes higher administrative privilege:
 * ADMIN (1) > ASSOCIATE (2) > TEACHER (3) > STUDENT (4) > TEMP (5).
 */
@Getter
public enum Access {
    ADMIN(1),
    ASSOCIATE(2),
    TEACHER(3),
    STUDENT(4),
    TEMP(5);

    private final int level;

    Access(int level) {
        this.level = level;
    }

    /**
     * Returns the Spring Security role representation (e.g. ROLE_ADMIN).
     */
    public String getRoleName() {
        return "ROLE_" + name();
    }

    /**
     * Checks if this access level meets or exceeds the required access level.
     * Since lower level number = higher privilege, level <= required.level.
     */
    public boolean isAtLeast(Access required) {
        return required != null && this.level <= required.level;
    }

    /**
     * Safely parse a role/access string, handling prefixes like "ROLE_", trimming,
     * case-insensitivity, and defaulting to TEMP if unrecognized or blank.
     */
    public static Access fromString(String value) {
        return parse(value).orElse(TEMP);
    }

    /**
     * Parses a role/access string into an Optional<Access>.
     */
    public static Optional<Access> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String clean = value.trim().toUpperCase();
        if (clean.startsWith("ROLE_")) {
            clean = clean.substring(5);
        }
        try {
            return Optional.of(Access.valueOf(clean));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}