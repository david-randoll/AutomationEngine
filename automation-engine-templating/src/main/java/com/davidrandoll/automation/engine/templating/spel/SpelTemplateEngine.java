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
        Expression expression = parser.parseExpression(templateString, ParserContext.TEMPLATE_EXPRESSION);
        return expression.getValue(context, Object.class);
    }
}