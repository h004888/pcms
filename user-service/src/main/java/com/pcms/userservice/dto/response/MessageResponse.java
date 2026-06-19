package com.pcms.userservice.dto.response;

/**
 * Generic success message response used by endpoints that don't return
 * a domain object (e.g. {@code PUT /auth/password}).
 */
public record MessageResponse(String message) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
