package com.automation.engine.engine.factory.resolver;

import com.automation.engine.engine.core.Automation;
import com.automation.engine.engine.factory.CreateAutomation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("jsonAutomationResolver")
@RequiredArgsConstructor
public class JsonAutomationResolver implements IAutomationResolver<String> {
    private final ManualAutomationResolver manualAutomationResolver;
    private final ObjectMapper mapper;

    @Override
    public Automation create(String json) {
        try {
            CreateAutomation createAutomation = mapper.readValue(json, CreateAutomation.class);
            return manualAutomationResolver.create(createAutomation);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}