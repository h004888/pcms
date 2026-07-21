package com.pcms.common.feign;

import com.pcms.common.exception.DownstreamServiceException;
import com.pcms.common.exception.ResourceNotFoundException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder;

    public FeignErrorDecoder(ErrorDecoder defaultDecoder) {
        this.defaultDecoder = defaultDecoder;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        if (status >= 200 && status < 300) {
            return defaultDecoder.decode(methodKey, response);
        }

        String bodyMessage = extractBodyMessage(response);
        String defaultMessage = buildDefaultMessage(methodKey, status);

        if (status == 404) {
            String message = bodyMessage != null ? bodyMessage : defaultMessage;
            return new ResourceNotFoundException(message, message);
        }

        String code = mapStatusCodeToMessageCode(status);
        String message = bodyMessage != null ? bodyMessage : defaultMessage;
        return new DownstreamServiceException(code, status, message, message);
    }

    private String buildDefaultMessage(String methodKey, int status) {
        return "Feign call failed [" + methodKey + "]: HTTP " + status;
    }

    private String mapStatusCodeToMessageCode(int status) {
        return switch (status) {
            case 400 -> "MSG33";
            case 401 -> "MSG01";
            case 403 -> "MSG31";
            case 404 -> "MSG31";
            case 409 -> "MSG09";
            case 422 -> "MSG33";
            case 429 -> "MSG34";
            default -> "MSG34";
        };
    }

    private String extractBodyMessage(Response response) {
        if (response.body() == null) {
            return null;
        }
        try {
            byte[] bodyBytes = Util.toByteArray(response.body().asInputStream());
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            if (body.contains("\"message\"")) {
                int start = body.indexOf("\"message\"") + 10;
                int end = body.indexOf("\"", start + 1);
                if (start > 9 && end > start) {
                    return body.substring(start + 1, end);
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
