package com.cetcxl.xlpay.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.admin.constants.ResultCode;
import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.Store;
import com.cetcxl.xlpay.admin.entity.vo.StoreUserVO;
import com.cetcxl.xlpay.admin.service.VerifyCodeService;
import com.cetcxl.xlpay.common.constants.PatternConstants;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.cetcxl.xlpay.common.entity.model.StoreUser;
import com.cetcxl.xlpay.admin.service.StoreService;
import com.cetcxl.xlpay.admin.service.StoreUserService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

@Validated
@RestController
@Api(tags = "商家管理相关接口")
public class StoreController extends BaseController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private StoreUserService storeUserService;
    @Autowired
    private StoreService storeService;

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("商家注册请求体")
    public static class StoreRegisterReq {

        @ApiModelProperty(value = "手机号", required = true)
        @Pattern(regexp = PatternConstants.PHONE)
        String phone;

        @ApiModelProperty(value = "密码", required = true)
        @NotNull
        String password;

        @ApiModelProperty(value = "验证码", required = true)
        @Pattern(regexp = PatternConstants.VERIFY_CODE)
        String verifyCode;

        @ApiModelProperty(value = "商户名称", required = true)
        @NotBlank
        String name;

        @ApiModelProperty(value = "联系人", required = true)
        @NotBlank
        String contact;

        @ApiModelProperty(value = "联系人电话", required = true)
        @Pattern(regexp = PatternConstants.PHONE)
        String contactPhone;

        @ApiModelProperty(required = true)
        @NotBlank
        String address;

        @ApiModelProperty(value = "统一社会信用代码", required = true)
        @NotBlank
        String socialCreditCode;
    }

    @PostMapping("/stores/register")
    @ApiOperation("企业注册")
    @Transactional
    public ResBody<StoreUserVO> register(@RequestBody @Validated StoreRegisterReq req) {
        if (!verifyCodeService.checkVerifyCode(req.getPhone(), req.getVerifyCode())) {
            return ResBody.error(ResultCode.VERIFY_CODE_FAIL);
        }

        StoreUser storeUser = storeUserService.getOne(
                Wrappers.lambdaQuery(StoreUser.class)
                        .eq(StoreUser::getPhone, req.getPhone())
                        .eq(StoreUser::getStatus, StoreUser.StoreUserStatus.ACTIVE)
        );
        if (Objects.nonNull(storeUser)) {
            return ResBody.error(ResultCode.COMPANY_USER_EXIST);
        }

        Store store = storeService.getOne(
                Wrappers.lambdaQuery(Store.class)
                        .eq(Store::getSocialCreditCode, req.getSocialCreditCode())
                        .eq(Store::getStatus, Company.CompanyStatus.ACTIVE)
        );
        if (Objects.nonNull(store)) {
            return ResBody.error(ResultCode.STORE_EXIST);
        }

        store = Store.builder()
                .name(req.getName())
                .contact(req.getContact())
                .phone(req.getContactPhone())
                .address(req.getAddress())
                .socialCreditCode(req.getSocialCreditCode())
                .status(Store.StoreStatus.ACTIVE)
                .build();
        storeService.save(store);

        storeUser = StoreUser.builder()
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .store(store.getId())
                .status(StoreUser.StoreUserStatus.ACTIVE)
                .build();
        storeUserService.save(storeUser);

        StoreUserVO storeUserVO = StoreUserVO.of(storeUser, store);
        return ResBody.success(storeUserVO);
    }

}
