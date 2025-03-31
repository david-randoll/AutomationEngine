package com.automation.engine.core.variables.interceptors;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.variables.IBaseVariable;
import com.automation.engine.core.variables.IVariable;
import com.automation.engine.core.variables.VariableContext;
import org.springframework.core.OrderComparator;
import org.springframework.util.ObjectUtils;

import java.util.List;

public class InterceptingAction implements IVariable {
    private final IBaseVariable delegate;
    private final List<IVariableInterceptor> interceptors;

    public InterceptingAction(IBaseVariable delegate, List<IVariableInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = ObjectUtils.isEmpty(interceptors) ? List.of() : interceptors.stream()
                .sorted(OrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public void setVariable(Event event, VariableContext variableContext) {
        executeInterceptors(0, event, variableContext);
    }

    private void executeInterceptors(int index, Event event, VariableContext variableContext) {
        if (index < interceptors.size()) {
            IVariableInterceptor interceptor = interceptors.get(index);
            IVariable action = (ec, ac) -> this.executeInterceptors(index + 1, ec, ac);
            interceptor.intercept(event, variableContext, action);
        } else {
            // All interceptors have been processed, execute the delegate
            delegate.setVariable(event, variableContext);
        }
    }
}