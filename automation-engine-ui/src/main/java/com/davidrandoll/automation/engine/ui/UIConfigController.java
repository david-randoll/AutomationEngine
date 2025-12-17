package com.davidrandoll.automation.engine.ui;

import com.davidrandoll.automation.engine.backend.api.AutomationEngineBackendApiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UIConfigController {
        private final ServerProperties serverProperties;
        private final AutomationEngineUiProperties uiProperties;
        private final AutomationEngineBackendApiProperties backendApiProperties;
        private final ObjectMapper objectMapper;

        @GetMapping(value = {
                        "/app-config.js",
                        "/*/app-config.js",
                        "/*/*/app-config.js",
                        "/*/*/*/app-config.js"
        }, produces = "application/javascript")
        public String getAppConfig() throws JsonProcessingException {
                String appConfigValues = objectMapper.writeValueAsString(getAppConfigJson());
                return "window.__APP_CONFIG__ = %s;".formatted(appConfigValues);
        }

        @GetMapping(value = {
                        "/app-config.json",
                        "/*/app-config.json",
                        "/*/*/app-config.json",
                        "/*/*/*/app-config.json"
        }, produces = "application/json")
        public Map<String, String> getAppConfigJson() {
                String contextPath = Optional.ofNullable(serverProperties.getServlet().getContextPath()).orElse("");
                return Map.of(
                                "contextPath", contextPath,
                                "uiPath", uiProperties.getPath(),
                                "apiPath", backendApiProperties.getPath());
        }
}