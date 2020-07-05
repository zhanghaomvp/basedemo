package com.cetcxl.xlpay.admin.controller;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.admin.entity.vo.ChecksVO;
import com.cetcxl.xlpay.admin.service.ChecksService;
import com.cetcxl.xlpay.admin.service.DealService;
import com.cetcxl.xlpay.admin.service.WalletCreditService;
import com.cetcxl.xlpay.common.entity.model.Checks;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

import static com.cetcxl.xlpay.common.entity.model.Checks.Status.CONFIRM;
import static com.cetcxl.xlpay.common.entity.model.Checks.Status.FINISH;
import static com.cetcxl.xlpay.common.entity.model.Deal.Status.CHECKING;

class ChecksControllerTest extends BaseTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ChecksService checksService;
    @Autowired
    DealService dealService;

    @Autowired
    WalletCreditService walletCreditService;

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
        Assert.assertTrue(checks.getStatus() == CONFIRM);
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
        Assert.assertTrue(checks.getStatus() == FINISH);

        WalletCredit nowWalletCredit = walletCreditService.getById(2);
        Assert.assertTrue(nowWalletCredit.getCreditQuota().compareTo(nowWalletCredit.getCreditBalance()) == 0);

    }
}