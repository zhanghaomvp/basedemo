package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.admin.service.WalletCashService;
import com.cetcxl.xlpay.admin.service.WalletCreditFlowService;
import com.cetcxl.xlpay.admin.service.WalletCreditService;
import com.cetcxl.xlpay.common.entity.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_NO;
import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_SIZE;

public class CompanyMemberControllerTest extends BaseTest {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WalletCashService walletCashService;
    @Autowired
    private WalletCreditFlowService walletCreditFlowService;
    @Autowired
    private WalletCreditService walletCreditService;

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
    void listCompanyMemberWalletCash_Success_NoParam() throws Exception {

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/members/wallet/cash", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total")
                                .value(2)
                );

    }

    @Test
    void listCompanyMemberWalletCash_Success_Param() throws Exception {

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/members/wallet/cash", 1)
                                .param("name", "张浩浩")
                                .param("department", "财务部")
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total").value(0)
                );
    }

    @Test
    void updateWalletCashStatus_Success() throws Exception {
        CompanyMemberController.UpdateWalletCashStatusReq req = CompanyMemberController.UpdateWalletCashStatusReq.builder()
                .status(WalletCash.WalletCashStaus.DISABLE)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{id}/status", 1, 1, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        WalletCash walletCash = walletCashService
                .getOne(
                        Wrappers
                                .lambdaQuery(WalletCash.class)
                                .eq(WalletCash::getId, 1)
                );
        Assert.assertTrue(walletCash.getStatus() == WalletCash.WalletCashStaus.DISABLE);
    }

    @Test
    void updateWalletCashAmount_Success() throws Exception {
        WalletCash originWalletCash = walletCashService
                .getOne(
                        Wrappers
                                .lambdaQuery(WalletCash.class)
                                .eq(WalletCash::getId, 1)
                );

        CompanyMemberController.UpdateWalletCashAmountReq req = CompanyMemberController.UpdateWalletCashAmountReq.builder()
                .dealType(Deal.DealType.ADMIN_RECHARGE)
                .amount("125")
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{id}/balance", 1, 1, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        WalletCash nowWalletCash = walletCashService
                .getOne(
                        Wrappers.lambdaQuery(WalletCash.class)
                                .eq(WalletCash::getId, 1)
                );

        Assert.assertTrue(
                nowWalletCash.getCashBalance()
                        .compareTo(
                                originWalletCash.getCashBalance()
                                        .add(new BigDecimal("125"))
                        ) == 0
        );
    }

    @Test
    void listWalletCredit_Success_Department() throws Exception {

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/members/wallet/credit", 1)
                                .param("name", "张浩浩")
                                .param("department", "区块链")
                                .param("pageNo", "1")
                                .param("pageSize", "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total").value(1)
                );
    }

    /**
     * 根据部门填写来反向单测
     *
     * @throws Exception
     */
    @Test
    void listWalletCredit_Success_no_Department() throws Exception {

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/members/wallet/credit", 1)
                                .param("name", "张浩浩")
                                .param("department", "体育部")
                                .param("pageNo", "1")
                                .param("pageSize", "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total").value(0)
                );
    }

    @Test
    void updateWalletCreditQuota_Success() throws Exception {

        CompanyMemberController.UpdateWalletCreditQuotaReq req = CompanyMemberController.UpdateWalletCreditQuotaReq.builder()
                .quota("200")
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/members/1/wallet/credit/{id}/quota", 1, 1, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        WalletCreditFlow walletCreditFlow = walletCreditFlowService
                .getOne(
                        Wrappers.lambdaQuery(WalletCreditFlow.class)
                                .eq(WalletCreditFlow::getId, 1)
                );
        Assert.assertTrue(walletCreditFlow.getQuota().compareTo(new BigDecimal("200")) == 0);
    }

    @Test
    void batchUpdateWalletCashAmount_Success() throws Exception {
        WalletCash originWalletCash = walletCashService
                .getOne(
                        Wrappers.lambdaQuery(WalletCash.class)
                                .eq(WalletCash::getId, 2)
                );

        CompanyMemberController.BatchUpdateWalletCashAmountReq req =
                CompanyMemberController.BatchUpdateWalletCashAmountReq.builder()
                        .walletIds(Lists.newArrayList(1, 2))
                        .companyMemberIds(Lists.newArrayList(1, 2))
                        .dealType(Deal.DealType.ADMIN_RECHARGE)
                        .amount("100")
                        .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/members/wallet/cashs/balance", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        WalletCash nowWalletCash = walletCashService
                .getOne(
                        Wrappers.lambdaQuery(WalletCash.class)
                                .eq(WalletCash::getId, 2)
                );

        Assert.assertTrue(
                nowWalletCash.getCashBalance()
                        .compareTo(
                                originWalletCash.getCashBalance()
                                        .add(new BigDecimal("100"))
                        ) == 0
        );
    }


    @Test
    void updateWalletCreditStatus_Success() throws Exception {
        CompanyMemberController.UpdateWalletCreditStatusReq req = CompanyMemberController.UpdateWalletCreditStatusReq.builder()
                .status(WalletCredit.WalletCreditStaus.DISABLE)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/companys/{companyId}/members/{companyMemberId}/wallet/credit/{id}/status", 1, 1, 1)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(walletCreditService.getById(1).getStatus().name(), WalletCredit.WalletCreditStaus.DISABLE.name());

    }

    @Test
    void batchUpdateWalletCreditQuota_Success() throws Exception {

        WalletCredit originWalletCredit = walletCreditService
                .getOne(
                        Wrappers.lambdaQuery(WalletCredit.class)
                                .eq(WalletCredit::getId, 2)
                );

        CompanyMemberController.BatchUpdateWalletCreditQuotaReq req =
                CompanyMemberController.BatchUpdateWalletCreditQuotaReq.builder()
                        .walletIds(Lists.newArrayList(1, 2))
                        .companyMemberIds(Lists.newArrayList(1, 2))
                        .quota("200")
                        .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/members/wallet/credits/balance", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        WalletCredit nowWalletCredit = walletCreditService
                .getOne(
                        Wrappers.lambdaQuery(WalletCredit.class)
                                .eq(WalletCredit::getId, 2)
                );

        Assert.assertTrue(
                nowWalletCredit.getCreditBalance()
                        .compareTo(
                                originWalletCredit.getCreditBalance()
                                        .add(new BigDecimal("200"))
                        ) == 0
        );
    }
}
