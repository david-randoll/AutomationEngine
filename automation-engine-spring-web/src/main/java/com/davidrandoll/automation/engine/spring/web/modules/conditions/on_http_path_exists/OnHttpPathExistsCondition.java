package com.davidrandoll.automation.engine.spring.web.modules.conditions.on_http_path_exists;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_path_exists.OnHttpPathExistsTrigger;
import com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_path_exists.OnHttpPathExistsTriggerContext;
import lombok.RequiredArgsConstructor;

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