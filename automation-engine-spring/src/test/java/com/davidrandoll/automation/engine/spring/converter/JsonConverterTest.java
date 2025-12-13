package com.davidrandoll.automation.engine.spring.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonConverterTest {

    @Mock
    private ObjectMapper objectMapper;

    private JsonConverter jsonConverter;

    @BeforeEach
    void setUp() {
        jsonConverter = new JsonConverter(objectMapper);
    }

    @Test
    void convert_ShouldConvertSuccessfully() throws JsonProcessingException {
        String json = "{\"key\":\"value\"}";
        TestObject expected = new TestObject();
        when(objectMapper.readValue(json, TestObject.class)).thenReturn(expected);

        TestObject result = jsonConverter.convert(json, TestObject.class);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void convert_ShouldThrowRuntimeExceptionOnJsonProcessingException() throws JsonProcessingException {
        String json = "invalid-json";
        when(objectMapper.readValue(eq(json), eq(TestObject.class))).thenThrow(new JsonProcessingException("Error") {});

        assertThatThrownBy(() -> jsonConverter.convert(json, TestObject.class))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(JsonProcessingException.class);
    }

    static class TestObject {}
}
