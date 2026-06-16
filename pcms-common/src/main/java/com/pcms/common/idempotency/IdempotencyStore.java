package com.pcms.common.idempotency;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * In-memory idempotency store backed by Caffeine cache (B-14).
 *
 * <p>Stores (key, request-hash, response) tuples for 24h. Used by
 * {@link IdempotencyKeyFilter} to:
 * <ul>
 *   <li>Replay cached response when same key + same hash comes again</li>
 *   <li>Return {@code 409 Conflict} when same key + different hash (key reuse)</li>
 * </ul>
 *
 * <p>For multi-instance deployments, swap this for a Redis-backed implementation.
 */
@Component
public class IdempotencyStore {

    public record Entry(String key, String requestHash, String responseBody, int statusCode) {}

    private final Cache<String, Entry> cache = Caffeine.newBuilder()
            .expireAfterWrite(IdempotencyHeaders.TTL_SECONDS, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .build();

    /**
     * Compute deterministic hash of the request payload.
     */
    public String hashRequest(String body) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(body == null ? new byte[0] : body.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString((body == null ? 0 : body.hashCode()));
        }
    }

    /**
     * Look up an existing entry for the given key.
     */
    public Optional<Entry> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    /**
     * Save the response for future replay.
     */
    public void put(String key, String requestHash, String responseBody, int statusCode) {
        cache.put(key, new Entry(key, requestHash, responseBody, statusCode));
    }

    /**
     * Remove a key (e.g., after explicit failure).
     */
    public void remove(String key) {
        cache.invalidate(key);
    }
}
