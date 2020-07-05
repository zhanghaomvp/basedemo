package com.cetcxl.xlpay.admin.controller;

import com.cetcxl.xlpay.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

class UtilControllerTest extends BaseTest {

    @Test
    void upload() throws Exception {

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", "123.xlsx", "", new byte[]{});
        mockMvc
                .perform(
                        multipart("/util/upload/xstore")
                                .file(mockMultipartFile)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileName").value("123.xlsx"));
    }
}