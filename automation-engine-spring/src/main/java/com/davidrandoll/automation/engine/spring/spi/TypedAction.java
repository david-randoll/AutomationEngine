package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

public interface TypedAction<T extends IActionContext> extends IAction {
    ITypeConverter getTypeConverter();

    TypedAction<T> getSelf();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default ActionResult execute(EventContext eventContext, ActionContext actionContext) {
        T data = getTypeConverter().convert(actionContext, this.getContextType());
        if (data == null)
            throw new IllegalArgumentException("Cannot convert action context data to " + this.getContextType());

        TypedAction<T> self = getSelfOrElseThrow();
        if (self.canExecute(eventContext, data)) {
            return self.executeWithResult(eventContext, data);
        }
        return ActionResult.CONTINUE;
    }

    default boolean canExecute(EventContext ec, T ac) {
        return true;
    }

    default ActionResult executeWithResult(EventContext ec, T ac) {
        TypedAction<T> self = getSelfOrElseThrow();
        self.doExecute(ec, ac);
        return ActionResult.CONTINUE;
    }

    void doExecute(EventContext ec, T ac);

    @Override
    default List<T> getExamples() {
        var example = getTypeConverter().convert(Map.of(), getContextType());
        return List.of((T) example);
    }

    @NonNull
    private TypedAction<T> getSelfOrElseThrow() {
        // Calling the proxied self to ensure AOP aspects are applied such as transactions, logging, etc.
        var self = getSelf();
        if (self == null)
            throw new IllegalStateException("Self reference is not initialized");
        return self;
    }
}