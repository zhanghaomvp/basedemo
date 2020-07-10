package com.cetcxl.xlpay.payuser.controller;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.payuser.dao.DealMapper;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_NO;
import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_SIZE;
import static com.cetcxl.xlpay.common.constants.PatternConstants.DATE_TIME;

class DealsControllerTest extends BaseTest {

    @Test
    void listDeal() throws Exception {
        setAuthentication(
                PayUser.builder()
                        .id(1)
                        .icNo(S_ICNO)
                        .build()
        );

        MvcResult mvcResult = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/pay-user/deals", 1)
                                .param("storeName", "shop1")
                                .param("begin", "2020-07-01 00:00:00")
                                .param("end", DateTimeFormatter.ofPattern(DATE_TIME).format(LocalDateTime.now()))
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        List<DealMapper.DealDTO> dealDTOS = objectMapper
                .readValue(
                        jsonNode.get("data").get("records").toString(),
                        new TypeReference<List<DealMapper.DealDTO>>() {
                        }
                );

        Assertions.assertTrue(dealDTOS.size() == 4);
    }

    @Test
    void deal() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/pay-user/deals/{dealId}", 1)
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.data.info").value("test")
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.data.companyName").value("中国电科")
                );
    }
}