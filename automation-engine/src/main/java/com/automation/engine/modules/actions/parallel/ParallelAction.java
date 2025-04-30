package com.automation.engine.modules.actions.parallel;

import com.automation.engine.AEConfigProvider;
import com.automation.engine.conditional.AEConditionalOnMissingBeanName;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("parallelAction")
@RequiredArgsConstructor
@AEConditionalOnMissingBeanName
public class ParallelAction extends PluggableAction<ParallelActionContext> {

    @Autowired(required = false)
    private AEConfigProvider provider;

    @Override
    public void execute(EventContext ec, ParallelActionContext ac) {
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