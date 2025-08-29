package com.davidrandoll.automation.engine.core.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
    public static String toUserFriendlyName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        String spaced = simpleName.replaceAll("([a-z])([A-Z])", "$1 $2");
        return org.apache.commons.lang3.StringUtils.capitalize(spaced);
    }
}