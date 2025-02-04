package com.automation.engine.modules.action_building_block.wait_for_trigger;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.request.Trigger;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Component("waitForTriggerAction")
@RequiredArgsConstructor
public class WaitForTriggerAction extends AbstractAction<WaitForTriggerActionContext> {
    private final DefaultAutomationResolver resolver;
    private final List<WaitingAction> waitingActions = new CopyOnWriteArrayList<>();

    @Override
    public void execute(Event event, WaitForTriggerActionContext context) {
        if (ObjectUtils.isEmpty(context.getTriggers())) return;

        long timeout = Optional.ofNullable(context.getTimeout())
                .orElse(Duration.ofSeconds(60)).toMillis();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        waitingActions.add(new WaitingAction(context.getTriggers(), future));

        try {
            boolean triggerMet = future.get(timeout, TimeUnit.MILLISECONDS);
            if (triggerMet) {
                log.info("Trigger met, proceeding early!");
            } else {
                log.info("Timeout reached, proceeding...");
            }
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            log.info("Timeout or error occurred, proceeding...");
            Thread.currentThread().interrupt();
        } finally {
            waitingActions.removeIf(action -> action.future == future);
        }
    }

    @EventListener
    public void handleEvent(Event event) {
        for (WaitingAction action : waitingActions) {
            if (!action.future.isDone() && resolver.anyTriggersTriggered(event, action.triggers)) {
                action.future.complete(true);
            }
        }
    }

    private record WaitingAction(List<Trigger> triggers, CompletableFuture<Boolean> future) {
    }
}
