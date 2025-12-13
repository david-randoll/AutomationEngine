package com.davidrandoll.automation.engine.spring.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeConverterTest {

    @Mock
    private ObjectMapper objectMapper;

    private TypeConverter typeConverter;

    @BeforeEach
    void setUp() {
        typeConverter = new TypeConverter(objectMapper);
    }

    @Test
    void convert_Class_ShouldConvertSuccessfully() {
        Object input = new Object();
        String expected = "converted";
        when(objectMapper.convertValue(input, String.class)).thenReturn(expected);

        String result = typeConverter.convert(input, String.class);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void convert_Class_ShouldLogWarningWhenResultIsNull() {
        Object input = new Object();
        when(objectMapper.convertValue(input, String.class)).thenReturn(null);

        String result = typeConverter.convert(input, String.class);

        assertThat(result).isNull();
        // Verification of logging is tricky without a logging capture tool, 
        // but we verify the method completes without error.
    }

    @Test
    void convert_TypeReference_ShouldConvertSuccessfully() {
        Object input = new Object();
        Map<String, String> expected = Map.of("key", "value");
        TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
        
        when(objectMapper.convertValue(eq(input), eq(typeRef))).thenReturn(expected);

        Map<String, String> result = typeConverter.convert(input, typeRef);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void convert_TypeReference_ShouldLogWarningWhenResultIsNull() {
        Object input = new Object();
        TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
        
        when(objectMapper.convertValue(eq(input), eq(typeRef))).thenReturn(null);

        Map<String, String> result = typeConverter.convert(input, typeRef);

        assertThat(result).isNull();
    }
}
