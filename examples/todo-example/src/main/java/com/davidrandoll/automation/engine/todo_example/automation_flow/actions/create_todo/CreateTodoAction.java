package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.create_todo;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoAssigneeRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoItemRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoStatusRepository;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoAssignee;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoItem;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("createTodoAction")
@RequiredArgsConstructor
public class CreateTodoAction extends PluggableAction<CreateTodoActionContext> {
    private final TodoItemRepository todoItemRepository;
    private final TodoStatusRepository todoStatusRepository;
    private final TodoAssigneeRepository todoAssigneeRepository;

    @Override
    public void doExecute(EventContext ec, CreateTodoActionContext ac) {
        var status = todoStatusRepository.findByCode(ac.getTodoStatus().getCode())
                .orElseGet(() -> todoStatusRepository.save(new TodoStatus(
                        ac.getTodoStatus().getCode(),
                        "Auto-created: " + ac.getTodoStatus().getDescription()
                )));

        TodoAssignee assignee = todoAssigneeRepository.findByUsername(ac.getTodoAssignee().getUsername())
                .orElseGet(() -> {
                    TodoAssignee newAssignee = new TodoAssignee(ac.getTodoAssignee().getUsername(), ac.getTodoAssignee().getFullName());
                    return todoAssigneeRepository.save(newAssignee);
                });

        var todo = todoItemRepository.save(new TodoItem(
                ac.getTitle(),
                status,
                assignee
        ));
        ec.addMetadata(ac.getStoreToVariable(), todo);
    }
}