package com.davidrandoll.automation.engine.converter;

import com.davidrandoll.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(value = IYamlConverter.class, ignored = YamlConverter.class)
@ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
public class YamlConverter implements IYamlConverter {
    @Override
    public <T> T convert(String yaml, Class<T> clazz) {
        try {
            var mapper = getYamlObjectMapper();
            return mapper.readValue(yaml, clazz);
        } catch (JsonProcessingException e) {
            throw new AutomationEngineInvalidYamlException(e);
        }
    }

    public ObjectMapper getYamlObjectMapper() {
        return Jackson2ObjectMapperBuilder
                .yaml()
                .build();
    }

    public static class AutomationEngineInvalidYamlException extends RuntimeException {
        public AutomationEngineInvalidYamlException(Throwable cause) {
            super(cause);
        }
    }
}
