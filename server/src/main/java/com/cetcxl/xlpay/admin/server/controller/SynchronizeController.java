package com.cetcxl.xlpay.admin.server.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.admin.server.common.constants.PatternConstants;
import com.cetcxl.xlpay.admin.server.common.controller.BaseController;
import com.cetcxl.xlpay.admin.server.common.interceptor.annotation.SignApi;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import com.cetcxl.xlpay.admin.server.entity.model.Company;
import com.cetcxl.xlpay.admin.server.entity.model.CompanyMember;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyMemberVO;
import com.cetcxl.xlpay.admin.server.service.CompanyMemberService;
import com.cetcxl.xlpay.admin.server.service.CompanyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Objects;

import static com.cetcxl.xlpay.admin.server.constants.ResultCode.COMPANY_MEMBER_EXIST;
import static com.cetcxl.xlpay.admin.server.constants.ResultCode.COMPANY_NOT_EXIST;

@Validated
@RestController
@Api(tags = "数据同步接口")
public class SynchronizeController extends BaseController {
    @Autowired
    CompanyService companyService;
    @Autowired
    CompanyMemberService companyMemberService;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("企业成员添加req")
    public static class CompanyMemberAddReq {
        @ApiModelProperty(value = "统一社会信用代码", required = true)
        @NotBlank
        String socialCreditCode;

        @ApiModelProperty(value = "身份证号码", required = true)
        @NotBlank
        String icNo;

        @ApiModelProperty(value = "姓名", required = true)
        @NotBlank
        String name;

        @ApiModelProperty(value = "手机号", required = true)
        @Pattern(regexp = PatternConstants.PHONE)
        String phone;


        @ApiModelProperty(value = "部门", required = true)
        @NotBlank
        String department;

        @ApiModelProperty(value = "工号", required = true)
        @NotBlank
        String employeeNo;
    }

    @PostMapping("/synchronize/company-member")
    @ApiOperation("")
    @SignApi
    public ResBody<CompanyMemberVO> addCompanyMember(@RequestBody @Validated CompanyMemberAddReq req) {
        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getSocialCreditCode, req.getSocialCreditCode())
                        .eq(Company::getStatus, Company.CompanyStatus.ACTIVE)
        );

        if (Objects.isNull(company)) {
            return ResBody.error(COMPANY_NOT_EXIST);
        }

        CompanyMember member = companyMemberService
                .getOne(
                        Wrappers.lambdaQuery(CompanyMember.class)
                                .eq(CompanyMember::getIcNo, req.getIcNo())
                                .eq(CompanyMember::getCompany, company.getId())
                );

        if (Objects.nonNull(member)) {
            return ResBody.error(COMPANY_MEMBER_EXIST);
        }

        CompanyMember companyMember = CompanyMember.builder()
                .company(company.getId())
                .icNo(req.getIcNo())
                .name(req.getName())
                .phone(req.getPhone())
                .department(req.getDepartment())
                .employeeNo(req.getEmployeeNo())
                .status(CompanyMember.CompanyMemberStatus.ACTIVE)
                .build();

        CompanyMemberVO companyMemberVO = CompanyMemberVO
                .of(
                        companyMemberService.addCompanyMember(companyMember),
                        company
                );

        return ResBody.success(companyMemberVO);
    }
}
