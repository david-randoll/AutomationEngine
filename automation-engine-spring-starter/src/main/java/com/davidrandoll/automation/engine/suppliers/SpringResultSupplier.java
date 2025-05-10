package com.davidrandoll.automation.engine.suppliers;

import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.creator.result.IResultSupplier;
import com.davidrandoll.automation.engine.creator.result.ResultNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(value = IResultSupplier.class, ignored = SpringResultSupplier.class)
public class SpringResultSupplier implements IResultSupplier {
    private final ApplicationContext applicationContext;

    @Override
    public IResult getResult(String name) {
        try {
            var resultName = "%sResult".formatted(name);
            return applicationContext.getBean(resultName, IResult.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Bean {} not found", name, e);
            throw new ResultNotFoundException(name);
        }
    }
}