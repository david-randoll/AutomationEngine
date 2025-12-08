package com.davidrandoll.automation.engine.ui;

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

    @GetMapping(
            value = {
                    "/app-config.js",
                    "/*/app-config.js",
                    "/*/*/app-config.js",
                    "/*/*/*/app-config.js"
            }, produces = "application/javascript"
    )
    public String getAppConfig() {
        String contextPath = Optional.ofNullable(serverProperties.getServlet().getContextPath()).orElse("");
        return "window.__APP_CONFIG__ = { contextPath: '%s' };".formatted(contextPath);
    }

    @GetMapping(
            value = {
                    "/app-config.json",
                    "/*/app-config.json",
                    "/*/*/app-config.json",
                    "/*/*/*/app-config.json"
            }, produces = "application/json"
    )
    public Map<String, String> getAppConfigJson() {
        String contextPath = Optional.ofNullable(serverProperties.getServlet().getContextPath()).orElse("");
        return Map.of(
                "contextPath", contextPath
        );
    }
}