package com.automation.engine.engine.factory.resolver;

import com.automation.engine.engine.core.Automation;
import com.automation.engine.engine.factory.CreateAutomation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonAutomationResolver {
    private final ManualAutomationResolver manualAutomationResolver;
    private final ObjectMapper mapper;

    public Automation createAutomation(String json) {
        try {
            CreateAutomation createAutomation = mapper.readValue(json, CreateAutomation.class);
            return manualAutomationResolver.createAutomation(createAutomation);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}