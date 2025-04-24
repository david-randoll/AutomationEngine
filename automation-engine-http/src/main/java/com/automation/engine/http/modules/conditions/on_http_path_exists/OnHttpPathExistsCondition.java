package com.automation.engine.http.modules.conditions.on_http_path_exists;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.modules.triggers.on_http_path_exists.OnHttpPathExistsTrigger;
import com.automation.engine.http.modules.triggers.on_http_path_exists.OnHttpPathExistsTriggerContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpPathExistsCondition")
@RequiredArgsConstructor
public class OnHttpPathExistsCondition extends PluggableCondition<OnHttpPathExistsConditionContext> {
    private final OnHttpPathExistsTrigger trigger;

    @Override
    public boolean isSatisfied(EventContext ec, OnHttpPathExistsConditionContext cc) {
        OnHttpPathExistsTriggerContext tc = OnHttpPathExistsTriggerContext.builder()
                .alias(cc.getAlias())
                .paths(cc.getPaths())
                .methods(cc.getMethods())
                .build();

        return trigger.isTriggered(ec, tc);
    }
}