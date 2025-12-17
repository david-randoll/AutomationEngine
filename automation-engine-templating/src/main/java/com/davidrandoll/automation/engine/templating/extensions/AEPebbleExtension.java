package com.davidrandoll.automation.engine.templating.extensions;

import io.pebbletemplates.pebble.attributes.AttributeResolver;
import io.pebbletemplates.pebble.extension.*;
import io.pebbletemplates.pebble.operator.BinaryOperator;
import io.pebbletemplates.pebble.operator.UnaryOperator;
import io.pebbletemplates.pebble.tokenParser.TokenParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class AEPebbleExtension extends AbstractExtension {
    private final List<TokenParser> tokenParsers;
    private final List<BinaryOperator> binaryOperators;
    private final List<UnaryOperator> unaryOperators;
    private final Map<String, Filter> filters;
    private final Map<String, Test> tests;
    private final Map<String, Function> functions;
    private final List<NodeVisitorFactory> nodeVisitorFactories;
    private final List<AttributeResolver> attributeResolvers;
}