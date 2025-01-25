package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import com.automation.engine.factory.CreateRequest;
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
            CreateRequest createRequest = mapper.readValue(json, CreateRequest.class);
            return manualAutomationResolver.create(createRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}