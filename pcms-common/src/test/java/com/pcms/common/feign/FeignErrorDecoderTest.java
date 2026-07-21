package com.pcms.common.feign;

import com.pcms.common.exception.BusinessException;
import com.pcms.common.exception.DownstreamServiceException;
import com.pcms.common.exception.ResourceNotFoundException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignErrorDecoderTest {

    private FeignErrorDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new FeignErrorDecoder(new feign.codec.ErrorDecoder.Default());
    }

    @Test
    void decode_404_returnsResourceNotFoundException_withBodyMessage() {
        String bodyJson = "{\"code\":\"MSG31\",\"status\":404,\"message\":\"Medicine not found: b78cd03c\",\"messageVi\":\"Không tìm thấy thuốc: b78cd03c\"}";
        Response response = buildResponse(404, bodyJson);

        Exception ex = decoder.decode("CatalogClient#getById", response);

        assertThat(ex).isInstanceOf(ResourceNotFoundException.class);
        ResourceNotFoundException rnf = (ResourceNotFoundException) ex;
        assertThat(rnf.getCode()).isEqualTo("MSG31");
        assertThat(rnf.getHttpStatus()).isEqualTo(404);
        assertThat(rnf.getMessage()).contains("Medicine not found");
    }

    @Test
    void decode_404_withoutBody_returnsResourceNotFoundException_withDefaultMessage() {
        Response response = buildResponse(404, null);

        Exception ex = decoder.decode("CatalogClient#getById", response);

        assertThat(ex).isInstanceOf(ResourceNotFoundException.class);
        ResourceNotFoundException rnf = (ResourceNotFoundException) ex;
        assertThat(rnf.getCode()).isEqualTo("MSG31");
        assertThat(rnf.getHttpStatus()).isEqualTo(404);
        assertThat(rnf.getMessage()).contains("Feign call failed");
    }

    @Test
    void decode_400_returnsBusinessException_withMSG33() {
        String bodyJson = "{\"code\":\"MSG33\",\"status\":400,\"message\":\"Bad request\"}";
        Response response = buildResponse(400, bodyJson);

        Exception ex = decoder.decode("CatalogClient#getById", response);

        assertThat(ex).isInstanceOf(BusinessException.class);
        BusinessException be = (BusinessException) ex;
        assertThat(be.getCode()).isEqualTo("MSG33");
        assertThat(be.getHttpStatus()).isEqualTo(400);
        assertThat(be.getMessage()).contains("Bad request");
    }

    @Test
    void decode_500_returnsBusinessException_withMSG34() {
        Response response = buildResponse(500, null);

        Exception ex = decoder.decode("CatalogClient#getById", response);

        assertThat(ex).isInstanceOf(BusinessException.class);
        BusinessException be = (BusinessException) ex;
        assertThat(be.getCode()).isEqualTo("MSG34");
        assertThat(be.getHttpStatus()).isEqualTo(500);
    }

    @Test
    void decode_503_returnsBusinessException_withMSG34() {
        Response response = buildResponse(503, null);

        Exception ex = decoder.decode("CatalogClient#getById", response);

        assertThat(ex).isInstanceOf(BusinessException.class);
        BusinessException be = (BusinessException) ex;
        assertThat(be.getCode()).isEqualTo("MSG34");
        assertThat(be.getHttpStatus()).isEqualTo(503);
    }

    @Test
    void decode_401_returnsBusinessException_withMSG01() {
        Response response = buildResponse(401, null);

        Exception ex = decoder.decode("CatalogClient#getById", response);

        assertThat(ex).isInstanceOf(BusinessException.class);
        BusinessException be = (BusinessException) ex;
        assertThat(be.getCode()).isEqualTo("MSG01");
        assertThat(be.getHttpStatus()).isEqualTo(401);
    }

    @Test
    void decode_409_returnsBusinessException_withMSG09() {
        Response response = buildResponse(409, null);

        Exception ex = decoder.decode("CatalogClient#getById", response);

        assertThat(ex).isInstanceOf(BusinessException.class);
        BusinessException be = (BusinessException) ex;
        assertThat(be.getCode()).isEqualTo("MSG09");
        assertThat(be.getHttpStatus()).isEqualTo(409);
    }

    @Test
    void decode_200_delegatesToDefaultDecoder_doesNotReturnBusinessException() {
        Response response = buildResponse(200, "{\"id\":\"some-id\"}");

        Exception ex = decoder.decode("CatalogClient#getById", response);

        assertThat(ex).isNotInstanceOf(BusinessException.class);
    }

    private static Response buildResponse(int status, String bodyJson) {
        byte[] body = bodyJson != null ? bodyJson.getBytes(StandardCharsets.UTF_8) : new byte[0];
        Request request = Request.create(
                Request.HttpMethod.GET,
                "http://localhost/test",
                Collections.emptyMap(),
                Request.Body.empty(),
                new RequestTemplate());
        return Response.builder()
                .status(status)
                .reason(status == 404 ? "Not Found" : "Error")
                .headers(Map.of("Content-Type", Collections.singletonList("application/json")))
                .body(body)
                .request(request)
                .build();
    }
}
