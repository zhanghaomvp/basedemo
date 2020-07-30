package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.common.constants.CommonResultCode;
import com.cetcxl.xlpay.common.constants.PatternConstants;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.common.service.VerifyCodeService;
import com.cetcxl.xlpay.payuser.constants.ResultCode;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.entity.vo.PayUserVO;
import com.cetcxl.xlpay.payuser.service.CompanyMemberService;
import com.cetcxl.xlpay.payuser.service.CompanyService;
import com.cetcxl.xlpay.payuser.service.PayUserService;
import com.cetcxl.xlpay.payuser.service.UserDetailServiceImpl;
import com.cetcxl.xlpay.payuser.util.ContextUtil;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;
import java.util.Optional;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.VERIFY_CODE_UNAVAILABLE;
import static com.cetcxl.xlpay.payuser.constants.ResultCode.*;

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
    @ApiOperation("信链钱包当前身份是否支持以及钱包是否开通接口")
    @ApiResponses({
            @ApiResponse(code = 3001, message = "绑定企业不存在"),
            @ApiResponse(code = 3002, message = "绑定企业成员不存在"),
            @ApiResponse(code = 2002, message = "信链钱包未开通")
    })
    public PayUserVO getPayUser(String socialCreditCode, String icNo) {
        Company company = companyService.lambdaQuery()
                .eq(Company::getSocialCreditCode, socialCreditCode)
                .one();
        if (Objects.isNull(company)) {
            throw new BaseRuntimeException(COMPANY_NOT_EXIST);
        }

        CompanyMember companyMember = companyMemberService.lambdaQuery()
                .eq(CompanyMember::getIcNo, icNo)
                .eq(CompanyMember::getCompany, company.getId())
                .eq(CompanyMember::getStatus, CompanyMember.CompanyMemberStatus.ACTIVE)
                .one();
        if (Objects.isNull(companyMember)) {
            throw new BaseRuntimeException(COMPANY_MEMBER_NOT_EXIST);
        }

        PayUser payUser = payUserService.lambdaQuery()
                .eq(PayUser::getIcNo, icNo)
                .one();
        if (Objects.isNull(payUser)) {
            throw new BaseRuntimeException(PAY_USER_NOT_EXIST);
        }

        PayUserVO payUserVO = PayUserVO.of(payUser, PayUserVO.class);

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        if (principal instanceof UserDetailServiceImpl.PayUserInfo) {
            payUserVO.setCookieAlive(true);
        }

        return payUserVO;
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
    public void register(@RequestBody @Validated RegisterReq req) {
        int count = payUserService.count(
                Wrappers.lambdaQuery(PayUser.class)
                        .or(wrapper -> wrapper.eq(PayUser::getIcNo, req.getIcNo()))
                        .or(wrapper -> wrapper.eq(PayUser::getPhone, req.getPhone()))
        );
        if (count > 0) {
            throw new BaseRuntimeException(ResultCode.PAY_USER_EXIST);
        }

        payUserService.save(
                PayUser.builder()
                        .icNo(req.getIcNo())
                        .password(passwordEncoder.encode(req.getPassword()))
                        .phone(req.getPhone())
                        .status(PayUser.PayUserStatus.ACTIVE)
                        .build()
        );
    }

    @GetMapping("/pay-user/heartbeat")
    @ApiOperation("C端用户登录状态校验状态接口")
    @ApiResponses({
            @ApiResponse(code = 9003, message = "验证失败 请重新登录"),
    })
    public void heartbeat() {
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

    @PostMapping("/pay-user/password")
    @ApiOperation("钱包支付密码修改")
    public void updatePayPassword(@RequestBody @Validated UpdatePayPasswordReq req) {

        PayUser payUser = payUserService.getById(
                ContextUtil.getUserInfo().getPayUser().getId()
        );
        if (!passwordEncoder.matches(req.oldPassword, payUser.getPassword())) {
            throw new BaseRuntimeException(ResultCode.PAY_USER_PASSWORD_NOT_CORRECT);
        }

        payUserService.update(
                Wrappers.lambdaUpdate(PayUser.class)
                        .eq(PayUser::getId, payUser.getId())
                        .set(PayUser::getPassword, passwordEncoder.encode(req.newPassword))
        );
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

        @ApiModelProperty(value = "支付密码")
        @NotBlank
        private String password;

    }

    @PostMapping(value = "/pay-user/secret-free-payment")
    @ApiOperation("小额免密支付功能")
    public void updateNoPayFunction(@RequestBody @Validated UpdateNoPayFunctionReq req) {
        PayUser payUser = payUserService.getById(
                ContextUtil.getUserInfo().getPayUser().getId()
        );
        if (!passwordEncoder.matches(req.getPassword(), payUser.getPassword())) {
            throw new BaseRuntimeException(ResultCode.PAY_USER_PASSWORD_NOT_CORRECT);
        }

        Integer functions = payUser.getFunctions();
        if (Objects.isNull(functions)) {
            functions = 0;
        }

        if (req.isOpen) {
            functions = PayUser.PayUserFuntion.NO_PASSWORD_PAY.open(functions);
        } else {
            functions = PayUser.PayUserFuntion.NO_PASSWORD_PAY.close(functions);
        }

        payUserService.lambdaUpdate()
                .eq(PayUser::getId, payUser.getId())
                .set(PayUser::getFunctions, functions)
                .update();
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
    public void resetSendVerifyCode(@Validated @RequestBody ResetSendVerifyCodeReq req) {
        PayUser payUser = payUserService.lambdaQuery()
                .eq(PayUser::getIcNo, req.getIcNo())
                .one();

        boolean flag = verifyCodeService.sendVerifyCode(payUser.getPhone());
        if (!flag) {
            throw new BaseRuntimeException(CommonResultCode.RPC_ERROR);
        }
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
    public void resetPassword(@RequestBody @Validated ResetPasswordReq req) {
        Optional<String> optional = verifyCodeService.getPhone(req.getVerifyCode());
        if (!optional.isPresent()) {
            throw new BaseRuntimeException(VERIFY_CODE_UNAVAILABLE);
        }

        PayUser payUser = payUserService.lambdaQuery()
                .eq(PayUser::getPhone, optional.get())
                .one();
        payUser.setPassword(passwordEncoder.encode(req.newPassword));

        payUserService.updateById(payUser);
    }
}
