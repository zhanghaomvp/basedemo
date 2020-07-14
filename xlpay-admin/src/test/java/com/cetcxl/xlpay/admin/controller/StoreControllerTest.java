package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.admin.entity.model.StoreUser;
import com.cetcxl.xlpay.admin.service.CompanyStoreRelationService;
import com.cetcxl.xlpay.admin.service.StoreService;
import com.cetcxl.xlpay.admin.service.StoreUserService;
import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.common.entity.model.Store;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.cetcxl.xlpay.common.service.VerifyCodeService;
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

import java.util.Objects;

import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_NO;
import static com.cetcxl.xlpay.common.config.MybatisPlusConfig.PageReq.PARAM_PAGE_SIZE;
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

    @Autowired
    CompanyStoreRelationService companyStoreRelationService;

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
                .socialCreditCode(S_SOCIAL_CREDIT_CODE)
                .businessLicense(0)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/stores/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_UTF8)
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

    @Test
    void listCompanysIsApproval_Success() throws Exception {
        setAuthentication(
                Store.builder()
                        .id(2)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/stores/{storeId}/companys", 2)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .param("isApproval", "true")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total").value(1)
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.records[0].relationStatus").value("APPROVAL")
                );
    }

    @Test
    void listCompanysIsNotApproval_Success() throws Exception {
        setAuthentication(
                Store.builder()
                        .id(1)
                        .build()
        );
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/stores/{storeId}/companys", 1)
                                .param(PARAM_PAGE_NO, "1")
                                .param(PARAM_PAGE_SIZE, "5")
                                .param("isApproval", "false")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.total").value(2)
                )
        ;
    }

    @Test
    void updateCompanyStoreRelation() throws Exception {
        CompanyStoreRelation oldRelation = companyStoreRelationService.getById(2);
        Assert.assertTrue(
                CompanyStoreRelation.Relation.CREDIT_PAY
                        .isClose(oldRelation.getRelation())
        );

        StoreController.StoreCompanyRelationReq req = StoreController.StoreCompanyRelationReq.builder()
                .isApproval(true)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .patch("/stores/{storeId}/company-store-relation/{id}", 2, 2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.status").value(ResBody.Status.OK.name())
                );

        CompanyStoreRelation newRelation = companyStoreRelationService.getById(2);
        Assert.assertTrue(
                CompanyStoreRelation.Relation.CREDIT_PAY
                        .isOpen(newRelation.getRelation())
        );
        Assert.assertTrue(
                Objects.isNull(newRelation.getApplyReleation())
        );

        companyStoreRelationService.lambdaUpdate()
                .set(CompanyStoreRelation::getRelation, 1)
                .set(CompanyStoreRelation::getApplyReleation, 3)
                .set(CompanyStoreRelation::getStatus, CompanyStoreRelation.RelationStatus.APPROVAL)
                .eq(CompanyStoreRelation::getId, 2)
                .update();
    }

    @Test
    void cancelCompanyStoreRelation() throws Exception {
        StoreController.StoreCompanyRelationReq req = StoreController.StoreCompanyRelationReq.builder()
                .isCancel(true)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .delete("/stores/{storeId}/company-store-relation/{id}", 2, 2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.status").value(ResBody.Status.OK.name())
                );

        CompanyStoreRelation newRelation = companyStoreRelationService.getById(2);
        Assert.assertTrue(Objects.isNull(newRelation.getRelation()));

        companyStoreRelationService.lambdaUpdate()
                .set(CompanyStoreRelation::getRelation, 1)
                .set(CompanyStoreRelation::getApplyReleation, 3)
                .set(CompanyStoreRelation::getStatus, CompanyStoreRelation.RelationStatus.APPROVAL)
                .eq(CompanyStoreRelation::getId, 2)
                .update();
    }


    @Test
    void queryStoreInfo_success() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/stores/{storeId}", 1)
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.companyNum").value(2)
                );
    }

    @Test
    void getAllCompanyNames_success() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/stores/{storeId}/company-names", 1)
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data").isArray()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data[0]").value("三十所")
                );
    }

}



