package com.davidrandoll.automation.engine.todo_example.automation_flow;

import com.davidrandoll.automation.engine.todo_example.db.repository.TodoAssigneeRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoItemRepository;
import com.davidrandoll.automation.engine.todo_example.db.repository.TodoStatusRepository;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoAssignee;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoItem;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
                      "assignee": "david"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation/with-status-and-assignee-object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactor service layer"))
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.assignee").value("david"));

        // Assert DB contains new TodoItem
        assertThat(todoItemRepository.findAll()).anyMatch(todo ->
                todo.getTitle().equals("Refactor service layer") &&
                todo.getStatus().getCode().equals("completed") &&
                todo.getAssignee().getUsername().equals("david")
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
                      "assignee": "david"
                    }
                """;

        mockMvc.perform(post("/api/todos/automation/with-status-and-assignee-object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Design database schema"))
                .andExpect(jsonPath("$.status").value("review"))
                .andExpect(jsonPath("$.assignee").value("david"));
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

    @Test
    void createAndChangeStatus_shouldCreateAndUpdateTodo() throws Exception {
        String requestBody = """
                {
                  "title": "Fix flaky test",
                  "status": "pending",
                  "assignee": "david",
                  "newStatusCode": "completed"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/todos/automation/create-and-change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Fix flaky test"))
                .andExpect(jsonPath("$.createdStatus").value("pending"))
                .andExpect(jsonPath("$.changedStatus").value("completed"))
                .andExpect(jsonPath("$.assignee").value("david"))
                .andReturn();

        // Extract ID from response
        String responseJson = result.getResponse().getContentAsString();
        JsonNode json = new ObjectMapper().readTree(responseJson);
        Long todoId = json.get("id").asLong();

        // Verify in DB
        TodoItem todo = todoItemRepository.findById(todoId).orElseThrow();
        assertThat(todo.getTitle()).isEqualTo("Fix flaky test");
        assertThat(todo.getStatus().getCode()).isEqualTo("completed");
        assertThat(todo.getAssignee().getUsername()).isEqualTo("david");
    }

    @Test
    void changeAssignee_shouldUpdateTodoAssignee() throws Exception {
        // First, create then assigned to someone else
        TodoStatus status = todoStatusRepository.save(new TodoStatus("in-progress", "In Progress"));
        TodoAssignee original = todoAssigneeRepository.save(new TodoAssignee("john", "John Doe"));
        TodoItem todo = todoItemRepository.save(new TodoItem("Write tests", status, original));

        String requestBody = String.format("""
                {
                  "todoId": %d,
                  "username": "ishwari",
                  "fullName": "Ishwari Misir"
                }
                """, todo.getId());

        mockMvc.perform(post("/api/todos/automation/change-assignee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todo.getId()))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.assignee").value("ishwari"));

        TodoItem updated = todoItemRepository.findById(todo.getId()).orElseThrow();
        assertThat(updated.getAssignee().getUsername()).isEqualTo("ishwari");
        assertThat(updated.getAssignee().getFullName()).isEqualTo("Ishwari Misir");
    }

    @Test
    void testCreateChangeStatusAndAssign() throws Exception {
        String requestBody = """
                {
                  "title": "Finalize backend APIs",
                  "status": "todo",
                  "initialAssignee": "alice",
                  "newStatus": "in-progress",
                  "newAssignee": {
                    "username": "bob",
                    "fullName": "Bob Smith"
                  }
                }
                """;

        mockMvc.perform(post("/api/todos/automation/create-change-status-assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Finalize backend APIs"))
                .andExpect(jsonPath("$.status").value("in-progress"))
                .andExpect(jsonPath("$.assignee").value("bob"));

        // Validate DB entry
        assertThat(todoItemRepository.findAll()).anyMatch(todo ->
                todo.getTitle().equals("Finalize backend APIs") &&
                todo.getStatus().getCode().equals("in-progress") &&
                todo.getAssignee().getUsername().equals("bob")
        );
    }


}