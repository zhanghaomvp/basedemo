package com.cetcxl.xlpay.admin.entity.vo;

import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import com.cetcxl.xlpay.common.entity.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class CompanyMemberVO extends BaseVO {
    private String companyName;
    private String socialCreditCode;

    private Integer id;
    private String icNo;
    private String name;
    private String phone;
    private String department;
    private String employeeNo;

    private CompanyMember.CompanyMemberStatus status;

    public static CompanyMemberVO of(CompanyMember companyMember, Company company) {
        CompanyMemberVO companyMemberVO = of(companyMember, CompanyMemberVO.class);
        companyMemberVO.setCompanyName(company.getName());
        companyMemberVO.setSocialCreditCode(company.getSocialCreditCode());
        return companyMemberVO;
    }
}
