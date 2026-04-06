package com.example.back.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.imports.dto.PythonHealthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private PythonParserClient pythonParserClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HealthController(pythonParserClient)).build();
    }

    @Test
    void healthReturnsJavaServiceStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("ok")))
            .andExpect(jsonPath("$.service", is("java-api")));
    }

    @Test
    void pythonHealthReturnsDelegatedResponse() throws Exception {
        PythonHealthResponse response = new PythonHealthResponse();
        response.setStatus("ok");
        response.setService("python-parser");
        when(pythonParserClient.getHealth()).thenReturn(response);

        mockMvc.perform(get("/api/python/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service", is("python-parser")));
    }
}
