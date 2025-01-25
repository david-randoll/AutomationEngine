package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import com.automation.engine.factory.CreateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service("yamlAutomationResolver")
@RequiredArgsConstructor
public class YamlAutomationResolver implements IAutomationResolver<String> {
    private final ManualAutomationResolver manualAutomationResolver;

    @Override
    public Automation create(String yaml) {
        var mapper = getYamlObjectMapper();
        try {
            CreateRequest createRequest = mapper.readValue(yaml, CreateRequest.class);
            return manualAutomationResolver.create(createRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectMapper getYamlObjectMapper() {
        return Jackson2ObjectMapperBuilder.yaml().build();
    }
}