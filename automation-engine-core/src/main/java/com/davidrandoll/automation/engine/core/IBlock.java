package com.davidrandoll.automation.engine.core;

import com.davidrandoll.automation.engine.core.utils.StringUtils;

public interface IBlock {
    default Class<?> getContextType() {
        return null;
    }

    default boolean autoEvaluateExpression() {
        return true;
    }

    /**
     * Provide a user-friendly label for the module.
     * This label will be used in the UI to represent the module.
     *
     * @return A string label for the module.
     */
    default String getModuleLabel() {
        return StringUtils.toUserFriendlyName(this.getClass());
    }

    /**
     * Provide a brief description of the module's functionality.
     * This decription will be used in the UI to help users understand what the module does.
     *
     * @return A string description of the module.
     */
    default String getModuleDescription() {
        return "No description provided.";
    }
}