package com.automation.engine.engine.actions.interceptors;

import com.automation.engine.engine.actions.ActionContext;
import com.automation.engine.engine.actions.IAction;
import com.automation.engine.engine.events.EventContext;
import com.automation.engine.templating.TemplateProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplatingActionInterceptor implements IActionInterceptor {
    private final TemplateProcessor templateProcessor;
    private final ObjectMapper objectMapper;

    @Override
    public void intercept(EventContext context, ActionContext actionContext, IAction action) {
        try {
            var jsonStr = objectMapper.writeValueAsString(actionContext.getData());
            var result = templateProcessor.process(jsonStr, context.getData());
            // convert the result back to a map
            var resultData = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {
            });
            actionContext.setData(resultData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            action.execute(context, actionContext);
        }
    }
}