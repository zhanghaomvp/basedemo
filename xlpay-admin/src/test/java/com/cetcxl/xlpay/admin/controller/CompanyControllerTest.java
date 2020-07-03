package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.admin.rpc.TrustlinkDataRpcService;
import com.cetcxl.xlpay.admin.service.CompanyService;
import com.cetcxl.xlpay.admin.service.CompanyStoreRelationService;
import com.cetcxl.xlpay.admin.service.VerifyCodeService;
import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.common.rpc.ResBody;
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

import java.util.Optional;

import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_NO;
import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_SIZE;
import static org.mockito.ArgumentMatchers.eq;

class CompanyControllerTest extends BaseTest {

    @Autowired
    ObjectMapper mapper;

    @Mock
    VerifyCodeService verifyCodeService;
    @Mock
    TrustlinkDataRpcService trustlinkDataRpcService;

    @InjectMocks
    @Autowired
    CompanyController companyController;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        companyController = (CompanyController) AopProxyUtils.getSingletonTarget(companyController);
        MockitoAnnotations.initMocks(this);
    }

    @Autowired
    CompanyStoreRelationService companyStoreRelationService;

    @Autowired
    CompanyService companyService;


    @Test
    void detail_success() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/1")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.name").value("中国电科")
                );
    }

    @Test
    void stores_hasRelation_success() throws Exception {
        setAuthentication(
                Company.builder()
                        .id(1)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/stores", 1)
                                .param("hasRelation", "true")
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total").value(2)
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.records[0].name").value("shop1")
                );
    }

    @Test
    void stores_not_hasRelation_success() throws Exception {
        setAuthentication(
                Company.builder()
                        .id(1)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/companys/{companyId}/stores", 1)
                                .param("hasRelation", "false")
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total").value(1)
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.records[0].name").value("shop3")
                );
    }

    @Test
    void register_success() throws Exception {
        Mockito.doReturn(true)
                .when(verifyCodeService)
                .checkVerifyCode(eq(S_PHONE), eq(S_VERIFY_CODE));

        Mockito
                .doReturn(
                        Optional.of(
                                TrustlinkDataRpcService.CompanyInfo.builder()
                                        .organizationName(S_CETCXL)
                                        .organizationCreditId(S_TEMP)
                                        .organizationTel(S_PHONE)
                                        .organizationEmail(S_TEMP)
                                        .build()
                        )
                )
                .when(trustlinkDataRpcService)
                .getCompanyInfo(eq(S_TEMP));

        CompanyController.CompanyRegisterReq req = CompanyController.CompanyRegisterReq.builder()
                .phone(S_PHONE)
                .password(S_TEMP)
                .verifyCode(S_VERIFY_CODE)
                .socialCreditCode(S_TEMP)
                .name(S_CETCXL)
                .build();

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/companys/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers
                        .jsonPath("$.data.companyName").value(S_CETCXL)
        ).andReturn();

        Assert.assertTrue(
                companyService.remove(
                        Wrappers.lambdaQuery(Company.class)
                                .eq(Company::getPhone, S_PHONE)
                )
        );
    }

    @Test
    void addCompanyStoreRelation_success() throws Exception {
        setAuthentication(
                Company.builder()
                        .id(1)
                        .build()
        );

        CompanyController.CompanyStoreRelationReq req = CompanyController.CompanyStoreRelationReq.builder()
                .canCashPay(true)
                .canCreditPay(true)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/companys/{companyId}/stores/{storeId}/company-store-relation", 1, 3)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.id").value(3)
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.applyReleation").value(3)
                );

        Assert.assertTrue(companyStoreRelationService.removeById(3));
    }

    @Test
    void updateCompanyStoreRelation_close_success() throws Exception {
        setAuthentication(
                Company.builder()
                        .id(1)
                        .build()
        );

        CompanyController.CompanyStoreRelationReq req = CompanyController.CompanyStoreRelationReq.builder()
                .canCashPay(false)
                .canCreditPay(false)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/companys/{companyId}/stores/{storeId}/company-store-relation/{id}", 1, 1, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.status").value(ResBody.Status.OK.name())
                );

        CompanyStoreRelation relation = companyStoreRelationService.getById(1);
        Assert.assertTrue(
                CompanyStoreRelation.Relation.CASH_PAY
                        .isClose(relation.getApplyReleation())
        );
        Assert.assertTrue(
                CompanyStoreRelation.Relation.CASH_PAY
                        .isClose(relation.getRelation())
        );

        Assert.assertTrue(CompanyStoreRelation.RelationStatus.WORKING == relation.getStatus());

        companyStoreRelationService.update(
                Wrappers.lambdaUpdate(CompanyStoreRelation.class)
                        .set(CompanyStoreRelation::getRelation, 1)
                        .eq(CompanyStoreRelation::getId, relation.getId())
        );
    }

    @Test
    void updateCompanyStoreRelation_add_success() throws Exception {
        setAuthentication(
                Company.builder()
                        .id(1)
                        .build()
        );

        CompanyController.CompanyStoreRelationReq req = CompanyController.CompanyStoreRelationReq.builder()
                .canCashPay(false)
                .canCreditPay(true)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/companys/{companyId}/stores/{storeId}/company-store-relation/{id}", 1, 1, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.status").value(ResBody.Status.OK.name())
                );

        CompanyStoreRelation relation = companyStoreRelationService.getById(1);
        Assert.assertTrue(
                CompanyStoreRelation.Relation.CREDIT_PAY
                        .isOpen(relation.getApplyReleation())
        );
        Assert.assertTrue(
                CompanyStoreRelation.Relation.CASH_PAY
                        .isClose(relation.getRelation())
        );
        Assert.assertFalse(
                CompanyStoreRelation.Relation.CREDIT_PAY
                        .isOpen(relation.getRelation())
        );
        Assert.assertTrue(CompanyStoreRelation.RelationStatus.APPROVAL == relation.getStatus());
    }
}