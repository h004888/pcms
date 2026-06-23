package com.pcms.reportservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Custom Feign encoder + decoder to support Map/LinkedHashMap for both
 * request bodies and response bodies.
 *
 * <p>Default SpringEncoder/SpringDecoder do not handle raw Map types.
 * This config uses Jackson directly to serialize/deserialize Map freely.
 *
 * <p>Fixes: 500 errors for endpoints that exchange Map with downstream services.
 */
public class FeignMapConfig implements Encoder, Decoder {

    private final ObjectMapper objectMapper;

    public FeignMapConfig() {
        this(new ObjectMapper());
    }

    public FeignMapConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ============== Encoder ==============
    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (object == null) {
            return;
        }
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(object);
            template.body(bytes, StandardCharsets.UTF_8);
            if (template.headers().get("Content-Type") == null) {
                template.header("Content-Type", "application/json");
            }
        } catch (Exception e) {
            throw new EncodeException("Failed to encode request body: " + e.getMessage(), e);
        }
    }

    // ============== Decoder ==============
    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.body() == null) {
            return null;
        }
        String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
        if (body.isEmpty()) {
            return null;
        }
        try {
            // Auto-detect from JSON body content
            String trimmed = body.trim();
            if (trimmed.startsWith("[")) {
                // Array response - deserialize as List of Map
                return objectMapper.readValue(body,
                        objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, Map.class));
            }
            if (trimmed.startsWith("{")) {
                // Object response - deserialize as Map
                return objectMapper.readValue(body, Map.class);
            }
            // Fallback: use declared type
            return objectMapper.readValue(body, objectMapper.constructType(type));
        } catch (Exception e) {
            throw new IOException("Failed to decode response body: " + e.getMessage(), e);
        }
    }
}