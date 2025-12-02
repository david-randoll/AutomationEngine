package com.davidrandoll.automation.engine.spring.modules.actions.uda;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserDefinedAction extends PluggableAction<UserDefinedActionContext> {
    @Override
    public void doExecute(EventContext ec, UserDefinedActionContext ac) {

    }
}