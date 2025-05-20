package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.change_status;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoItemRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoStatusRepository;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoItem;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("changeStatusAction")
@RequiredArgsConstructor
public class ChangeStatusAction extends PluggableAction<ChangeStatusActionContext> {
    private final TodoItemRepository todoItemRepository;
    private final TodoStatusRepository todoStatusRepository;

    @Override
    public void doExecute(EventContext ec, ChangeStatusActionContext ac) {
        TodoItem todo = todoItemRepository.findById(ac.getTodoId())
                .orElseThrow(() -> new IllegalArgumentException("TodoItem not found with id: " + ac.getTodoId()));

        TodoStatus newStatus = todoStatusRepository.findByCode(ac.getNewStatusCode())
                .orElseGet(() -> todoStatusRepository.save(new TodoStatus(
                        ac.getNewStatusCode(),
                        "Auto-created: " + ac.getNewStatusCode()
                )));

        todo.setStatus(newStatus);
        todoItemRepository.save(todo);

        ec.addMetadata(ac.getStoreToVariable(), todo);
    }
}
