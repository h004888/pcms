package com.pcms.pharmacistworkbench.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.Request;
import feign.Util;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Custom Feign encoder to support Map/LinkedHashMap request bodies.
 *
 * <p>Default SpringEncoder from spring-cloud-openfeign does not include a
 * message converter for raw Map types, causing EncodeException when
 * @RequestBody uses Map<String, Object>.
 *
 * <p>This encoder serializes Map bodies directly to JSON bytes using Jackson,
 * fixing 500 errors for /rx/cross-sell, /rx/drug-check, /rx/summary endpoints.
 */
public class FeignMapConfig implements Encoder {

    private final ObjectMapper objectMapper;

    public FeignMapConfig() {
        this(new ObjectMapper());
    }

    public FeignMapConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (object == null) {
            return;
        }
        try {
            byte[] bytes;
            if (object instanceof Map || bodyType == Map.class) {
                bytes = objectMapper.writeValueAsBytes(object);
            } else {
                bytes = objectMapper.writeValueAsBytes(object);
            }
            template.body(bytes, StandardCharsets.UTF_8);
            if (template.headers().get("Content-Type") == null) {
                template.header("Content-Type", "application/json");
            }
        } catch (Exception e) {
            throw new EncodeException("Failed to encode request body: " + e.getMessage(), e);
        }
    }
}