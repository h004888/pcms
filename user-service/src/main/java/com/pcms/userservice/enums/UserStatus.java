package com.pcms.userservice.enums;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED   // BR05: locked after 5 failed login attempts
}
