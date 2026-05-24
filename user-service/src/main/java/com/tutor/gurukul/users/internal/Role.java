package com.tutor.gurukul.users.internal;

/*
 * This enum represents the different roles that a user can have in the system.
 * It is used to determine the permissions and access levels for each user.
 * The roles defined in this enum are:
 * - STUDENT: Represents a user who is a student and has limited access to certain features of the system.
 * - TUTOR: Represents a user who is a tutor and has access to additional features related to tutoring and managing students.
 * - ADMIN: Represents a user who is an administrator and has full access to all features of the system, including managing users, companies, and other administrative tasks.
 */
enum Role {
    STUDENT,
    TUTOR,
    ADMIN
}
