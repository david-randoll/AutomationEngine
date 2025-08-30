package com.davidrandoll.automation.engine.modules.actions.wait_for_trigger;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import com.davidrandoll.automation.engine.AEConfigProvider;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
@FieldNameConstants
public class WaitForTriggerAction extends PluggableAction<WaitForTriggerActionContext> {
    private final List<WaitingAction> waitingActions = new CopyOnWriteArrayList<>();
    private final AEConfigProvider provider;

    @Override
    public void doExecute(EventContext ec, WaitForTriggerActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getTriggers())) return;

        long timeout = Optional.ofNullable(ac.getTimeout())
                .orElse(provider.getDefaultTimeout()).toMillis();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        waitingActions.add(new WaitingAction(ac.getTriggers(), future));

        ScheduledFuture<?> pollingTask = null;
        ScheduledExecutorService scheduler = provider != null ? provider.getScheduledExecutorService() : null;
        if (scheduler != null) {
            pollingTask = scheduler.scheduleAtFixedRate(() -> {
                log.debug("Polling for trigger...");
                if (!future.isDone() && processor.anyTriggersTriggered(ec, ac.getTriggers())) {
                    future.complete(true);
                    log.debug("Trigger met, completing future!");
                }
            }, 0, 1, TimeUnit.SECONDS);
        }

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
            if (pollingTask != null)
                pollingTask.cancel(true);
            waitingActions.removeIf(action -> action.future == future);
        }
    }

    @EventListener
    public void handleEvent(EventContext eventContext) {
        if (ObjectUtils.isEmpty(waitingActions)) return;
        log.debug("WaitForTriggerAction received event: {}", eventContext.getEvent());
        for (WaitingAction action : waitingActions) {
            if (!action.future.isDone() && processor.anyTriggersTriggered(eventContext, action.triggers)) {
                action.future.complete(true);
            }
        }
    }

    private record WaitingAction(List<TriggerDefinition> triggers, CompletableFuture<Boolean> future) {
    }
}
