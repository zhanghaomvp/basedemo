package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.common.entity.model.Store;
import com.cetcxl.xlpay.admin.BaseTest;
import com.cetcxl.xlpay.common.entity.model.StoreUser;
import com.cetcxl.xlpay.admin.service.StoreService;
import com.cetcxl.xlpay.admin.service.StoreUserService;
import com.cetcxl.xlpay.admin.service.VerifyCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.eq;

class StoreControllerTest extends BaseTest {
    @Autowired
    ObjectMapper mapper;

    @Mock
    VerifyCodeService verifyCodeService;

    @InjectMocks
    @Autowired
    StoreController storeController;

    @Autowired
    StoreService storeService;

    @Autowired
    StoreUserService storeUserService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        storeController = (StoreController) AopProxyUtils.getSingletonTarget(storeController);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void store_register_success() throws Exception {
        Mockito.doReturn(true)
                .when(verifyCodeService)
                .checkVerifyCode(eq(S_PHONE), eq(S_VERIFY_CODE));

        StoreController.StoreRegisterReq req = StoreController.StoreRegisterReq.builder()
                .phone(S_PHONE)
                .password(S_TEMP)
                .verifyCode(S_VERIFY_CODE)
                .name(S_SHOP)
                .contact(S_TEMP)
                .contactPhone(S_PHONE)
                .address(S_TEMP)
                .socialCreditCode(S_TEMP)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/stores/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req))
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.storeName").value(S_SHOP)
                )
                .andReturn();

        Assert.assertTrue(
                storeService.remove(
                        Wrappers
                                .lambdaQuery(Store.class)
                                .eq(Store::getName, S_SHOP)
                )

        );

        Assert.assertTrue(
                storeUserService.remove(
                        Wrappers
                                .lambdaQuery(StoreUser.class)
                                .eq(StoreUser::getPhone, S_PHONE)
                )

        );

    }
}



