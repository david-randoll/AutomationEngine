package com.davidrandoll.automation.engine.actions;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Test action to verify that SpEL returns objects (arrays, lists, maps) instead of strings.
 * This is only used in tests to validate templating behavior.
 */
@Component("objectTypeTestAction")
@Slf4j
public class ObjectTypeTestAction extends PluggableAction<ObjectTypeTestAction.ObjectTypeTestActionContext> {

    // Static field to allow tests to access the last executed context
    public static ObjectTypeTestActionContext lastContext;

    @Override
    public void doExecute(EventContext ec, ObjectTypeTestActionContext ac) {
        // Store context for test access
        lastContext = ac;
        
        Object testValue = ac.getTestValue();
        
        // Log the actual type received
        log.info("Received type: {}", testValue != null ? testValue.getClass().getName() : "null");
        
        if (testValue instanceof List) {
            List<?> list = (List<?>) testValue;
            log.info("Received List with {} elements: {}", list.size(), list);
            ac.setReceivedType("List");
            ac.setListSize(list.size());
        } else if (testValue instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) testValue;
            log.info("Received Map with {} entries: {}", map.size(), map);
            ac.setReceivedType("Map");
            ac.setMapSize(map.size());
        } else if (testValue instanceof String) {
            log.info("Received String: {}", testValue);
            ac.setReceivedType("String");
        } else if (testValue != null) {
            log.info("Received other type: {} - {}", testValue.getClass().getSimpleName(), testValue);
            ac.setReceivedType(testValue.getClass().getSimpleName());
        } else {
            log.info("Received null");
            ac.setReceivedType("null");
        }
    }

    @Data
    public static class ObjectTypeTestActionContext implements IActionContext {
        private String alias;
        private String description;
        
        // Input: the value to test (can be any type when using SpEL)
        private Object testValue;
        
        // Outputs: set by the action to verify what was received
        private String receivedType;
        private Integer listSize;
        private Integer mapSize;
    }
}
