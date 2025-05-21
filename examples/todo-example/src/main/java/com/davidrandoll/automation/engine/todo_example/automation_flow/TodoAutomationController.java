package com.davidrandoll.automation.engine.todo_example.automation_flow;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos/automation")
@RequiredArgsConstructor
public class TodoAutomationController {
    private final AutomationEngine engine;

    @PostMapping
    public Object createTodo(@RequestBody JsonNode body) {
        IEvent event = engine.getEventFactory().createEvent(body);
        String yaml = """
                alias: create-todo-automation
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: createTodo
                    title: "{{ title }}"
                    status: "{{ status }}"
                    assignee: "{{ assignee }}"
                    storeToVariable: todo
                result:
                  id: "{{todo.id}}"
                  title: "{{ todo.title }}"
                  status: "{{ todo.status.code }}"
                  assignee: "{{ todo.assignee.username }}"
                """;
        return engine.executeAutomationWithYaml(yaml, event)
                .orElse(null);
    }

    @PostMapping("with-status-and-assignee-object")
    public Object createTodoWithStatusAndAssigneeObject(@RequestBody JsonNode body) {
        IEvent event = engine.getEventFactory().createEvent(body);
        String yaml = """
                alias: create-todo-automation
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: createTodo
                    title: "{{ title }}"
                    status:
                      code: "{{ status }}"
                    assignee:
                      username: "{{ assignee }}"
                    storeToVariable: todo
                result:
                  id: "{{todo.id}}"
                  title: "{{ todo.title }}"
                  status: "{{ todo.status.code }}"
                  assignee: "{{ todo.assignee.username }}"
                """;
        return engine.executeAutomationWithYaml(yaml, event)
                .orElse(null);
    }

    @PostMapping("/change-status")
    public Object changeStatus(@RequestBody JsonNode body) {
        IEvent event = engine.getEventFactory().createEvent(body);

        String yaml = """
                alias: change-todo-status
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: changeStatus
                    todoId: "{{ todoId }}"
                    newStatusCode: "{{ newStatusCode }}"
                result:
                  id: "{{ todo.id }}"
                  title: "{{ todo.title }}"
                  status: "{{ todo.status.code }}"
                  assignee: "{{ todo.assignee.username }}"
                """;

        return engine.executeAutomationWithYaml(yaml, event)
                .orElse(null);
    }

    @PostMapping("/create-and-change-status")
    public Object createAndChangeStatus(@RequestBody JsonNode body) {
        IEvent event = engine.getEventFactory().createEvent(body);

        String yaml = """
                alias: create-and-change-status
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: createTodo
                    title: "{{ title }}"
                    status: "{{ status }}"
                    assignee: "{{ assignee }}"
                    storeToVariable: "todo"
                
                  - action: variable
                    createdStatus: "{{ todo.status.code }}"
                
                  - action: changeStatus
                    todoId: "{{ todo.id }}"
                    newStatusCode: "{{ newStatusCode }}"
                    storeToVariable: changedTodo
                result:
                  id: "{{ todo.id }}"
                  title: "{{ todo.title }}"
                  createdStatus: "{{ createdStatus }}"
                  changedStatus: "{{ changedTodo.status.code }}"
                  assignee: "{{ todo.assignee.username }}"
                """;

        return engine.executeAutomationWithYaml(yaml, event)
                .orElse(null);
    }

    @PostMapping("/change-assignee")
    public Object changeAssignee(@RequestBody JsonNode body) {
        IEvent event = engine.getEventFactory().createEvent(body);

        String yaml = """
                alias: change-assignee-automation
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: changeAssignee
                    todoId: "{{ todoId }}"
                    newAssigneeUsername: "{{ username }}"
                    newAssigneeFullName: "{{ fullName }}"
                    storeToVariable: updatedTodo
                result:
                  id: "{{ updatedTodo.id }}"
                  title: "{{ updatedTodo.title }}"
                  assignee: "{{ updatedTodo.assignee.username }}"
                """;

        return engine.executeAutomationWithYaml(yaml, event)
                .orElse(null);
    }

    @PostMapping("/create-change-status-assign")
    public Object createChangeStatusAndAssign(@RequestBody JsonNode body) {
        IEvent event = engine.getEventFactory().createEvent(body);

        String yaml = """
                alias: create-change-status-assign
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: createTodo
                    title: "{{ title }}"
                    status: "{{ status }}"
                    assignee: "{{ initialAssignee }}"
                    storeToVariable: todo
                
                  - action: changeStatus
                    todoId: "{{ todo.id }}"
                    newStatusCode: "{{ newStatus }}"
                    storeToVariable: updatedTodo
                
                  - action: changeAssignee
                    todoId: "{{ updatedTodo.id }}"
                    newAssigneeUsername: "{{ newAssignee.username }}"
                    newAssigneeFullName: "{{ newAssignee.fullName }}"
                    storeToVariable: finalTodo
                
                result:
                  id: "{{ finalTodo.id }}"
                  title: "{{ finalTodo.title }}"
                  status: "{{ finalTodo.status.code }}"
                  assignee: "{{ finalTodo.assignee.username }}"
                """;

        return engine.executeAutomationWithYaml(yaml, event)
                .orElse(null);
    }


}