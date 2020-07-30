package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.service.PayUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class PayUserControllerTest extends BaseTest {
    @Autowired
    ObjectMapper mapper;

    @Autowired
    private PayUserService payUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(
                PayUser.builder()
                        .id(1)
                        .build()
        );
    }

    @Test
    void getPayUser() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/pay-user")
                                .param("socialCreditCode", "cetcxl")
                                .param("icNo", "511528198909010018")
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.data.phone")
                        .value("17360026771"));
    }

    @Test
    void register_success() throws Exception {
        PayUserController.RegisterReq userAddReq = PayUserController.RegisterReq.builder()
                .icNo(S_TEMP)
                .phone(S_PHONE)
                .password(S_PAY_PASSWORD)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userAddReq))
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                );

        PayUser payUser = payUserService
                .getOne(
                        Wrappers.lambdaQuery(PayUser.class)
                                .eq(PayUser::getIcNo, S_TEMP)
                );

        Assertions.assertNotNull(payUser);
        Assertions.assertTrue(payUserService.removeById(payUser.getId()));
    }

    @Test
    void register_error() throws Exception {
        PayUserController.RegisterReq userAddReq = PayUserController.RegisterReq.builder()
                .icNo("511528198909010018")
                .phone(S_TEMP)
                .password(S_PAY_PASSWORD)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userAddReq))
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.status").value("ERROR")
                );

        userAddReq = PayUserController.RegisterReq.builder()
                .icNo(S_TEMP)
                .phone("17360026771")
                .password(S_PAY_PASSWORD)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userAddReq))
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.status").value("ERROR")
                );

    }

    @Test
    void updatePayPassword_Success() throws Exception {

        PayUserController.UpdatePayPasswordReq req = PayUserController.UpdatePayPasswordReq.builder()
                .oldPassword("741852")
                .newPassword("741852")
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                );

        Assert.assertTrue(passwordEncoder.matches("741852", payUserService.getById(1).getPassword()));
    }

    @Test
    void updateNoPayFunction_Success() throws Exception {
        setAuthentication(
                PayUser.builder()
                        .id(2)
                        .build()
        );

        PayUserController.UpdateNoPayFunctionReq req = PayUserController.UpdateNoPayFunctionReq.builder()
                .isOpen(false)
                .password("741852")
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/secret-free-payment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                );
        Assert.assertEquals(new Integer(0), payUserService.getById(2).getFunctions());

    }

}