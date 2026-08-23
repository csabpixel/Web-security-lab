package app.vulnerable.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
class HydraControllerTest {

    @Autowired
    WebApplicationContext ctx;

    MockMvc mvc;

    @BeforeEach
    void setup() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        mvc.perform(post("/api/hydra/mode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mode\":\"easy\"}"));
    }


    @Test
    @DisplayName("Könnyű módban 1, nehéz módban 5 felhasználó")
    void easyHardUsers() throws Exception {
        MvcResult easy = mvc.perform(get("/api/hydra/users")).andReturn();
        long easyCount = Arrays.stream(easy.getResponse().getContentAsString().split("\n"))
                .filter(s -> !s.isBlank()).count();
        assertThat(easyCount).isEqualTo(1);

        mvc.perform(post("/api/hydra/mode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mode\":\"hard\"}"));

        MvcResult hard = mvc.perform(get("/api/hydra/users")).andReturn();
        long hardCount = Arrays.stream(hard.getResponse().getContentAsString().split("\n"))
                .filter(s -> !s.isBlank()).count();
        assertThat(hardCount).isEqualTo(5);
    }
}
