package com.cetcxl.xlpay.admin.controller;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.service.XstoreService;
import com.zxl.sdk.XstorSdk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

class UtilControllerTest extends BaseTest {
    @Mock
    XstorSdk xstorSdk;
    @InjectMocks
    @Autowired
    XstoreService xstoreService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void upload() throws Exception {
        Mockito.doReturn(1L)
                .when(xstorSdk)
                .upload(anyString(), any(), anyLong(), anyString(), anyString(), anyString(), anyString());

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