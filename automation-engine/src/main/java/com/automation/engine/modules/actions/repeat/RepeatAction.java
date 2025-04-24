package com.automation.engine.modules.actions.repeat;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("repeatAction")
@RequiredArgsConstructor
public class RepeatAction extends PluggableAction<RepeatActionContext> {

    @Override
    public void execute(EventContext ec, RepeatActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) return;
        for (int i = 0; i < ac.getCount(); i++) {
            processor.executeActions(ec, ac.getActions());
        }

        if (ac.hasWhileConditions()) {
            while (processor.allConditionsSatisfied(ec, ac.getWhileConditions())) {
                processor.executeActions(ec, ac.getActions());
            }
        }

        if (ac.hasUntilConditions()) {
            while (!processor.allConditionsSatisfied(ec, ac.getUntilConditions())) {
                processor.executeActions(ec, ac.getActions());
            }
        }

        if (ac.hasForEach()) {
            for (Object item : ac.getForEach()) {
                ec.addMetadata("item", item);
                processor.executeActions(ec, ac.getActions());
                ec.removeMetadata("item");
            }
        }
    }
}