package com.davidrandoll.automation.engine.spring.spi;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.result.IResultContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.core.utils.GenericTypeResolver;

import java.util.List;
import java.util.Map;

public interface TypedResult<T extends IResultContext> extends IResult {
    ITypeConverter getTypeConverter();

    @Override
    default Class<?> getContextType() {
        return GenericTypeResolver.getGenericParameterClass(getClass());
    }

    @Override
    default Object getExecutionSummary(EventContext context, ResultContext resultContext) {
        T data = getTypeConverter().convert(resultContext.getData(), getContextType());
        return getExecutionSummary(context, data);
    }

    Object getExecutionSummary(EventContext ec, T cc);

    @Override
    default List<T> getExamples() {
        var contextType = getContextType();
        var example = getTypeConverter().convert(Map.of(), contextType);
        return List.of((T) example);
    }
}
