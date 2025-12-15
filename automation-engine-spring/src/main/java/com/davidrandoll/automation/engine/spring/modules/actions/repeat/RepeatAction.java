package com.davidrandoll.automation.engine.spring.modules.actions.repeat;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class RepeatAction extends PluggableAction<RepeatActionContext> {

    @Override
    public void doExecute(EventContext ec, RepeatActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) return;
        for (int i = 0; i < ac.getCount(); i++) {
            processor.executeActions(ec, ac.getActions());
        }

        if (ac.hasWhileConditions()) {
            while (processor.allConditionsSatisfied(ec, ac.getWhileConditions())) {
                processor.executeActions(ec, ac.getActions());
            }
        }

        if (ac.hasUntilConditions()) {
            while (!processor.allConditionsSatisfied(ec, ac.getUntilConditions())) {
                processor.executeActions(ec, ac.getActions());
            }
        }

        if (ac.hasForEach()) {
            Iterable<?> items = convertToIterable(ac.getForEach());
            String variableName = ac.getAs();

            for (Object item : items) {
                ec.addMetadata(variableName, item);
                processor.executeActions(ec, ac.getActions());
                ec.removeMetadata(variableName);
            }
        }
    }

    /**
     * Converts various object types to an Iterable for forEach loops
     *
     * @param obj The object to convert (Collection, Array, or single item)
     * @return An iterable representation
     */
    private Iterable<?> convertToIterable(Object obj) {
        if (obj == null) return Collections.emptyList();
        if (obj instanceof Collection<?> colObj) return colObj;
        if (obj instanceof Iterable<?> itObj) return itObj;

        if (obj.getClass().isArray()) {
            if (obj instanceof Object[] arrObj)
                return Arrays.asList(arrObj);
            return convertPrimitiveArrayToList(obj);
        }
        return Collections.singletonList(obj);
    }

    /**
     * Converts primitive arrays to lists
     */
    private Iterable<?> convertPrimitiveArrayToList(Object array) {
        if (array instanceof int[] arr) {
            return Arrays.stream(arr).boxed().toList();
        } else if (array instanceof long[] arr) {
            return Arrays.stream(arr).boxed().toList();
        } else if (array instanceof double[] arr) {
            return Arrays.stream(arr).boxed().toList();
        } else if (array instanceof boolean[] arr) {
            return java.util.stream.IntStream.range(0, arr.length)
                    .mapToObj(i -> arr[i])
                    .toList();
        }
        return Collections.singletonList(array);
    }
}