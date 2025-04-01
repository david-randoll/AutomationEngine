package com.automation.engine.modules.actions.parallel;

import com.automation.engine.AutomationEngineConfigProvider;
import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("parallelAction")
@RequiredArgsConstructor
public class ParallelAction extends AbstractAction<ParallelActionContext> {
    private final DefaultAutomationResolver resolver;

    @Autowired(required = false)
    private AutomationEngineConfigProvider provider;

    @Override
    public void execute(Event event, ParallelActionContext context) {
        if (ObjectUtils.isEmpty(context.getActions())) return;

        var executor = provider != null ? provider.getExecutor() : null;
        if (executor != null) {
            log.debug("Executor provider found, using provided executor");
            resolver.executeActionsAsync(event, context.getActions(), executor);
        } else {
            log.debug("No executor provider found, using default executor");
            resolver.executeActionsAsync(event, context.getActions());
        }
    }
}