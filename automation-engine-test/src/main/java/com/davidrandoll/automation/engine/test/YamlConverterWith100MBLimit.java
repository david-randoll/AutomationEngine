package com.davidrandoll.automation.engine.test;

import com.davidrandoll.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;

@Service
@RequiredArgsConstructor
public class YamlConverterWith100MBLimit implements IYamlConverter {
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
        var options = new LoaderOptions();
        options.setCodePointLimit(100 * 1024 * 1024);
        var factory = YAMLFactory.builder()
                .loaderOptions(options)
                .build();
        var mapper = Jackson2ObjectMapperBuilder
                .yaml()
                .factory(factory)
                .build();

        // Handle coercion of empty strings to null
        mapper.coercionConfigDefaults()
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);

        return mapper;
    }

    public static class AutomationEngineInvalidYamlException extends RuntimeException {
        public AutomationEngineInvalidYamlException(Throwable cause) {
            super(cause);
        }
    }
}
