package com.tutor.gurukul.users.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * The Users class represents a user entity in the system. It is annotated with JPA annotations to map it to a database table.
 * The class contains fields for user information such as first name, last name, email, password, phone number, role, address, and company ID.
 * The class also includes Lombok annotations to generate boilerplate code such as getters, setters, constructors, and builders.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_users", schema = "gurukul")
class Users {

    /**
     * The unique identifier for the user. This field is the primary key of the Users entity and is generated automatically using a UUID strategy.
     * It is annotated with @Id to indicate that it is the primary key and @GeneratedValue to specify the generation strategy. The @Column annotation specifies the column name in the database and indicates that this field cannot be null or updated after creation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;
    /**
     * The first name of the user. This field is mapped to the "first_name" column in the database and is marked as non-nullable, meaning that a value must be provided when creating a new user record. The @Column annotation is used to specify the column name and constraints for this field.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;
    /**
     * The last name of the user. This field is mapped to the "last_name" column in the database and is marked as non-nullable, meaning that a value must be provided when creating a new user record. The @Column annotation is used to specify the column name and constraints for this field.
     */
    @Column(name = "last_name", nullable = false)
    private String lastName;
    /**
     * The email address of the user. This field is mapped to the "email" column in the database and is marked as non-nullable, meaning that a value must be provided when creating a new user record. The @Column annotation is used to specify the column name and constraints for this field.
     */
    @Column(name = "email", nullable = false)
    private String email;
    /**
     * The password of the user. This field is mapped to the "password" column in the database and is marked as non-nullable, meaning that a value must be provided when creating a new user record. The @Column annotation is used to specify the column name and constraints for this field.
     */
    @Column(name = "password", nullable = false)
    private String password;
    /**
     * The phone number of the user. This field is mapped to the "phone_number" column in the database and is marked as nullable, meaning that a value may be provided when creating a new user record. The @Column annotation is used to specify the column name and constraints for this field.
     */
    @Column(name = "phone_number")
    private String phoneNumber;
    /**
     * The role of the user. This field is mapped to the "role" column in the database and is marked as non-nullable, meaning that a value must be provided when creating a new user record. The @Enumerated annotation is used to specify that this field should be stored as a string in the database.
     */
    @Enumerated(value = EnumType.STRING)
    private Role role;
    /**
     * The address of the user. This field is an embedded object that contains the user's address details. The @Embedded annotation indicates that this field should be stored as an embedded object in the database.
     */
    @Embedded
    private Address address;
    /**
     * The ID of the company to which the user belongs. This field is mapped to the "company_id" column in the database and is marked as non-nullable, meaning that a value must be provided when creating a new user record. The @Column annotation is used to specify the column name and constraints for this field.
     */
    @Column(name = "company_id", nullable = false)
    private String companyId;
}
