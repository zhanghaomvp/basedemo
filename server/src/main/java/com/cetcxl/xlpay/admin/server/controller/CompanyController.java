package com.cetcxl.xlpay.admin.server.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.admin.server.common.constants.PatternConstants;
import com.cetcxl.xlpay.admin.server.common.controller.BaseController;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import com.cetcxl.xlpay.admin.server.constants.ResultCode;
import com.cetcxl.xlpay.admin.server.entity.conveter.CompanyConverter;
import com.cetcxl.xlpay.admin.server.entity.model.Company;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyVO;
import com.cetcxl.xlpay.admin.server.service.CompanyService;
import com.cetcxl.xlpay.admin.server.service.VerifyCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

@Validated
@RestController
@Api(tags = "企业管理相关接口")
public class CompanyController extends BaseController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private CompanyConverter companyConverter;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Data
    @ApiModel("用户注册请求体")
    public static class CompanyRegisterReq {

        @ApiModelProperty(value = "企业名称", required = true)
        @NotEmpty
        String name;

        @ApiModelProperty(value = "密码", required = true)
        @NotNull
        String password;

        @ApiModelProperty(value = "手机号", required = true)
        @Pattern(regexp = "^(1[3-9])\\d{9}$")
        String phone;

        @ApiModelProperty(value = "统一社会信用代码", required = true)
        @NotNull
        String socialCreditCode;

        @ApiModelProperty(value = "验证码", required = true)
        @Pattern(regexp = "^\\d{6}$")
        String verifyCode;
    }

    @PostMapping("/company/register")
    @ApiOperation("企业注册")
    public ResBody<CompanyVO> register(@RequestBody @Validated CompanyRegisterReq req) {
        if (!verifyCodeService.checkVerifyCode(req.getPhone(), req.getVerifyCode())) {
            return ResBody.error(ResultCode.VERIFY_CODE_FAIL);
        }

        Company one = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getLoginName, req.getPhone())
                        .ne(Company::getStatus, Company.CompanyStatusEnum.DISABLE)
        );

        if (Objects.nonNull(one)) {
            return ResBody.error(ResultCode.COMPANY_EXIST);
        }

        Company company = Company.builder()
                .name(req.getName())
                .loginName(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .socialCreditCode(req.getSocialCreditCode())
                .phone(req.getPhone())
                .functions(Company.CompanyFuntionEnum.MEMBER_PAY.addFuntion(0))
                .status(Company.CompanyStatusEnum.ACTIVE)
                .build();
        companyService.save(company);

        return ResBody.success(companyConverter.toCompanyVO(company));
    }

    @GetMapping("/company/{companyId}")
    @ApiOperation("企业详情")
    public ResBody<CompanyVO> detail(@PathVariable @Pattern(regexp = PatternConstants.REGEX_MUST_NUMBER) String companyId) {
        Company one = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getId, companyId)
                        .ne(Company::getStatus, Company.CompanyStatusEnum.DISABLE)
        );

        if (Objects.isNull(one)) {
            return ResBody.error(ResultCode.COMPANY_NOT_EXIST);
        }

        return ResBody.success(companyConverter.toCompanyVO(one));
    }


}
