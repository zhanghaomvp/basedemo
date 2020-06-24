package com.cetcxl.xlpay.admin.server.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.admin.server.BaseTest;
import com.cetcxl.xlpay.admin.server.entity.model.Company;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCash;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCreditFlow;
import com.cetcxl.xlpay.admin.server.service.WalletCashService;
import com.cetcxl.xlpay.admin.server.service.WalletCreditFlowService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

import static com.cetcxl.xlpay.admin.server.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_NO;
import static com.cetcxl.xlpay.admin.server.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_SIZE;

public class CompanyMemberControllerTest extends BaseTest {

    @Autowired
    private WalletCashService walletCashService;
    @Autowired
    private WalletCreditFlowService walletCreditFlowService;

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

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{id}/status", 1, 1, 1)
                                .param("status", WalletCash.WalletCashStaus.DISABLE.name())
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

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/members/{companyMemberId}/wallet/cash/{id}/balance", 1, 1, 1)
                                .param("amount", "125")
                                .param("dealType", "ADMIN_RECHARGE")
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

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/members/1/wallet/credit/{id}/quota", 1, 1, 1)
                                .param("quota", "200")
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

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/members/wallet/cashs/balance", 1)
                                .param("walletIds[]", "1", "2")
                                .param("companyMemberIds[]", "1", "2")
                                .param("dealType", "ADMIN_RECHARGE")
                                .param("amount", "100")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk()
                );

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
}
