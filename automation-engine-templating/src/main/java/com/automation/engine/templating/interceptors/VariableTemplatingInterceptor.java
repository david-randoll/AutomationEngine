package com.automation.engine.templating.interceptors;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.variables.IVariable;
import com.automation.engine.core.variables.VariableContext;
import com.automation.engine.core.variables.interceptors.IVariableInterceptor;
import com.automation.engine.templating.TemplateProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VariableTemplatingInterceptor implements IVariableInterceptor {
    private final TemplateProcessor templateProcessor;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts a variable to process and apply templated data.
     * <p>
     * Assumes that all strings in the action context are templated.
     * For example, the context map could contain a key-value pair like:
     * "message": "Hello {{name}}". The templateProcessor will replace
     * the placeholder {{name}} with the corresponding value from the event context.
     * If a string does not contain a template, it remains unchanged.
     *
     * @param eventContext    the event context providing data for template replacement
     * @param variableContext  the variable context containing data to process
     * @param variable the variable to set after processing
     */

    @Override
    @SneakyThrows
    public void intercept(EventContext eventContext, VariableContext variableContext, IVariable variable) {
        log.debug("VariableTemplatingInterceptor: Processing variable data...");
        if (ObjectUtils.isEmpty(variableContext.getData()) || ObjectUtils.isEmpty(eventContext.getEventData())) {
            variable.resolve(eventContext, variableContext);
        }

        var mapCopy = new HashMap<>(variableContext.getData());
        for (Map.Entry<String, Object> entry : mapCopy.entrySet()) {
            if (entry.getValue() instanceof String valueStr) {
                String processedValue = templateProcessor.process(valueStr, eventContext.getEventData());
                entry.setValue(processedValue);
            }
        }
        variable.resolve(eventContext, new VariableContext(mapCopy));
        log.debug("VariableTemplatingInterceptor done.");
    }
}