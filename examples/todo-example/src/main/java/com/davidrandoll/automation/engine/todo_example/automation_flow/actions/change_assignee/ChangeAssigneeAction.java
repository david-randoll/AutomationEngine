package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.change_assignee;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoAssigneeRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoItemRepository;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoAssignee;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("changeAssigneeAction")
@RequiredArgsConstructor
public class ChangeAssigneeAction extends PluggableAction<ChangeAssigneeActionContext> {

    private final TodoItemRepository todoItemRepository;
    private final TodoAssigneeRepository todoAssigneeRepository;

    @Override
    public void doExecute(EventContext ec, ChangeAssigneeActionContext ac) {
        TodoItem todo = todoItemRepository.findById(ac.getTodoId())
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with ID: " + ac.getTodoId()));

        TodoAssignee assignee = todoAssigneeRepository.findByUsername(ac.getNewAssigneeUsername())
                .orElseGet(() -> todoAssigneeRepository.save(
                        new TodoAssignee(ac.getNewAssigneeUsername(), ac.getNewAssigneeFullName()))
                );

        todo.setAssignee(assignee);
        todoItemRepository.save(todo);

        ec.addMetadata(ac.getStoreToVariable(), todo);
    }
}
