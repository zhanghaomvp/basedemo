package com.cetcxl.xlpay.admin.rpc;

import com.cetcxl.xlpay.BaseTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrustlinkDataRpcServiceTest extends BaseTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TrustlinkDataRpcService trustlinkDataRpcService;

    @Test
    void getCompanyInfo() throws JsonProcessingException {
        WireMock.stubFor(
                WireMock
                        .get(
                                WireMock
                                        .urlPathEqualTo("/pay-trustlink-data/company/getCompanyInfo")
                        )
                        .willReturn(
                                WireMock
                                        .okJson(
                                                objectMapper.writeValueAsString(
                                                        TrustlinkDataRpcService.CompanyInfo.builder()
                                                                .organizationName(S_CETCXL)
                                                                .build()
                                                )
                                        )
                        )

        );

        Optional<TrustlinkDataRpcService.CompanyInfo> companyInfo = trustlinkDataRpcService.getCompanyInfo("05050001");

        assertTrue(companyInfo.isPresent());
        assertEquals(S_CETCXL, companyInfo.get().getOrganizationName());
    }

    @Test
    void syncCompanyEmployee() {
        WireMock.stubFor(
                WireMock
                        .get(
                                WireMock
                                        .urlPathEqualTo("/pay-trustlink-data/company/syncCompanyEmployee")
                        )
                        .willReturn(
                                WireMock
                                        .ok()
                        )
        );

        trustlinkDataRpcService.syncCompanyEmployee(S_TEMP);
    }
}