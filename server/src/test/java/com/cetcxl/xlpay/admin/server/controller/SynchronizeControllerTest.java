package com.cetcxl.xlpay.admin.server.controller;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.admin.server.BaseTest;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCash;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyMemberVO;
import com.cetcxl.xlpay.admin.server.service.WalletCashService;
import com.cetcxl.xlpay.admin.server.service.WalletCreditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.cetcxl.xlpay.admin.server.common.interceptor.SignApiInterceptor.*;

class SynchronizeControllerTest extends BaseTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @Test
    void addCompanyMember() throws Exception {
        SynchronizeController.CompanyMemberAddReq companyMemberAddReq = SynchronizeController
                .CompanyMemberAddReq
                .builder()
                .socialCreditCode(S_CETCXL)
                .icNo(S_TEMP)
                .name(S_TEMP)
                .phone(S_PHONE)
                .department(S_PHONE)
                .employeeNo(S_TEMP)
                .build();

        MvcResult mvcResult = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/synchronize/company-member")
                                .header(SIGN_APP, S_TEST)
                                .header(SIGN_SALT, S_TEMP)
                                .header(SIGN, sign(S_TEMP))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(companyMemberAddReq))
                )
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data.companyName").value("中国电科")
                )
                .andReturn();

        JsonNode jsonNode = mapper.readTree(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        JsonNode data = jsonNode.get("data");
        CompanyMemberVO companyMemberVO = mapper.readValue(data.traverse(), CompanyMemberVO.class);

        WalletCash one = walletCashService
                .getOne(
                        Wrappers.lambdaQuery(WalletCash.class)
                                .eq(WalletCash::getCompanyMember, companyMemberVO.getId())
                );
        Assertions.assertEquals(0, one.getCashBalance().intValue());
    }

    private String sign(String s) {
        Sign sign = SecureUtil.sign(
                SignAlgorithm.SHA1withRSA,
                "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAK2i1AweKSaexWi2iRW+fJky0joDeWALWufNb5XpJac1QxDUxl7xHO6g2EQ3irvmM3borZLRALkuB09tMxmPfpzeztjH8OwvUh7CXOOzNR9wXt+tH5ft0O0A/56XcMd8Y4dwNChXhdwc8fuNWevBvX9Dqw7oEK7BoQrGf3EgsJwFAgMBAAECgYALzohNW/j/skjJdQK0os5FezTbqFl8KWmAskcbJ3kIT1EvIiNsWJ2CBcKO6O6bFkJ7nG4TufiASbFKOlUA25wH9qFoumxFDirmw6Cs77FpOJ5guG28kbW0gO2ZwIr5q/+C+GVWowvqIfUIG+UvnKFiDAWRQ5UNw+EnW68XvT8OhQJBAOENuf+FM/6CFT8OVD4yBCYfdf4f36NMzjNokKhHKqEhuMtn3alIwp9qmQsk0mFkHc+dkRuH9PDkbGAdA+khj6cCQQDFgx1zvribIQ8/DfVTmMJchP/IZvhgI3HuCo8fbuFy7IodlwSX11oRQJGeanAI/iLaFJRFYgZ9rI70yYS7PMxzAkEArRVkfisQwOWEt5kqmybWYAeENKyIz8vLLmh2EKWjGIeZ2v4H0SD/ZaGTEKoCDxrzfnA9YIIglH/pBcZq8op4MwJBAIH9+ltcUemfh4ZLbIQ5jOoRirrdsmiry2cMwgfBFVZrAbfZ1ecNkDS8l1p42QXCJTP8yV0k1/rMoEXRf68vo6sCQCLNINa4EzIa8FFZ3qccUPeZK3Atbpli7BXOR9G/rFqVF90uRnCwwKmiyXj6OufjaKk3ddN0wTIWbsjJrORias4=",
                null
        );

        byte[] bytes = sign.sign(s.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }
}