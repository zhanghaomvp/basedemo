package com.cetcxl.xlpay.payuser.controller;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.entity.vo.DealVO;
import com.cetcxl.xlpay.payuser.service.DealService;
import com.cetcxl.xlpay.payuser.service.PayService;
import com.cetcxl.xlpay.payuser.service.WalletCashService;
import com.cetcxl.xlpay.payuser.service.WalletCreditService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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

import static com.cetcxl.xlpay.common.entity.model.Deal.DealType.CASH_DEAL;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayControllerTest extends BaseTest {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DealService dealService;
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        setAuthentication(
                PayUser.builder()
                        .id(1)
                        .icNo(S_ICNO)
                        .build()
        );
    }

    @Test
    void getCashBalance() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/pay-user/company/{socialCreditCode}/wallet/cash", S_CETCXL)
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data").value(Matchers.notNullValue())
                );
    }

    @Test
    void getCreditBalance() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/pay-user/company/{socialCreditCode}/wallet/credit", S_CETCXL)
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.creditBalance").value("100.0")
                );
    }

    @Test
    void payCash() throws Exception {
        BigDecimal oldCashBalance = walletCashService.getById(1).getCashBalance();

        PayController.PayReq req = PayController.PayReq.builder()
                .amount("5")
                .storeId(1)
                .info(S_TEMP)
                .password(S_PAY_PASSWORD)
                .build();

        String content = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/wallet/cash/{walletId}/deal", 2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        DealVO dealVO = objectMapper
                .readValue(
                        content,
                        new TypeReference<ResBody<DealVO>>() {
                        }
                )
                .getData();

        Deal deal = dealService.getById(dealVO.getId());

        assertTrue(S_TEMP.equals(deal.getInfo()));
        assertTrue(CASH_DEAL == deal.getType());

        BigDecimal newCashBalance = walletCashService.getById(2).getCashBalance();
        assertTrue(newCashBalance.add(new BigDecimal("5")).compareTo(oldCashBalance) == 0);
    }

    @Test
    void payCash_noPassword_success() throws Exception {
        PayController.PayReq req = PayController.PayReq.builder()
                .amount("1")
                .storeId(1)
                .info(S_TEMP)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/wallet/cash/{walletId}/deal", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.status").value(Matchers.is(ResBody.Status.OK.name()))
                );
    }

    @Test
    void payCash_noPassword_error() throws Exception {
        setAuthentication(
                PayUser.builder()
                        .id(2)
                        .build()
        );

        PayController.PayReq req = PayController.PayReq.builder()
                .amount("1")
                .storeId(1)
                .info(S_TEMP)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/wallet/cash/{walletId}/deal", 2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.status").value(Matchers.is(ResBody.Status.ERROR.name()))
                );

    }

    @Test
    void payCredit() throws Exception {
        setAuthentication(
                PayUser.builder()
                        .id(2)
                        .build()
        );

        WalletCredit oldWalletCredit = walletCreditService.getById(2);

        PayController.PayReq req = PayController.PayReq.builder()
                .amount("5")
                .storeId(2)
                .info(S_TEMP)
                .password(S_PAY_PASSWORD)
                .build();

        String content = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/wallet/credit/{walletId}/deal", 2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        DealVO dealVO = objectMapper
                .readValue(
                        content,
                        new TypeReference<ResBody<DealVO>>() {
                        }
                )
                .getData();

        Deal deal = dealService.getById(dealVO.getId());

        assertTrue(S_TEMP.equals(deal.getInfo()));
        assertTrue(Deal.PayType.CREDIT == deal.getPayType());

        WalletCredit newWalletCredit = walletCreditService.getById(2);
        assertTrue(
                newWalletCredit.getCreditBalance()
                        .add(new BigDecimal("5"))
                        .compareTo(oldWalletCredit.getCreditBalance()) == 0
        );
    }

    @Test
    void storeWallet() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/pay-user/company/{socialCreditCode}/store/{storeId}/wallets", "cetcxl", 1)
                                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        PayService.StoreWalletDTO dto = objectMapper
                .readValue(
                        mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<ResBody<PayService.StoreWalletDTO>>() {
                        }
                )
                .getData();

        Assertions.assertEquals(dto.getCompanyName(), "中国电科");
        Assertions.assertNull(dto.getCreditWallet());
        assertTrue(dto.getCashWallet().getCashBalance().intValue() > 0);
    }
}