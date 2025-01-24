package com.automation.engine.engine.factory.resolver;

import com.automation.engine.engine.core.Automation;
import com.automation.engine.engine.factory.CreateAutomation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class YamlAutomationResolver {
    private final ManualAutomationResolver manualAutomationResolver;

    public Automation createAutomation(String yaml) {
        var mapper = getYamlObjectMapper();
        try {
            CreateAutomation createAutomation = mapper.readValue(yaml, CreateAutomation.class);
            return manualAutomationResolver.createAutomation(createAutomation);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectMapper getYamlObjectMapper() {
        return Jackson2ObjectMapperBuilder.yaml().build();
    }
}