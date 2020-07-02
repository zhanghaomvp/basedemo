package com.cetcxl.xlpay.admin.entity.vo;

import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyUser;
import com.cetcxl.xlpay.common.entity.vo.BaseVO;
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
        CompanyUserVO companyUserVO = of(companyUser, CompanyUserVO.class);
        companyUserVO.setCompanyName(company.getName());
        companyUserVO.setSocialCreditCode(company.getSocialCreditCode());
        return companyUserVO;
    }
}
