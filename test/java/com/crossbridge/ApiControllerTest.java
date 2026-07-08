package com.crossbridge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Pipeline runs mvn verify — this test MUST pass
    // before Docker image is even built
    // Fail here = fast feedback, no wasted build time
    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void infoEndpointReturnsServiceName() throws Exception {
        mockMvc.perform(get("/api/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("crossbridge"));
    }

    @Test
    void echoEndpointReturnsMessage() throws Exception {
        mockMvc.perform(get("/api/echo/hello"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("hello"));
    }
}
