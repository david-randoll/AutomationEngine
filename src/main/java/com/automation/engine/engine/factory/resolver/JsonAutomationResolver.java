package com.automation.engine.engine.factory.resolver;

import com.automation.engine.engine.core.Automation;
import com.automation.engine.engine.factory.CreateAutomation;
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

    public Automation createAutomation(Object json) {
        CreateAutomation createAutomation = mapper.convertValue(json, CreateAutomation.class);
        return manualAutomationResolver.createAutomation(createAutomation);
    }
}