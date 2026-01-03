package com.davidrandoll.automation.engine.spring.modules.actions.delay;

import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.orchestrator.IAEOrchestrator;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DelayAction extends PluggableAction<DelayActionContext> {
    private final TaskScheduler taskScheduler;
    private final IAEOrchestrator orchestrator;

    @Override
    public ActionResult executeWithResult(EventContext ec, DelayActionContext ac) {
        Duration duration = ac.getDuration();
        UUID executionId = ec.getExecutionId();

        if (duration == null || duration.isZero() || duration.isNegative()) {
            return ActionResult.continueExecution();
        }

        log.info("Pausing execution {} for {}", executionId, duration);

        taskScheduler.schedule(() -> {
            log.info("Resuming execution {}", executionId);
            orchestrator.resumeAutomation(executionId);
        }, Instant.now().plus(duration));

        return ActionResult.pauseExecution();
    }

    @Override
    public void doExecute(EventContext ec, DelayActionContext ac) {
        // No-op, using executeWithResult instead
    }
}