package com.davidrandoll.automation.engine.tracing.utils;

import com.davidrandoll.automation.engine.tracing.TraceChildren;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class TraceUtils {
    /**
     * Filters out internal keys (starting with __) from context data for the
     * snapshot.
     */
    public static Map<String, Object> filterTraceData(Map<String, Object> data) {
        if (data == null) {
            return new HashMap<>();
        }
        Map<String, Object> filtered = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!entry.getKey().startsWith("__")) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    /**
     * Checks if the children container has any entries.
     */
    public static boolean hasAnyChildren(TraceChildren children) {
        if (children == null) {
            return false;
        }
        return !children.getVariables().isEmpty()
                || !children.getTriggers().isEmpty()
                || !children.getConditions().isEmpty()
                || !children.getActions().isEmpty()
                || children.getResult() != null;
    }
}
