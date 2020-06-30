package com.cetcxl.xlpay.admin.rpc;

import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.RPC_ERROR;

@Service
@Slf4j
public class TrustlinkDataRpcService {
    @Value("${http.trust-link-data.url}")
    private String url;
    @Autowired
    private RestTemplate restTemplate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private String organizationId;
        private String organizationCreditId;
        private String organizationName;
        private String organizationTel;
    }

    public Optional<CompanyInfo> getCompanyInfo(String socialCreditCode) {
        ResponseEntity<CompanyInfo> responseEntity = restTemplate
                .getForEntity(
                        url + "/company/getCompanyInfo?creditId={socialCreditCode}",
                        CompanyInfo.class,
                        socialCreditCode
                );

        if (responseEntity.getStatusCode().isError()) {
            log.error("TrustlinkDataRpcService getCompanyInfo error : {} ", responseEntity);
            throw new BaseRuntimeException(RPC_ERROR);
        }
        return Optional.ofNullable(responseEntity.getBody());
    }

    public void syncCompanyEmployee(String socialCreditCode) {
        ResponseEntity<String> responseEntity = restTemplate
                .getForEntity(
                        url + "/company/syncCompanyEmployee?creditId={socialCreditCode}",
                        String.class,
                        socialCreditCode
                );

        if (responseEntity.getStatusCode().isError()) {
            log.error("TrustlinkDataRpcService syncCompanyEmployee error : {} ", responseEntity);
            throw new BaseRuntimeException(RPC_ERROR);
        }
    }
}
