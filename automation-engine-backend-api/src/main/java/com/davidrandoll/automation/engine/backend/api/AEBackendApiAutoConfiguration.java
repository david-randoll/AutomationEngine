package com.davidrandoll.automation.engine.backend.api;

import com.davidrandoll.automation.engine.backend.api.controllers.AutomationEngineController;
import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        AEBackendApiConfig.class,
        JsonSchemaConfig.class,
        AutomationEngineController.class
})
public class AEBackendApiAutoConfiguration {
}