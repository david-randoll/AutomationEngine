package com.davidrandoll.automation.engine.todo_example.manual_flow;

import com.davidrandoll.automation.engine.todo_example.TodoExampleApplication;
import com.davidrandoll.automation.engine.todo_example.db.table.TodoItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TodoExampleApplication.class)
@AutoConfigureMockMvc
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        todoService.getAll().forEach(todo -> todoService.delete(todo.getId()));
    }

    @Test
    void testCreateTodo() throws Exception {
        mockMvc.perform(post("/api/todos/manual")
                        .param("title", "Test Todo")
                        .param("statusCode", "OPEN")
                        .param("assigneeUsername", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Todo"))
                .andExpect(jsonPath("$.status.code").value("OPEN"))
                .andExpect(jsonPath("$.assignee.username").value("john"));

        List<TodoItem> all = todoService.getAll();
        assertThat(all).hasSize(1);
    }

    @Test
    void testUpdateTitle() throws Exception {
        TodoItem created = todoService.create("Original", "OPEN", null);

        mockMvc.perform(put("/api/todos/manual/{id}/title", created.getId())
                        .param("title", "Updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void testChangeStatus() throws Exception {
        TodoItem created = todoService.create("Status Test", "OPEN", null);

        mockMvc.perform(put("/api/todos/manual/{id}/status", created.getId())
                        .param("statusCode", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("IN_PROGRESS"));
    }

    @Test
    void testAssignUser() throws Exception {
        TodoItem created = todoService.create("Assign Test", "OPEN", null);

        mockMvc.perform(put("/api/todos/manual/{id}/assign", created.getId())
                        .param("username", "david"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.username").value("david"));
    }

    @Test
    void testUnassignUser() throws Exception {
        TodoItem created = todoService.create("Unassign Test", "OPEN", "david");

        mockMvc.perform(put("/api/todos/manual/{id}/unassign", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee").doesNotExist());
    }

    @Test
    void testDeleteTodo() throws Exception {
        TodoItem created = todoService.create("Delete Me", "OPEN", null);

        mockMvc.perform(delete("/api/todos/manual/{id}", created.getId()))
                .andExpect(status().isNoContent());

        assertThat(todoService.getAll()).isEmpty();
    }

    @Test
    void testGetAllAndGetById() throws Exception {
        TodoItem one = todoService.create("First", "OPEN", null);
        TodoItem two = todoService.create("Second", "OPEN", null);

        mockMvc.perform(get("/api/todos/manual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/todos/manual/{id}", one.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First"));
    }
}