package com.davidrandoll.automation.engine.core;

import com.davidrandoll.automation.engine.core.utils.StringUtils;

public interface IBlock {
    /**
     * Get the context type that this block operates on.
     *
     * @return The class type of the context, or null if it operates on any type.
     */
    default Class<?> getContextType() {
        return null;
    }

    /**
     * Indicates whether the block should automatically evaluate expressions.
     * This can be useful for blocks that need to process dynamic content.
     *
     * @return true if the block should auto-evaluate expressions, false otherwise.
     */
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

    /**
     * Provide example configurations or usage scenarios for the module.
     * This can help users understand how to effectively use the module.
     *
     * @return An object containing examples, or null if none are provided.
     */
    default Object getExamples() {
        return null;
    }
}