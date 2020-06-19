package com.cetcxl.xlpay.admin.server.controller;

import com.cetcxl.xlpay.admin.server.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class CompanyControllerTest extends BaseTest {
    @Test
    void detail_success() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/company/1")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.name").value("中国电科")
                );
    }
}