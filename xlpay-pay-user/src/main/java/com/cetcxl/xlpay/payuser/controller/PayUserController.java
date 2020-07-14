package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.common.constants.PatternConstants;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.cetcxl.xlpay.common.service.VerifyCodeService;
import com.cetcxl.xlpay.payuser.constants.ResultCode;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.entity.vo.PayUserVO;
import com.cetcxl.xlpay.payuser.service.CompanyMemberService;
import com.cetcxl.xlpay.payuser.service.CompanyService;
import com.cetcxl.xlpay.payuser.service.PayUserService;
import com.cetcxl.xlpay.payuser.util.ContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;
import java.util.Optional;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.VERIFY_CODE_UNAVAILABLE;
import static com.cetcxl.xlpay.payuser.constants.ResultCode.COMPANY_MEMBER_NOT_EXIST;
import static com.cetcxl.xlpay.payuser.constants.ResultCode.COMPANY_NOT_EXIST;

@Validated
@RestController
@Api(tags = "PayUser管理接口")
public class PayUserController extends BaseController {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    VerifyCodeService verifyCodeService;

    @Autowired
    private PayUserService payUserService;

    @Autowired
    CompanyService companyService;
    @Autowired
    CompanyMemberService companyMemberService;

    @GetMapping("/pay-user")
    @ApiOperation("信链钱包查询")
    @Transactional
    public ResBody<PayUserVO> getPayUser(String socialCreditCode, String icNo) {
        Company company = companyService.lambdaQuery()
                .eq(Company::getSocialCreditCode, socialCreditCode)
                .one();
        if (Objects.isNull(company)) {
            return ResBody.error(COMPANY_NOT_EXIST);
        }

        CompanyMember companyMember = companyMemberService.lambdaQuery()
                .eq(CompanyMember::getIcNo, icNo)
                .eq(CompanyMember::getCompany, company.getId())
                .one();
        if (Objects.isNull(companyMember)) {
            return ResBody.error(COMPANY_MEMBER_NOT_EXIST);
        }

        PayUser payUser = payUserService.lambdaQuery()
                .eq(PayUser::getIcNo, icNo)
                .one();

        return ResBody.success(PayUserVO.of(payUser, PayUserVO.class));
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("信链钱包开通请求体")
    public static class RegisterReq {

        @ApiModelProperty(value = "身份证", required = true)
        @NotBlank
        String icNo;

        @ApiModelProperty
        @NotBlank
        String phone;

        @ApiModelProperty(value = "支付密码", required = true)
        @Pattern(regexp = PatternConstants.PAY_PASSWORD)
        @NotBlank
        String password;
    }

    @PostMapping("/pay-user")
    @ApiOperation("信链钱包开通")
    @Transactional
    public ResBody register(@RequestBody @Validated RegisterReq req) {
        int count = payUserService.count(
                Wrappers.lambdaQuery(PayUser.class)
                        .or(wrapper -> wrapper.eq(PayUser::getIcNo, req.getIcNo()))
                        .or(wrapper -> wrapper.eq(PayUser::getPhone, req.getPhone()))
        );
        if (count > 0) {
            return ResBody.error(ResultCode.PAY_USER_EXIST);
        }

        payUserService.save(
                PayUser.builder()
                        .icNo(req.getIcNo())
                        .password(passwordEncoder.encode(req.getPassword()))
                        .phone(req.getPhone())
                        .status(PayUser.PayUserStatus.ACTIVE)
                        .build()
        );

        return ResBody.success();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("支付密码修改请求体")
    public static class UpdatePayPasswordReq {

        @ApiModelProperty(value = "旧密码", required = true)
        @Pattern(regexp = PatternConstants.PAY_PASSWORD)
        private String oldPassword;

        @ApiModelProperty(value = "新密码", required = true)
        @Pattern(regexp = PatternConstants.PAY_PASSWORD)
        private String newPassword;
    }

    @PatchMapping("/pay-user/password")
    @ApiOperation("钱包支付密码修改")
    public ResBody updatePayPassword(@RequestBody @Validated UpdatePayPasswordReq req) {

        PayUser payUser = payUserService.getById(
                ContextUtil.getUserInfo().getPayUser().getId()
        );
        if (!passwordEncoder.matches(req.oldPassword, payUser.getPassword())) {
            return ResBody.error(ResultCode.PAY_USER_PASSWORD_NOT_CORRECT);
        }

        payUserService.update(
                Wrappers.lambdaUpdate(PayUser.class)
                        .eq(PayUser::getId, payUser.getId())
                        .set(PayUser::getPassword, passwordEncoder.encode(req.newPassword))
        );

        return ResBody.success();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("小额免密支付请求体")
    public static class UpdateNoPayFunctionReq {

        @ApiModelProperty(value = "是否开通小额免密支付", required = true)
        @NotNull
        private boolean isOpen;

    }

    @PatchMapping(value = "/pay-user/secret-free-payment")
    @ApiOperation("小额免密支付功能")
    public ResBody updateNoPayFunction(@RequestBody @Validated UpdateNoPayFunctionReq req) {
        PayUser payUser = payUserService.getById(
                ContextUtil.getUserInfo().getPayUser().getId()
        );

        Integer functions = payUser.getFunctions();
        if (req.isOpen) {
            functions = PayUser.PayUserFuntion.NO_PASSWORD_PAY.open(functions);
        } else {
            functions = PayUser.PayUserFuntion.NO_PASSWORD_PAY.close(functions);
        }

        payUserService.lambdaUpdate()
                .eq(PayUser::getId, payUser.getId())
                .set(PayUser::getFunctions, functions)
                .update();
        return ResBody.success();
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("重置密码发送验证码")
    public static class ResetSendVerifyCodeReq {

        @ApiModelProperty
        @NotBlank
        private String icNo;

    }

    @PostMapping("/pay-user/password/initial/verify-code")
    @ApiOperation("重置密码发送验证码")
    @Transactional
    public ResBody resetSendVerifyCode(@Validated @RequestBody ResetSendVerifyCodeReq req) {
        PayUser payUser = payUserService.lambdaQuery()
                .eq(PayUser::getIcNo, req.getIcNo())
                .one();

        boolean flag = verifyCodeService.sendVerifyCode(payUser.getPhone());
        return flag ? ResBody.success() : ResBody.error();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("重置密码设置新密码")
    public static class ResetPasswordReq {

        @ApiModelProperty
        @NotBlank
        private String verifyCode;
        @ApiModelProperty
        @NotBlank
        @Pattern(regexp = PatternConstants.PAY_PASSWORD)
        private String newPassword;
    }

    @PostMapping("/pay-user/password/initial")
    @ApiOperation("重置密码设置新密码")
    @Transactional
    public ResBody resetPassword(@RequestBody @Validated ResetPasswordReq req) {
        Optional<String> optional = verifyCodeService.getPhone(req.getVerifyCode());
        if (!optional.isPresent()) {
            return ResBody.error(VERIFY_CODE_UNAVAILABLE);
        }

        PayUser payUser = payUserService.lambdaQuery()
                .eq(PayUser::getPhone, optional.get())
                .one();
        payUser.setPassword(passwordEncoder.encode(req.newPassword));

        payUserService.updateById(payUser);
        return ResBody.success();
    }
}
