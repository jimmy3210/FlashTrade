package com.tradex.flashtrade.dto.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ApiResponseContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void successUsesTheUnifiedResponseShape() throws Exception {
        JsonNode body = objectMapper.readTree(objectMapper.writeValueAsBytes(
                ApiResponse.success("ok")));

        assertThat(body.path("code").asText()).isEqualTo("SUCCESS");
        assertThat(body.path("message").asText()).isEqualTo("请求成功");
        assertThat(body.path("data").asText()).isEqualTo("ok");
        assertThat(body.has("success")).isFalse();
        assertThat(body.has("error")).isFalse();
    }

    @Test
    void errorsKeepTraceAndFieldErrorsUnderData() throws Exception {
        JsonNode body = objectMapper.readTree(objectMapper.writeValueAsBytes(
                ApiResponse.error(
                        "VALIDATION_ERROR",
                        "请求参数校验失败",
                        java.util.List.of(new ApiResponse.FieldError(
                                "username", "REQUIRED", "用户名不能为空")),
                        "trace-123")));

        assertThat(body.path("code").asText()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.path("data").path("traceId").asText()).isEqualTo("trace-123");
        assertThat(body.path("data").path("fieldErrors").get(0).path("field").asText())
                .isEqualTo("username");
        assertThat(body.has("error")).isFalse();
    }
}
