package com.cetcxl.xlpay.admin.controller;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.admin.dao.ChecksMapper;
import com.cetcxl.xlpay.admin.entity.vo.ChecksVO;
import com.cetcxl.xlpay.admin.service.ChecksRecordService;
import com.cetcxl.xlpay.admin.service.ChecksService;
import com.cetcxl.xlpay.admin.service.DealService;
import com.cetcxl.xlpay.admin.service.WalletCreditService;
import com.cetcxl.xlpay.common.entity.model.*;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_NO;
import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_SIZE;
import static com.cetcxl.xlpay.common.entity.model.Checks.Status.*;
import static com.cetcxl.xlpay.common.entity.model.Deal.Status.CHECKING;

class ChecksControllerTest extends BaseTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ChecksService checksService;
    @Autowired
    ChecksRecordService checksRecordService;
    @Autowired
    DealService dealService;

    @Autowired
    WalletCreditService walletCreditService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        setAuthentication(
                Company.builder()
                        .id(1)
                        .build()
        );
    }

    @Test
    void addCheck() throws Exception {
        ChecksController.AddCheckReq req = ChecksController.AddCheckReq.builder()
                .storeId(1)
                .dealIds(Lists.newArrayList(1, 2))
                .attachments(Lists.newArrayList("1", "2"))
                .payType(Deal.PayType.CASH)
                .info(S_TEMP)
                .build();

        MvcResult mvcResult = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/deals/checks", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResBody<ChecksVO> resBody = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<ResBody<ChecksVO>>() {
                });

        Checks checks = checksService.getById(resBody.getData().getBatch());
        Assert.assertTrue(checks.getTotalDealAmonut().compareTo(new BigDecimal("3")) == 0);

        Deal deal = dealService.getById(2);
        Assert.assertTrue(CHECKING == deal.getStatus());
        Assert.assertTrue(checks.getBatch().equals(deal.getCheckBatch()));

        ChecksRecord checksRecord = checksRecordService.lambdaQuery()
                .eq(ChecksRecord::getCheckBatch, checks.getBatch())
                .one();
        Assert.assertTrue(APPLY == checksRecord.getAction());
    }

    @Test
    void auditCheck() throws Exception {
        ChecksController.AuditCheckReq req = ChecksController.AuditCheckReq.builder()
                .approvalOrReject(Boolean.TRUE)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/companys/{companyId}/deals/checks/{checkBatch}", 1, 2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Checks checks = checksService.getById(2);
        Assert.assertTrue(checks.getStatus() == APPROVAL);

        ChecksRecord checksRecord = checksRecordService.lambdaQuery()
                .eq(ChecksRecord::getCheckBatch, checks.getBatch())
                .eq(ChecksRecord::getAction, APPROVAL)
                .one();
        Assert.assertNotNull(checksRecord);
    }

    @Test
    void confirmCheck() throws Exception {
        WalletCredit oldWalletCredit = walletCreditService.getById(2);
        Assert.assertTrue(oldWalletCredit.getCreditQuota().compareTo(oldWalletCredit.getCreditBalance()) > 0);

        ChecksController.AuditCheckReq req = ChecksController.AuditCheckReq.builder()
                .confirmOrDeny(Boolean.TRUE)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/stores/{storeId}/deals/checks/{checkBatch}", 1, 3)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Checks checks = checksService.getById(3);
        Assert.assertTrue(checks.getStatus() == COFIRM);

        WalletCredit nowWalletCredit = walletCreditService.getById(2);
        Assert.assertTrue(nowWalletCredit.getCreditQuota().compareTo(nowWalletCredit.getCreditBalance()) == 0);

        ChecksRecord checksRecord = checksRecordService.lambdaQuery()
                .eq(ChecksRecord::getCheckBatch, checks.getBatch())
                .eq(ChecksRecord::getAction, COFIRM)
                .one();
        Assert.assertNotNull(checksRecord);
        Assert.assertTrue(checksRecord.getOperator().equals(1));
    }

    @Test
    void listCompanyCheck() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/checks", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .isNumber()
                )
                .andReturn();


        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        JsonNode data = jsonNode.get("data").get("records");
        final List<ChecksMapper.CheckDTO> checkDTOS = objectMapper.readValue(
                data.toString(),
                new TypeReference<List<ChecksMapper.CheckDTO>>() {
                }
        );

        Assertions.assertTrue(checkDTOS.size() > 0);
        Assertions.assertTrue(checkDTOS.get(2).getApprovalPhone().equals("17360126771"));

    }

    @Test
    void getCheckDetail() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/checks/{checkId}", 3)
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.status")
                                .value(APPROVAL.name())
                );
    }
}