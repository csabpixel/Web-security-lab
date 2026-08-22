package app.vulnerable.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
class SqliControllerTest {

    @Autowired
    WebApplicationContext ctx;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    @DisplayName("Vulnerable és secure mód")
    void vulnerableVsSecure() throws Exception {
        mvc.perform(post("/api/sqli/search")
                        .contentType("application/json")
                        .content("{\"mode\":\"vulnerable\",\"query\":\"' OR '1'='1\"}"))
                .andExpect(jsonPath("$.results.length()").value(Matchers.greaterThan(1)));

        mvc.perform(post("/api/sqli/search")
                        .contentType("application/json")
                        .content("{\"mode\":\"secure\",\"query\":\"' OR '1'='1\"}"))
                .andExpect(jsonPath("$.results.length()").value(0));
    }

    @Test
    @DisplayName("Task 1 belépés")
    void task1() throws Exception {
        mvc.perform(post("/api/sqli/tasks/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin' OR '1'='1\",\"password\":\"barmi\"}"))
                .andExpect(jsonPath("$.success").value(true));
    }
}
