package com.automation.engine.templating;

import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.events.EventContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionTemplatingInterceptor implements IActionInterceptor {
    private final TemplateProcessor templateProcessor;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts an action to process and apply templated data.
     * <p>
     * Assumes that all strings in the action context are templated.
     * For example, the action context map could contain a key-value pair like:
     * "message": "Hello {{name}}". The templateProcessor will replace
     * the placeholder {{name}} with the corresponding value from the event context.
     * If a string does not contain a template, it remains unchanged.
     *
     * @param context       the event context providing data for template replacement
     * @param actionContext the action context containing data to process
     * @param action        the action to be executed after processing
     */

    @Override
    public void intercept(EventContext context, ActionContext actionContext, IAction action) {
        try {
            log.debug("ActionTemplatingInterceptor: Processing action data: {}", actionContext.getData());
            var actionDataAsJson = objectMapper.writeValueAsString(actionContext.getData());
            var processedTemplate = templateProcessor.process(actionDataAsJson, context.getData());
            // convert the result back to a map
            Map<String, Object> processedDataMap = objectMapper.readValue(processedTemplate, new TypeReference<>() {
            });
            actionContext.setData(processedDataMap);
            log.debug("ActionTemplatingInterceptor done.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            action.execute(context, actionContext);
        }
    }
}