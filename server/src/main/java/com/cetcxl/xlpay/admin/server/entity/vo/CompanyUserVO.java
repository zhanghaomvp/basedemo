package com.cetcxl.xlpay.admin.server.entity.vo;

import com.cetcxl.xlpay.admin.server.entity.model.Company;
import com.cetcxl.xlpay.admin.server.entity.model.CompanyUser;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel
public class CompanyUserVO extends BaseVO {
    private Integer id;
    private String phone;
    private String companyName;
    private String socialCreditCode;
    private LocalDateTime created;

    public static CompanyUserVO of(CompanyUser companyUser, Company company) {
        CompanyUserVO CompanyUserVO = of(companyUser, CompanyUserVO.class);
        CompanyUserVO.setCompanyName(company.getName());
        CompanyUserVO.setSocialCreditCode(company.getSocialCreditCode());
        return CompanyUserVO;
    }
}