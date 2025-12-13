package com.davidrandoll.automation.engine.ui;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UIConfigControllerTest extends AutomationEngineTest {
    @Test
    void getAppConfig_ShouldReturnJavaScriptConfigWithContextPath() throws Exception {
        mockMvc.perform(get("/app-config.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/javascript"))
                .andExpect(content().string("window.__APP_CONFIG__ = { contextPath: '' };"));
    }

    @Test
    void getAppConfig_WithSingleLevelWildcard() throws Exception {
        mockMvc.perform(get("/subfolder/app-config.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/javascript"))
                .andExpect(content().string("window.__APP_CONFIG__ = { contextPath: '' };"));
    }

    @Test
    void getAppConfig_WithTwoLevelWildcard() throws Exception {
        mockMvc.perform(get("/level1/level2/app-config.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/javascript"))
                .andExpect(content().string("window.__APP_CONFIG__ = { contextPath: '' };"));
    }

    @Test
    void getAppConfig_WithThreeLevelWildcard() throws Exception {
        mockMvc.perform(get("/a/b/c/app-config.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/javascript"))
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
