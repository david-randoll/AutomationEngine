package com.automation.engine.modules.action_building_block.parallel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.Executor;

@Data
@AllArgsConstructor
public class ExecutorProvider {
    private Executor executor;
}