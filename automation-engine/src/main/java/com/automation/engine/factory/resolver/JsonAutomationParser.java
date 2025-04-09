package com.automation.engine.factory.resolver;

import com.automation.engine.core.Automation;
import com.automation.engine.factory.CreateAutomationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service("jsonAutomationParser")
@RequiredArgsConstructor
public class JsonAutomationParser implements IAutomationFormatParser<String> {
    private final ManualAutomationBuilder builder;
    private final ObjectMapper mapper;

    @Override
    @NonNull
    public Automation create(String json) {
        try {
            CreateAutomationRequest createRequest = mapper.readValue(json, CreateAutomationRequest.class);
            return builder.create(createRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}