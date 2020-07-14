package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.service.PayUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void register_success() throws Exception {
        PayUserController.RegisterReq userAddReq = PayUserController.RegisterReq.builder()
                .icNo(S_TEMP)
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
    void updatePayPassword_Success() throws Exception {

        PayUserController.UpdatePayPasswordReq req = PayUserController.UpdatePayPasswordReq.builder()
                .oldPassword("741852")
                .newPassword("741852")
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/pay-user/{id}/password", 1)
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

        PayUserController.UpdateNoPayFunctionReq req = PayUserController.UpdateNoPayFunctionReq.builder()
                .isOpen(false)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/pay-user/{id}/secret-free-payment", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                );
        Assert.assertEquals(new Integer(0), payUserService.getById(1).getFunctions());

    }
}