package com.automation.engine.templating;

import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.automation.engine.core.events.Event;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
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
     * @param event   the event context providing data for template replacement
     * @param context the action context containing data to process
     * @param action  the action to be executed after processing
     */

    @Override
    @SneakyThrows
    public void intercept(Event event, ActionContext context, IAction action) {
        log.debug("ActionTemplatingInterceptor: Processing action data...");
        if (ObjectUtils.isEmpty(context.getData())) return;
        if (ObjectUtils.isEmpty(event.getData())) return;

        processOnlyStringFields(event, context, action);
        log.debug("ActionTemplatingInterceptor done.");

        //processEntireContextAsString(event, context, action);
    }

    private void processOnlyStringFields(Event event, ActionContext context, IAction action) throws IOException {
        var mapCopy = new HashMap<>(context.getData());
        for (Map.Entry<String, Object> entry : mapCopy.entrySet()) {
            if (entry.getValue() instanceof String valueStr) {
                String processedValue = templateProcessor.process(valueStr, event.getData());
                entry.setValue(processedValue);
            }
        }
        action.execute(event, new ActionContext(mapCopy));
    }

    private void processEntireContextAsString(Event event, ActionContext context, IAction action) {
        Map<String, Object> processedDataMap = null;
        try {
            String actionDataAsJson = objectMapper.writeValueAsString(context.getData());
            var processedTemplate = templateProcessor.process(actionDataAsJson, event.getData());
            // convert the result back to a map
            processedDataMap = objectMapper.readValue(processedTemplate, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        action.execute(event, new ActionContext(processedDataMap));
    }
}