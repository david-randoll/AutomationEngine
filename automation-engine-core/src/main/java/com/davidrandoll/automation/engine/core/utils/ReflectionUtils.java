package com.davidrandoll.automation.engine.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@UtilityClass
public class ReflectionUtils {
    /**
     * This method builds a map from an object's fields.
     * It uses reflection to get all fields of the class and their values.
     * The field names are used as keys in the map.
     *
     * @return a map containing the event data
     */
    public Map<String, Object> buildMapFromObject(Object obj) {
        var result = new HashMap<String, Object>();
        var fields = FieldUtils.getAllFields(obj.getClass());
        for (var field : fields) {
            try {
                result.put(field.getName(), FieldUtils.readField(field, obj, true));
            } catch (IllegalAccessException e) {
                log.error("Error reading field {} from object {}", field.getName(), obj.getClass().getSimpleName(), e);
            }
        }
        return result;
    }
}
