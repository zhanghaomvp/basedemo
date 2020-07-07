package com.cetcxl.xlpay.admin.controller;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.entity.model.Deal;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_NO;
import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_SIZE;
import static com.cetcxl.xlpay.common.constants.PatternConstants.DATE_TIME;

class DealsControllerTest extends BaseTest {

    @Test
    void listCompanyDeal() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .isNumber()
                );

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .param("payType", Deal.PayType.CASH.name())
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .isNumber()
                );

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .param("payType", Deal.PayType.CASH.name())
                                .param("status", Deal.Status.PAID.name())
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .isNumber()
                );

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .param("payType", Deal.PayType.CASH.name())
                                .param("status", Deal.Status.PAID.name())
                                .param("department", "13234")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .isNumber()
                );

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .param("name", "张浩浩1")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .isNumber()
                );
    }

    @Test
    void listCompanyDeal_withTime_success() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .param("begin", "2020-07-06 16:22:00")
                                .param("end", "2020-07-06 16:22:30")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .value(1)
                );

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .param("begin", "2020-07-06 16:22:30")
                                .param("end", "2020-07-06 16:22:30")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .value(0)
                );
    }

    @Test
    void ListStoreDeal() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/stores/{storeId}/deals", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .isNumber()
                );
    }

    @Test
    void getDealDetail() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/deals/{dealId}", 1, 1)
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.info")
                                .value("test")
                );
    }

    @Test
    void companyDashboard_withOutDepartment() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals/dashboard", 1)
                                .param("begin", "2020-07-06 16:22:00")
                                .param("end", DateTimeFormatter.ofPattern(DATE_TIME).format(LocalDateTime.now()))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.totalAmount")
                                .value("18.0")
                );
    }

    @Test
    void companyDashboard_withDepartment() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/deals/dashboard", 1)
                                .param("department", "区块链")
                                .param("begin", "2020-07-06 16:22:00")
                                .param("end", DateTimeFormatter.ofPattern(DATE_TIME).format(LocalDateTime.now()))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.totalAmount")
                                .value("13.0")
                );
    }
}