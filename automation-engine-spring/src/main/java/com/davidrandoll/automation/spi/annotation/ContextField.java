package com.davidrandoll.automation.spi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides metadata for fields in context classes. This annotation is generic and not
 * tied to any specific use case. It can be used to provide hints for rendering,
 * documentation, or any other purpose.
 * <p>
 * Fields without this annotation will use default logic based on their type.
 * This annotation should only be used when customization is needed.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @ContextField(widget = Widget.TEXTAREA, placeholder = "Enter your message here")
 * private String message;
 * 
 * @ContextField(widget = Widget.DROPDOWN, dropdownOptions = {"option1", "option2", "option3"})
 * private String choice;
 * 
 * @ContextField(widget = Widget.MONACO_EDITOR, monacoLanguage = "javascript")
 * private String scriptCode;
 * }
 * </pre>
 *
 * @see Widget
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ContextField {

    /**
     * The widget type to use for rendering this field.
     * If not specified, the presentation layer will determine the widget based on field type.
     *
     * @return the widget type
     */
    Widget widget() default Widget.AUTO;

    /**
     * Placeholder text to display when the field is empty.
     *
     * @return the placeholder text
     */
    String placeholder() default "";

    /**
     * Help text or description to display alongside the field.
     *
     * @return the help text
     */
    String helpText() default "";

    /**
     * Options to display in a dropdown widget.
     * If the field is an enum and this is empty, enum values will be used automatically.
     *
     * @return array of dropdown options
     */
    String[] dropdownOptions() default {};

    /**
     * Display labels for dropdown options (parallel array to dropdownOptions).
     * If not specified, the option values themselves will be used as labels.
     *
     * @return array of display labels
     */
    String[] dropdownLabels() default {};

    /**
     * Language identifier for Monaco Editor (e.g., "java", "javascript", "yaml", "json").
     *
     * @return the language identifier
     */
    String monacoLanguage() default "text";

    /**
     * Additional Monaco Editor options as JSON string.
     * Example: {"minimap": {"enabled": false}, "lineNumbers": "on"}
     *
     * @return JSON string of Monaco options
     */
    String monacoOptions() default "";

    /**
     * Name of the custom component to use for rendering.
     * The component must be registered in the presentation layer's component registry.
     *
     * @return the custom component name
     */
    String customComponent() default "";

    /**
     * Display order hint. Lower numbers appear first.
     * Note: @JsonPropertyOrder takes precedence if present.
     *
     * @return the order value
     */
    int order() default 0;

    /**
     * Minimum value for numeric fields.
     *
     * @return the minimum value
     */
    double min() default Double.NEGATIVE_INFINITY;

    /**
     * Maximum value for numeric fields.
     *
     * @return the maximum value
     */
    double max() default Double.POSITIVE_INFINITY;

    /**
     * Whether the field should be read-only.
     *
     * @return true if read-only
     */
    boolean readOnly() default false;

    /**
     * Additional custom properties as JSON string for advanced use cases.
     * Example: {"colorPicker": true, "format": "hex"}
     *
     * @return JSON string of custom properties
     */
    String customProps() default "";

    /**
     * Widget types for field presentation.
     */
    enum Widget {
        /**
         * Automatically determine widget based on field type (default behavior).
         */
        AUTO,

        /**
         * Multi-line text input.
         */
        TEXTAREA,

        /**
         * Numeric input with optional min/max constraints.
         */
        NUMBER,

        /**
         * Checkbox for boolean values.
         */
        CHECKBOX,

        /**
         * Dropdown select with options.
         */
        DROPDOWN,

        /**
         * Code editor with syntax highlighting (e.g., Monaco Editor).
         */
        MONACO_EDITOR,

        /**
         * Custom component registered in the presentation layer.
         */
        CUSTOM,

        /**
         * Date picker.
         */
        DATE,

        /**
         * Time picker.
         */
        TIME,

        /**
         * Date and time picker.
         */
        DATETIME,

        /**
         * Color picker.
         */
        COLOR,

        /**
         * File upload.
         */
        FILE,

        /**
         * Slider for numeric values.
         */
        SLIDER,

        /**
         * Radio button group.
         */
        RADIO,

        /**
         * Switch/toggle for boolean values.
         */
        SWITCH
    }
}
