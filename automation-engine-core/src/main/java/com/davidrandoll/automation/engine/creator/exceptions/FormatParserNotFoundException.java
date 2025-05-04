package com.davidrandoll.automation.engine.creator.exceptions;

public class FormatParserNotFoundException extends RuntimeException {
    public FormatParserNotFoundException(String format) {
        super("Format parser not found for format: %s".formatted(format));
    }
}
