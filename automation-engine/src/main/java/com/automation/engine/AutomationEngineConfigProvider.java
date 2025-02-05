package com.automation.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutomationEngineConfigProvider {
    private Executor executor;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
}