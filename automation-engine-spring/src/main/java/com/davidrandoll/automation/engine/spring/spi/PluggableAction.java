package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.core.actions.IBaseAction;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.creator.AutomationProcessor;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

public abstract class PluggableAction<T extends IActionContext> implements TypedAction<T> {
    @Autowired
    private ITypeConverter typeConverter;

    @Autowired
    @Delegate
    protected AutomationProcessor processor;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ITypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypedAction<T> getSelf() {
        return (TypedAction<T>) applicationContext.getBean(this.getClass());
    }

    /**
     * Creates action instances from action definitions.
     * This is a convenience method for block actions that want to invoke child actions.
     *
     * @param ec      The event context
     * @param actions The action definitions to convert
     * @return A list of executable action instances
     */
    protected List<IBaseAction> createActions(EventContext ec, List<ActionDefinition> actions) {
        return processor.resolveActions(actions);
    }
}