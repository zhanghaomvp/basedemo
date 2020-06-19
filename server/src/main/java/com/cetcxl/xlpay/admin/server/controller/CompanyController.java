package com.cetcxl.xlpay.admin.server.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.admin.server.common.constants.PatternConstants;
import com.cetcxl.xlpay.admin.server.common.controller.BaseController;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import com.cetcxl.xlpay.admin.server.constants.ResultCode;
import com.cetcxl.xlpay.admin.server.entity.model.Company;
import com.cetcxl.xlpay.admin.server.entity.model.CompanyUser;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyUserVO;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyVO;
import com.cetcxl.xlpay.admin.server.service.CompanyService;
import com.cetcxl.xlpay.admin.server.service.CompanyUserService;
import com.cetcxl.xlpay.admin.server.service.VerifyCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Objects;

@Validated
@RestController
@Api(tags = "企业管理相关接口")
public class CompanyController extends BaseController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private CompanyUserService companyUserService;
    @Autowired
    private CompanyService companyService;

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Data
    @ApiModel("用户注册请求体")
    public static class CompanyRegisterReq {

        @ApiModelProperty(value = "手机号", required = true)
        @Pattern(regexp = PatternConstants.PHONE)
        String phone;

        @ApiModelProperty(value = "密码", required = true)
        @NotBlank
        String password;

        @ApiModelProperty(value = "验证码", required = true)
        @Pattern(regexp = PatternConstants.VERIFY_CODE)
        String verifyCode;

        @ApiModelProperty(value = "企业名称", required = true)
        @NotBlank
        String name;

        @ApiModelProperty(value = "统一社会信用代码", required = true)
        @NotBlank
        String socialCreditCode;

    }

    @PostMapping("/company/register")
    @ApiOperation("企业注册")
    @Transactional
    public ResBody<CompanyUserVO> register(@RequestBody @Validated CompanyRegisterReq req) {
        if (!verifyCodeService.checkVerifyCode(req.getPhone(), req.getVerifyCode())) {
            return ResBody.error(ResultCode.VERIFY_CODE_FAIL);
        }

        CompanyUser companyUser = companyUserService.getOne(Wrappers.lambdaQuery(CompanyUser.class)
                .eq(CompanyUser::getPhone, req.getPhone())
                .eq(CompanyUser::getStatus, CompanyUser.CompanyUserStatus.ACTIVE));

        if (Objects.nonNull(companyUser)) {
            return ResBody.error(ResultCode.COMPANY_USER_EXIST);
        }

        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getSocialCreditCode, req.getSocialCreditCode())
                        .eq(Company::getStatus, Company.CompanyStatus.ACTIVE)
        );

        if (Objects.isNull(company)) {
            company = Company.builder()
                    .name(req.getName())
                    .socialCreditCode(req.getSocialCreditCode())
                    .phone(req.getPhone())
                    .functions(Company.CompanyFuntion.MEMBER_PAY.addFuntion(0))
                    .status(Company.CompanyStatus.ACTIVE)
                    .build();
            companyService.save(company);
        }

        companyUser = CompanyUser.builder()
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .company(company.getId())
                .status(CompanyUser.CompanyUserStatus.ACTIVE)
                .build();
        companyUserService.save(companyUser);

        CompanyUserVO companyUserVO = CompanyUserVO.of(companyUser, company);
        return ResBody.success(companyUserVO);
    }

    @GetMapping("/company/{companyId}")
    @ApiOperation("企业详情")
    public ResBody<CompanyVO> detail(@PathVariable @Pattern(regexp = PatternConstants.MUST_NUMBER) String companyId) {
        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getId, companyId)
                        .eq(Company::getStatus, Company.CompanyStatus.ACTIVE)
        );

        if (Objects.isNull(company)) {
            return ResBody.error(ResultCode.COMPANY_NOT_EXIST);
        }

        CompanyVO companyVO = CompanyVO.of(company, CompanyVO.class);
        return ResBody.success(companyVO);
    }

}
