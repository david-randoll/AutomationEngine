package com.davidrandoll.automation.engine.ui;

import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UIConfigControllerTest extends AutomationEngineTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/app-config.js",
            "/subfolder/app-config.js",
            "/level1/level2/app-config.js",
            "/a/b/c/app-config.js"
    })
    void getAppConfig_ShouldReturnJavaScriptConfigForAnyPath(String path) throws Exception {

        MvcResult result = mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/javascript"))
                .andReturn();

        String response = result.getResponse().getContentAsString();

        // Validate JS wrapper
        assertThat(response)
                .startsWith("window.__APP_CONFIG__ = ")
                .endsWith(";");

        // Extract JSON payload
        String json = response
                .replaceFirst("^window.__APP_CONFIG__ = ", "")
                .replaceFirst(";$", "");

        // Semantic JSON assertion (order-independent)
        JSONAssert.assertEquals(
                """
                        {
                          "apiPath": "/automation-engine",
                          "contextPath": "",
                          "uiPath": "/automation-engine"
                        }
                        """,
                json,
                true
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/app-config.json",
            "/folder/app-config.json",
            "/dir1/dir2/app-config.json",
            "/x/y/z/app-config.json"
    })
    void getAppConfigJson_ShouldReturnJsonConfigForAnyPath(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.contextPath", is("")))
                .andExpect(jsonPath("$.uiPath", is("/automation-engine")))
                .andExpect(jsonPath("$.apiPath", is("/automation-engine")));
    }
}
