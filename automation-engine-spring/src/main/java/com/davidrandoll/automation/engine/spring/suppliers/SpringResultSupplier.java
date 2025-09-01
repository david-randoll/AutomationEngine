package com.davidrandoll.automation.engine.spring.suppliers;

import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.creator.result.IResultSupplier;
import com.davidrandoll.automation.engine.creator.result.ResultNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor
public class SpringResultSupplier implements IResultSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public IResult getResult(String name) {
        try {
            if (name.endsWith("Result")) {
                return applicationContext.getBean(name, IResult.class);
            }
            var resultName = "%sResult".formatted(name);
            return applicationContext.getBean(resultName, IResult.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new ResultNotFoundException(name);
        }
    }
}