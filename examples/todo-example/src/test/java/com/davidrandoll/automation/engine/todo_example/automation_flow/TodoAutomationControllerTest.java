package com.davidrandoll.automation.engine.todo_example.automation_flow;

import com.davidrandoll.automation.engine.todo_example.db.repository.TodoAssigneeRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoItemRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class TodoAutomationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private TodoStatusRepository todoStatusRepository;

    @Autowired
    private TodoAssigneeRepository todoAssigneeRepository;

    @Test
    void testCreateTodo_withFlatFields() throws Exception {
        String requestBody = """
                    {
                      "title": "Write integration tests",
                      "status": "in-progress",
                      "assignee": "david"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Write integration tests"))
                .andExpect(jsonPath("$.status").value("in-progress"))
                .andExpect(jsonPath("$.assignee").value("david"));

        // Assert DB contains new TodoItem
        assertThat(todoItemRepository.findAll()).anyMatch(todo ->
                todo.getTitle().equals("Write integration tests") &&
                todo.getStatus().getCode().equals("in-progress") &&
                todo.getAssignee().getUsername().equals("david")
        );
    }

    @Test
    void testCreateTodo_withStatusAndAssigneeObject() throws Exception {
        String requestBody = """
                    {
                      "title": "Refactor service layer",
                      "status": "completed",
                      "assignee": "ishwari"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation/with-status-and-assignee-object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactor service layer"))
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.assignee").value("ishwari"));

        // Assert DB contains new TodoItem
        assertThat(todoItemRepository.findAll()).anyMatch(todo ->
                todo.getTitle().equals("Refactor service layer") &&
                todo.getStatus().getCode().equals("completed") &&
                todo.getAssignee().getUsername().equals("ishwari")
        );
    }

    @Test
    void testCreateTodo_missingStatusAndAssignee_shouldUseDefaults() throws Exception {
        String requestBody = """
                    {
                      "title": "Task with default values"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task with default values"))
                .andExpect(jsonPath("$.status").value("pending")) // Default code from context
                .andExpect(jsonPath("$.assignee").value((String) ""));
    }

    @Test
    void testCreateTodo_withFullAssigneeAndStatusObjects() throws Exception {
        String requestBody = """
                    {
                      "title": "Design database schema",
                      "status": "review",
                      "assignee": "ishwari"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation/with-status-and-assignee-object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Design database schema"))
                .andExpect(jsonPath("$.status").value("review"))
                .andExpect(jsonPath("$.assignee").value("ishwari"));
    }

    @Test
    void testCreateTodo_withNewStatusAndAssignee_shouldAutoCreate() throws Exception {
        String requestBody = """
                    {
                      "title": "Create new docs",
                      "status": "docs",
                      "assignee": "new_user"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation/with-status-and-assignee-object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Create new docs"))
                .andExpect(jsonPath("$.status").value("docs"))
                .andExpect(jsonPath("$.assignee").value("new_user"));

        var saved = todoItemRepository.findAll();
        assertThat(saved).anyMatch(todo ->
                todo.getTitle().equals("Create new docs") &&
                todo.getStatus().getCode().equals("docs") &&
                todo.getAssignee().getUsername().equals("new_user")
        );

        assertThat(todoStatusRepository.findByCode("docs")).isPresent();
        assertThat(todoAssigneeRepository.findByUsername("new_user")).isPresent();
    }

    @Test
    void testCreateTodo_missingTitle_shouldFailOrHandle() throws Exception {
        String requestBody = """
                    {
                      "status": "pending",
                      "assignee": "david"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("")); // Or expect error if enforced

        assertThat(todoItemRepository.findAll()).anyMatch(todo ->
                todo.getTitle() == null || todo.getTitle().isBlank()
        );
    }

    @Test
    void testCreateTodo_withExtraFields_shouldIgnoreThem() throws Exception {
        String requestBody = """
                    {
                      "title": "Cleanup",
                      "status": "done",
                      "assignee": "david",
                      "unexpectedField": "shouldBeIgnored"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Cleanup"))
                .andExpect(jsonPath("$.status").value("done"))
                .andExpect(jsonPath("$.assignee").value("david"));
    }


}