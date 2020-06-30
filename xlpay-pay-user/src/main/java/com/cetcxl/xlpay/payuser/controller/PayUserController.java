package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.common.constants.PatternConstants;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.cetcxl.xlpay.payuser.constants.ResultCode;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.service.PayUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

import static com.cetcxl.xlpay.payuser.constants.ResultCode.PAY_USER_NO_PASSWORD_PAY_IS_EXIST;
import static com.cetcxl.xlpay.payuser.constants.ResultCode.PAY_USER_NO_PASSWORD_PAY_NO_EXIST;

@Validated
@RestController
@Api(tags = "PayUser管理接口")
public class PayUserController extends BaseController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private PayUserService payUserService;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("信链钱包开通请求体")
    public static class UserAddReq {

        @ApiModelProperty(value = "身份证", required = true)
        @NotBlank
        String icNo;

        @ApiModelProperty(value = "支付密码", required = true)
        @Pattern(regexp = PatternConstants.PAY_PASSWORD)
        String password;
    }

    @PostMapping("/pay-user")
    @ApiOperation("信链钱包开通")
    @Transactional
    public ResBody register(@RequestBody @Validated UserAddReq req) {
        PayUser payUser = payUserService
                .getOne(
                        Wrappers.lambdaQuery(PayUser.class)
                                .eq(PayUser::getIcNo, req.getIcNo())
                );
        if (Objects.nonNull(payUser)) {
            return ResBody.error(ResultCode.PAY_USER_EXIST);
        }

        payUser = PayUser.builder()
                .icNo(req.getIcNo())
                .password(passwordEncoder.encode(req.getPassword()))
                .status(PayUser.PayUserStatus.ACTIVE)
                .build();
        payUserService.save(payUser);

        return ResBody.success();
    }

    @PatchMapping(value = "/pay-user/{id}/password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiOperation("钱包支付密码修改")
    public ResBody updatePayPassword(@PathVariable Integer id,
                                     @RequestParam @Pattern(regexp = PatternConstants.PAY_PASSWORD) String oldPassword,
                                     @RequestParam @Pattern(regexp = PatternConstants.PAY_PASSWORD) String newPassword) {

        PayUser payUser = payUserService.getById(id);
        if (!passwordEncoder.matches(oldPassword, payUser.getPassword())) {
            return ResBody.error(ResultCode.PAY_USER_PASSWORD_NOT_EXIST);
        }

        payUserService.update(
                Wrappers.lambdaUpdate(PayUser.class)
                        .eq(PayUser::getId, id)
                        .set(PayUser::getPassword, passwordEncoder.encode(newPassword))
        );

        return ResBody.success();
    }

    @PatchMapping(value = "/pay-user/{id}/secret-free-payment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiOperation("小额免密支付功能")
    public ResBody noPayPassword(@PathVariable Integer id,
                                 @RequestParam @NotNull Boolean isOpen) {

        PayUser payUser = payUserService.getById(id);
        Integer functions = payUser.getFunctions();

        if (isOpen) {
            if (PayUser.PayUserFuntion.NO_PASSWORD_PAY.isOpen(functions)) {
                return ResBody.error(PAY_USER_NO_PASSWORD_PAY_IS_EXIST);
            }
            functions = PayUser.PayUserFuntion.NO_PASSWORD_PAY.open(functions);
        } else {
            if (!PayUser.PayUserFuntion.NO_PASSWORD_PAY.isOpen(functions)) {
                return ResBody.error(PAY_USER_NO_PASSWORD_PAY_NO_EXIST);
            }
            functions = PayUser.PayUserFuntion.NO_PASSWORD_PAY.close(functions);
        }

        payUserService.update(
                Wrappers.lambdaUpdate(PayUser.class)
                        .eq(PayUser::getId, id)
                        .set(PayUser::getFunctions, functions)
        );
        return ResBody.success();
    }
}
