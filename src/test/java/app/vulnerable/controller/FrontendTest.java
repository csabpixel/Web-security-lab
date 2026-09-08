package app.vulnerable.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class FrontendTest {

    @Autowired
    WebApplicationContext ctx;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    @DisplayName("Hydra wordlist végpont elérhető és nem üres")
    void wordlistEndpoint() throws Exception {
        mvc.perform(get("/api/hydra/wordlist"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Pizza menü végpont elérhető")
    void menuEndpoint() throws Exception {
        mvc.perform(get("/api/pizza/menu"))
                .andExpect(status().isOk());
    }
}
