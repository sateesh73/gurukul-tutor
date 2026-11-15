package com.gurukul.authservice.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Access Enum & Hierarchy Tests")
class AccessTest {

    @Test
    @DisplayName("Should map each Access level to ROLE_ prefixed authority name")
    void getRoleName() {
        assertThat(Access.ADMIN.getRoleName()).isEqualTo("ROLE_ADMIN");
        assertThat(Access.ASSOCIATE.getRoleName()).isEqualTo("ROLE_ASSOCIATE");
        assertThat(Access.TEACHER.getRoleName()).isEqualTo("ROLE_TEACHER");
        assertThat(Access.STUDENT.getRoleName()).isEqualTo("ROLE_STUDENT");
        assertThat(Access.TEMP.getRoleName()).isEqualTo("ROLE_TEMP");
    }

    @Test
    @DisplayName("Should properly verify access hierarchy with isAtLeast")
    void isAtLeast() {
        // ADMIN (1) has highest privilege
        assertThat(Access.ADMIN.isAtLeast(Access.ADMIN)).isTrue();
        assertThat(Access.ADMIN.isAtLeast(Access.TEACHER)).isTrue();
        assertThat(Access.ADMIN.isAtLeast(Access.STUDENT)).isTrue();
        assertThat(Access.ADMIN.isAtLeast(Access.TEMP)).isTrue();

        // TEACHER (3) cannot access ADMIN (1) or ASSOCIATE (2)
        assertThat(Access.TEACHER.isAtLeast(Access.ADMIN)).isFalse();
        assertThat(Access.TEACHER.isAtLeast(Access.ASSOCIATE)).isFalse();
        assertThat(Access.TEACHER.isAtLeast(Access.TEACHER)).isTrue();
        assertThat(Access.TEACHER.isAtLeast(Access.STUDENT)).isTrue();

        // STUDENT (4)
        assertThat(Access.STUDENT.isAtLeast(Access.TEACHER)).isFalse();
        assertThat(Access.STUDENT.isAtLeast(Access.STUDENT)).isTrue();
        assertThat(Access.STUDENT.isAtLeast(Access.TEMP)).isTrue();

        // Null check
        assertThat(Access.ADMIN.isAtLeast(null)).isFalse();
    }

    @Test
    @DisplayName("Should safely parse role and access strings with fromString")
    void fromString() {
        assertThat(Access.fromString("admin")).isEqualTo(Access.ADMIN);
        assertThat(Access.fromString("ROLE_ADMIN")).isEqualTo(Access.ADMIN);
        assertThat(Access.fromString("teacher")).isEqualTo(Access.TEACHER);
        assertThat(Access.fromString("ROLE_STUDENT")).isEqualTo(Access.STUDENT);
        assertThat(Access.fromString("associate")).isEqualTo(Access.ASSOCIATE);
        assertThat(Access.fromString(null)).isEqualTo(Access.TEMP);
        assertThat(Access.fromString("")).isEqualTo(Access.TEMP);
        assertThat(Access.fromString("UNKNOWN_ROLE")).isEqualTo(Access.TEMP);
    }
}
