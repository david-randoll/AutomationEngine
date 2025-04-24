package com.automation.engine.modules.actions.wait_for_trigger;

import com.automation.engine.AutomationEngineConfigProvider;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.creator.triggers.Trigger;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Component("waitForTriggerAction")
@RequiredArgsConstructor
@FieldNameConstants
public class WaitForTriggerAction extends PluggableAction<WaitForTriggerActionContext> {
    private final List<WaitingAction> waitingActions = new CopyOnWriteArrayList<>();

    @Autowired(required = false)
    private AutomationEngineConfigProvider provider;

    @Override
    public void execute(EventContext ec, WaitForTriggerActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getTriggers())) return;

        long timeout = Optional.ofNullable(ac.getTimeout())
                .orElse(provider.getDefaultTimeout()).toMillis();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        waitingActions.add(new WaitingAction(ac.getTriggers(), future));

        ScheduledFuture<?> pollingTask = null;
        ScheduledExecutorService scheduler = provider != null ? provider.getScheduledExecutorService() : null;
        if (scheduler != null) {
            pollingTask = scheduler.scheduleAtFixedRate(() -> {
                if (!future.isDone() && processor.anyTriggersTriggered(ec, ac.getTriggers())) {
                    future.complete(true);
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
        for (WaitingAction action : waitingActions) {
            if (!action.future.isDone() && processor.anyTriggersTriggered(eventContext, action.triggers)) {
                action.future.complete(true);
            }
        }
    }

    private record WaitingAction(List<Trigger> triggers, CompletableFuture<Boolean> future) {
    }
}
