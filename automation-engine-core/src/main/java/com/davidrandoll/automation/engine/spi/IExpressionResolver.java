package com.davidrandoll.automation.engine.spi;

import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.Map;

public interface IExpressionResolver {
    Map<String, Object> resolve(EventContext eventContext, Map<String, Object> context);
}