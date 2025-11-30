package com.davidrandoll.automation.engine.ui;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        AutomationEngineUiConfiguration.class
})
public class AutomationEngineUiAutoConfiguration {
}

