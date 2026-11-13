package com.gurukul.authservice.user;

import lombok.Getter;

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

}