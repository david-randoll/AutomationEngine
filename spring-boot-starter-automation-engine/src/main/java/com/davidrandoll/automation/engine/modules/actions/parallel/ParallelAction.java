package com.davidrandoll.automation.engine.modules.actions.parallel;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.AEConfigProvider;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public class ParallelAction extends PluggableAction<ParallelActionContext> {
    private final AEConfigProvider provider;

    @Override
    public void doExecute(EventContext ec, ParallelActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) return;

        var executor = provider != null ? provider.getExecutor() : null;
        if (executor != null) {
            log.debug("Executor provider found, using provided executor");
            processor.executeActionsAsync(ec, ac.getActions(), executor);
        } else {
            log.debug("No executor provider found, using default executor");
            processor.executeActionsAsync(ec, ac.getActions());
        }
    }
}