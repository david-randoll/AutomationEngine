package com.davidrandoll.automation.engine.ui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class UIConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAppConfig_ShouldReturnJavaScriptConfigWithContextPath() throws Exception {
        mockMvc.perform(get("/app-config.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/javascript"))
                .andExpect(content().string("window.__APP_CONFIG__ = { contextPath: '' };"));
    }

    @Test
    void getAppConfig_WithSingleLevelWildcard() throws Exception {
        mockMvc.perform(get("/subfolder/app-config.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/javascript"))
                .andExpect(content().string("window.__APP_CONFIG__ = { contextPath: '' };"));
    }

    @Test
    void getAppConfig_WithTwoLevelWildcard() throws Exception {
        mockMvc.perform(get("/level1/level2/app-config.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/javascript"))
                .andExpect(content().string("window.__APP_CONFIG__ = { contextPath: '' };"));
    }

    @Test
    void getAppConfig_WithThreeLevelWildcard() throws Exception {
        mockMvc.perform(get("/a/b/c/app-config.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/javascript"))
                .andExpect(content().string("window.__APP_CONFIG__ = { contextPath: '' };"));
    }

    @Test
    void getAppConfigJson_ShouldReturnJsonConfigWithContextPath() throws Exception {
        mockMvc.perform(get("/app-config.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.contextPath", is("")));
    }

    @Test
    void getAppConfigJson_WithSingleLevelWildcard() throws Exception {
        mockMvc.perform(get("/folder/app-config.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.contextPath", is("")));
    }

    @Test
    void getAppConfigJson_WithTwoLevelWildcard() throws Exception {
        mockMvc.perform(get("/dir1/dir2/app-config.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.contextPath", is("")));
    }

    @Test
    void getAppConfigJson_WithThreeLevelWildcard() throws Exception {
        mockMvc.perform(get("/x/y/z/app-config.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.contextPath", is("")));
    }
}
