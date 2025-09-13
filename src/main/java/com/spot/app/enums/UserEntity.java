package com.spot.app.enums;

// currently just a reference file
public enum UserEntity {
    ADMIN(1),
    TEST_USER(2);

    private final int id;

    UserEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
