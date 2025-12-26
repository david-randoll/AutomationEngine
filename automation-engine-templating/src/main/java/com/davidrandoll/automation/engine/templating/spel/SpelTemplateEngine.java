package com.davidrandoll.automation.engine.templating.spel;

import com.davidrandoll.automation.engine.templating.ITemplateEngine;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * Implementation of {@link ITemplateEngine} using Spring Expression Language (SpEL).
 * <p>
 * This implementation preserves the native types of evaluated expressions (arrays, maps, primitives, etc.)
 * instead of converting them to strings, which is important for passing complex objects between automation
 * components.
 * <p>
 * When the template is a pure expression (e.g., "#{someVar}"), it returns the native type.
 * When the template contains mixed literal text and expressions (e.g., "Hello #{name}!"), it returns a String.
 */
public class SpelTemplateEngine implements ITemplateEngine {
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public Object process(String templateString, Map<String, Object> variables) {
        StandardEvaluationContext context = new StandardEvaluationContext(variables);
        context.addPropertyAccessor(new MapAccessor());
        
        // Check if this is a pure expression (starts with "#{" and ends with "}")
        // If so, parse it as a direct expression to preserve native types
        if (isPureExpression(templateString)) {
            // Remove the "#{" prefix and "}" suffix and parse as a direct expression
            String expression = templateString.substring(2, templateString.length() - 1);
            Expression exp = parser.parseExpression(expression);
            return exp.getValue(context, Object.class);
        }
        
        // Otherwise, parse as a template expression (which converts to String)
        Expression expression = parser.parseExpression(templateString, ParserContext.TEMPLATE_EXPRESSION);
        return expression.getValue(context, Object.class);
    }
    
    /**
     * Checks if the template string is a pure expression (no literal text parts).
     * A pure expression is one that starts with "#{" and ends with "}" with no text outside.
     * 
     * @param templateString the template string to check
     * @return true if the string is a pure expression, false otherwise
     */
    private boolean isPureExpression(String templateString) {
        if (templateString == null) {
            return false;
        }
        String trimmed = templateString.trim();
        return trimmed.startsWith("#{") && trimmed.endsWith("}") && trimmed.indexOf("#{") == trimmed.lastIndexOf("#{");
    }
}