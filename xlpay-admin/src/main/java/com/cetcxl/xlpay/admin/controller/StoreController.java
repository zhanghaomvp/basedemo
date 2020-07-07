package com.cetcxl.xlpay.admin.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.constants.ResultCode;
import com.cetcxl.xlpay.admin.dao.StoreMapper;
import com.cetcxl.xlpay.admin.entity.vo.StoreUserVO;
import com.cetcxl.xlpay.admin.entity.vo.StoreVO;
import com.cetcxl.xlpay.admin.service.*;
import com.cetcxl.xlpay.admin.util.ContextUtil;
import com.cetcxl.xlpay.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.common.constants.PatternConstants;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.common.entity.model.Store;
import com.cetcxl.xlpay.common.entity.model.StoreUser;
import com.cetcxl.xlpay.common.rpc.ResBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

import static com.cetcxl.xlpay.admin.constants.ResultCode.COMPANY_STORE_RELATION_WORKING;
import static com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation.RelationStatus.WORKING;

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
    private CompanyStoreRelationService companyStoreRelationService;

    @Autowired
    StoreMapper storeMapper;

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

        @ApiModelProperty(value = "调用上传接口返回的营业执照对应attachment_id", required = true)
        @NotNull
        Integer businessLicense;
    }

    @PostMapping("/stores/register")
    @ApiOperation("商家注册")
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
                .businessLicense(req.getBusinessLicense())
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


    @Data
    @ApiModel
    public static class ListCompanysReq extends MybatisPlusConfig.PageReq {
        @ApiModelProperty(value = "商家名称")
        String companyName;

        @ApiModelProperty(value = "当前企业与商家关联关系状态")
        private Boolean isApproval;
    }

    @GetMapping("/stores/{storeId}/companys")
    @ApiOperation("企业查询")
    public ResBody<IPage<StoreMapper.StoreCompanyDTO>> listCompanys(ListCompanysReq req) {
        UserDetailServiceImpl.UserInfo user = ContextUtil.getUserInfo();
        if (req.getIsApproval()) {
            return ResBody
                    .success(
                            storeMapper
                                    .listStoreCompanyIsApproval(
                                            new Page(req.getPageNo(), req.getPageSize()),
                                            user.getStore().getId(),
                                            req.getCompanyName()
                                    )
                    );
        } else {
            return ResBody
                    .success(
                            storeMapper
                                    .listStoreCompanyNotApproval(
                                            new Page(req.getPageNo(), req.getPageSize()),
                                            user.getStore().getId(),
                                            req.getCompanyName()
                                    )
                    );
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class StoreCompanyRelationReq {
        @ApiModelProperty(required = true, value = "是否通过")
        @NotNull(groups = UpdateCompanyStoreRelationGroup.class)
        Boolean isApproval;
        @ApiModelProperty(required = true, value = "是否通过")
        @NotNull(groups = CancelCompanyStoreRelationGroup.class)
        Boolean isCancel;
    }

    interface UpdateCompanyStoreRelationGroup {
    }

    @PatchMapping("/stores/{storeId}/company-store-relation/{id}")
    @ApiOperation("商家确认企业商家授信")
    @Transactional
    public ResBody updateCompanyStoreRelation(@PathVariable Integer id,
                                              @Validated(UpdateCompanyStoreRelationGroup.class)
                                              @RequestBody StoreCompanyRelationReq req) {
        CompanyStoreRelation companyStoreRelation = companyStoreRelationService.getById(id);
        if (WORKING == companyStoreRelation.getStatus()) {
            return ResBody.error(COMPANY_STORE_RELATION_WORKING);
        }

        if (req.getIsApproval()) {
            companyStoreRelationService.lambdaUpdate()
                    .set(CompanyStoreRelation::getApplyReleation, null)
                    .set(CompanyStoreRelation::getRelation, companyStoreRelation.getApplyReleation())
                    .set(CompanyStoreRelation::getStatus, WORKING)
                    .eq(CompanyStoreRelation::getId, companyStoreRelation.getId())
                    .update();
        } else {
            companyStoreRelationService.lambdaUpdate()
                    .set(CompanyStoreRelation::getApplyReleation, null)
                    .set(CompanyStoreRelation::getStatus, WORKING)
                    .eq(CompanyStoreRelation::getId, companyStoreRelation.getId())
                    .update();
        }

        return ResBody.success();
    }

    interface CancelCompanyStoreRelationGroup {
    }

    @DeleteMapping("/stores/{storeId}/company-store-relation/{id}")
    @ApiOperation("商家取消企业商家授信")
    @Transactional
    public ResBody cancelCompanyStoreRelation(@PathVariable Integer id,
                                              @Validated(CancelCompanyStoreRelationGroup.class)
                                              @RequestBody StoreCompanyRelationReq req) {
        companyStoreRelationService.lambdaUpdate()
                .set(CompanyStoreRelation::getApplyReleation, null)
                .set(CompanyStoreRelation::getRelation, null)
                .set(CompanyStoreRelation::getStatus, WORKING)
                .eq(CompanyStoreRelation::getId, id)
                .update();
        return ResBody.success();
    }


    @GetMapping("/stores/{storeId}")
    @ApiOperation("查询商家账户信息")
    public ResBody<StoreVO> queryStoreInfo(@PathVariable @Pattern(regexp =
            PatternConstants.MUST_NUMBER) String storeId) {
        Store store = storeService.getById(storeId);

        StoreVO infoVO = StoreVO.of(store, StoreVO.class);

        infoVO.setCompanyNum(
                companyStoreRelationService.lambdaQuery()
                        .eq(CompanyStoreRelation::getStore, store.getId())
                        .count()
        );

        return ResBody.success(infoVO);
    }


    @GetMapping("/stores/{storeId}/qr-code")
    @ApiOperation("商家获取二维码接口")
    public void generateQrCode(HttpServletResponse response, @PathVariable Integer storeId) throws Exception {
        resolvePicResponseHeader(response, "qr-code");
        QrCodeUtil.generate(
                storeService.getQrCodeContent(storeId),
                300,
                300,
                StringUtils.EMPTY,
                response.getOutputStream()
        );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class ResetloginPasswordReq {
        @ApiModelProperty(required = true, value = "登录名(手机号)")
        @NotNull
        private String phone;

        @ApiModelProperty(required = true, value = "新密码")
        @NotBlank
        private String newPassword;

        @ApiModelProperty(value = "验证码", required = true)
        @Pattern(regexp = PatternConstants.VERIFY_CODE)
        String verifyCode;
    }

    @PatchMapping("stores/store-user/password")
    @ApiOperation("重置商家登录密码")
    public ResBody resetLoginPassword(@Validated @RequestBody StoreController.ResetloginPasswordReq req) {

        if (!verifyCodeService.checkVerifyCode(req.getPhone(), req.getVerifyCode())) {
            return ResBody.error(ResultCode.VERIFY_CODE_FAIL);
        }
        StoreUser storeUser = storeUserService.lambdaQuery()
                .eq(StoreUser::getPhone, req.getPhone())
                .eq(StoreUser::getStatus, StoreUser.StoreUserStatus.ACTIVE).one();

        if (ObjectUtil.isNull(storeUser)) {
            return ResBody.error(ResultCode.STORE_NOT_EXIST);
        }

        storeUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
        storeUserService.updateById(storeUser);
        return ResBody.success();
    }

}
