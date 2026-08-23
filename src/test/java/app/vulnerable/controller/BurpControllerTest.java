package app.vulnerable.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
class BurpControllerTest {

    @Autowired
    WebApplicationContext ctx;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    @DisplayName("3 hibás próba után késleltetés")
    void pinDelay() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/burp/pin/reset").session(session));

        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/api/burp/pin").param("code", "999").session(session));
        }

        long start = System.currentTimeMillis();
        mvc.perform(get("/api/burp/pin").param("code", "999").session(session));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isGreaterThanOrEqualTo(500);
    }

    @Test
    @DisplayName("8 hibás próba után lockout")
    void pinLockout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/burp/pin/reset").session(session));

        for (int i = 0; i < 8; i++) {
            mvc.perform(get("/api/burp/pin").param("code", "999").session(session));
        }

        mvc.perform(get("/api/burp/pin").param("code", "999").session(session))
                .andExpect(jsonPath("$.locked").value(true));
    }
}
