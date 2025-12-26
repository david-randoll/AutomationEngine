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