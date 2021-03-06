package com.cetcxl.xlpay.admin.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.constants.ResultCode;
import com.cetcxl.xlpay.admin.dao.CompanyMemberMapper;
import com.cetcxl.xlpay.admin.dao.StoreMapper;
import com.cetcxl.xlpay.admin.entity.model.CompanyUser;
import com.cetcxl.xlpay.admin.entity.vo.CompanyStoreRelationVO;
import com.cetcxl.xlpay.admin.entity.vo.CompanyUserVO;
import com.cetcxl.xlpay.admin.entity.vo.CompanyVO;
import com.cetcxl.xlpay.admin.rpc.TrustlinkDataRpcService;
import com.cetcxl.xlpay.admin.service.*;
import com.cetcxl.xlpay.admin.util.ContextUtil;
import com.cetcxl.xlpay.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.common.constants.PatternConstants;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.common.service.VerifyCodeService;
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
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.cetcxl.xlpay.admin.constants.ResultCode.COMPANY_STORE_RELATION_APPROVING;
import static com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation.Relation.CASH_PAY;
import static com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation.Relation.CREDIT_PAY;
import static com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation.RelationStatus.APPROVAL;
import static com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation.RelationStatus.WORKING;

@Validated
@RestController
@Api(tags = "企业管理相关接口")
public class CompanyController extends BaseController {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    TrustlinkDataRpcService trustlinkDataRpcService;

    @Autowired
    private CompanyUserService companyUserService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    CompanyMemberService companyMemberService;

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Autowired
    StoreMapper storeMapper;

    @Autowired
    CompanyMemberMapper companyMemberMapper;

    @Autowired
    CompanyStoreRelationService companyStoreRelationService;

    @Data
    @ApiModel("用户注册请求体")
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompanyRegisterReq {

        @ApiModelProperty(value = "手机号", required = true)
        @Pattern(regexp = PatternConstants.PHONE)
        String phone;

        @ApiModelProperty(value = "密码", required = true)
        @NotBlank
        String password;

        @ApiModelProperty(value = "验证码", required = true)
        @Pattern(regexp = PatternConstants.VERIFY_CODE)
        @NotBlank
        String verifyCode;

        @ApiModelProperty(value = "企业名称", required = true)
        @NotBlank
        String name;

        @ApiModelProperty(value = "统一社会信用代码", required = true)
        @Pattern(regexp = PatternConstants.SOCIAL_CREDIT_CODE)
        @NotBlank
        String socialCreditCode;

    }

    @PostMapping("/companys/register")
    @ApiOperation("企业注册")
    @Transactional
    public CompanyUserVO register(@RequestBody @Validated final CompanyRegisterReq req) {
        if (!verifyCodeService.checkVerifyCode(req.getVerifyCode(), req.getPhone())) {
            throw new BaseRuntimeException(ResultCode.VERIFY_CODE_FAIL);
        }

        CompanyUser companyUser = companyUserService.getOne(Wrappers.lambdaQuery(CompanyUser.class)
                .eq(CompanyUser::getPhone, req.getPhone())
                .eq(CompanyUser::getStatus, CompanyUser.CompanyUserStatus.ACTIVE));

        if (Objects.nonNull(companyUser)) {
            throw new BaseRuntimeException(ResultCode.COMPANY_USER_EXIST);
        }

        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getSocialCreditCode, req.getSocialCreditCode())
                        .eq(Company::getStatus, Company.CompanyStatus.ACTIVE)
        );

        if (Objects.isNull(company)) {
            Optional<TrustlinkDataRpcService.CompanyInfo> optionalCompanyInfo =
                    trustlinkDataRpcService.getCompanyInfo(req.getSocialCreditCode());

            if (!optionalCompanyInfo.isPresent()) {
                throw new BaseRuntimeException(ResultCode.COMPANY_NOT_EXIST);
            }

            TrustlinkDataRpcService.CompanyInfo companyInfo = optionalCompanyInfo.get();
            if (!req.getName().equals(companyInfo.getOrganizationName())) {
                throw new BaseRuntimeException(ResultCode.COMPANY_NOT_EXIST);
            }

            company = Company.builder()
                    .name(companyInfo.getOrganizationName())
                    .socialCreditCode(companyInfo.getOrganizationCreditId())
                    .phone(companyInfo.getOrganizationTel())
                    .email(companyInfo.getOrganizationEmail())
                    .functions(Company.CompanyFuntion.MEMBER_PAY.open(0))
                    .status(Company.CompanyStatus.ACTIVE)
                    .build();
            companyService.save(company);

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            trustlinkDataRpcService.syncCompanyEmployee(req.getSocialCreditCode());
                        }
                    }
            );
        }

        companyUser = CompanyUser.builder()
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .company(company.getId())
                .status(CompanyUser.CompanyUserStatus.ACTIVE)
                .build();
        companyUserService.save(companyUser);

        return CompanyUserVO.of(companyUser, company);
    }

    @GetMapping("/companys/{companyId}")
    @ApiOperation("企业详情")
    public CompanyVO detail(@PathVariable @Pattern(regexp = PatternConstants.MUST_NUMBER) String companyId) {
        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getId, companyId)
                        .eq(Company::getStatus, Company.CompanyStatus.ACTIVE)
        );

        if (Objects.isNull(company)) {
            throw new BaseRuntimeException(ResultCode.COMPANY_NOT_EXIST);
        }
        CompanyVO companyVO = CompanyVO.of(company, CompanyVO.class);

        companyVO.setMemberCount(
                companyMemberService.count(
                        Wrappers.lambdaQuery(CompanyMember.class)
                                .eq(CompanyMember::getCompany, company.getId())
                )
        );
        companyVO.setStoreCount(
                companyStoreRelationService.count(
                        Wrappers.lambdaQuery(CompanyStoreRelation.class)
                                .eq(CompanyStoreRelation::getCompany, company.getId())
                                .gt(CompanyStoreRelation::getRelation, 0)
                )
        );

        return companyVO;
    }

    @Data
    @ApiModel
    public static class ListStoresReq extends MybatisPlusConfig.PageReq {
        @NotNull
        @ApiModelProperty(value = "是否与商家已存在关联关系")
        Boolean hasRelation;

        @ApiModelProperty(value = "商家名称")
        String name;
    }

    @GetMapping("/companys/{companyId}/stores")
    @ApiOperation("商家查询")
    public IPage<StoreMapper.CompanyStoreDTO> listStores(ListStoresReq req) {
        UserDetailServiceImpl.UserInfo user = ContextUtil.getUserInfo();

        if (req.hasRelation) {
            return storeMapper
                    .listCompanyStoresWithRelation(
                            new Page(req.getPageNo(), req.getPageSize()),
                            user.getCompany().getId(),
                            req.getName()
                    );
        } else {
            return storeMapper
                    .listCompanyStoresNotWithRelation(
                            new Page(req.getPageNo(), req.getPageSize()),
                            user.getCompany().getId(),
                            req.getName()

                    );
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class CompanyStoreRelationReq {
        @ApiModelProperty(required = true, value = "是否开通余额消费授信")
        @NotNull
        Boolean canCashPay;
        @ApiModelProperty(required = true, value = "是否开通信用消费授信")
        @NotNull
        Boolean canCreditPay;
    }

    @PostMapping("/companys/{companyId}/stores/{storeId}/company-store-relation")
    @ApiOperation("增加企业商家授信")
    @Transactional
    public CompanyStoreRelationVO addCompanyStoreRelation(@PathVariable Integer storeId,
                                                          @Validated @RequestBody CompanyStoreRelationReq req) {
        UserDetailServiceImpl.UserInfo user = ContextUtil.getUserInfo();
        Integer companyId = user.getCompany().getId();

        CompanyStoreRelation one = companyStoreRelationService.getOne(
                Wrappers.lambdaQuery(CompanyStoreRelation.class)
                        .eq(CompanyStoreRelation::getCompany, companyId)
                        .eq(CompanyStoreRelation::getStore, storeId)
        );

        if (Objects.nonNull(one)) {
            throw new BaseRuntimeException(ResultCode.COMPANY_STORE_RELATION_EXIST);
        }

        int applyReleation = 0;
        if (req.getCanCashPay()) {
            applyReleation = CASH_PAY.open(applyReleation);
        }
        if (req.getCanCreditPay()) {
            applyReleation = CREDIT_PAY.open(applyReleation);
        }

        CompanyStoreRelation relation = CompanyStoreRelation.builder()
                .company(companyId)
                .store(storeId)
                .applyReleation(applyReleation)
                .status(APPROVAL)
                .build();
        companyStoreRelationService.save(relation);
        return CompanyStoreRelationVO.of(relation);
    }

    @PatchMapping("/companys/{companyId}/stores/{storeId}/company-store-relation/{id}")
    @ApiOperation("修改企业商家授信")
    @Transactional
    public void updateCompanyStoreRelation(@PathVariable Integer id, @Validated @RequestBody CompanyStoreRelationReq req) {
        CompanyStoreRelation companyStoreRelation = companyStoreRelationService.getById(id);

        if (APPROVAL == companyStoreRelation.getStatus()) {
            throw new BaseRuntimeException(COMPANY_STORE_RELATION_APPROVING);
        }

        Integer relation = companyStoreRelation.getRelation();
        if (Objects.isNull(relation)) {
            relation = 0;
        }

        Integer applyReleation = 0;
        boolean isAddRelation = false;

        if (req.getCanCashPay()) {
            if (CASH_PAY.isClose(relation)) {
                isAddRelation = true;
            }
            applyReleation = CASH_PAY.open(applyReleation);
        } else {
            relation = CASH_PAY.close(relation);
        }

        if (req.getCanCreditPay()) {
            if (CREDIT_PAY.isClose(relation)) {
                isAddRelation = true;
            }
            applyReleation = CREDIT_PAY.open(applyReleation);
        } else {
            relation = CREDIT_PAY.close(relation);
        }

        companyStoreRelationService.update(
                Wrappers.lambdaUpdate(CompanyStoreRelation.class)
                        .set(CompanyStoreRelation::getApplyReleation, applyReleation)
                        .set(CompanyStoreRelation::getRelation, relation == 0 ? null : relation)
                        .set(CompanyStoreRelation::getStatus, isAddRelation ? APPROVAL : WORKING)
                        .eq(CompanyStoreRelation::getId, companyStoreRelation.getId())
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

    @PatchMapping("companys/company-user/password")
    @ApiOperation("重置企业登录密码")
    public void resetLoginPassword(@Validated @RequestBody ResetloginPasswordReq req) {

        if (!verifyCodeService.checkVerifyCode(req.getVerifyCode(), req.getPhone())) {
            throw new BaseRuntimeException(ResultCode.VERIFY_CODE_FAIL);
        }
        CompanyUser companyUser = companyUserService.lambdaQuery()
                .eq(CompanyUser::getPhone, req.getPhone())
                .eq(CompanyUser::getStatus, CompanyUser.CompanyUserStatus.ACTIVE).one();

        if (ObjectUtil.isNull(companyUser)) {
            throw new BaseRuntimeException(ResultCode.COMPANY_NOT_EXIST);
        }

        companyUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
        companyUserService.updateById(companyUser);
    }

    @GetMapping("companys/{companyId}/departments")
    @ApiOperation("获取当前企业所有部门列表")
    public List<String> getAllDepartment(@PathVariable Integer companyId) {
        return companyMemberMapper.getAllDepartment(companyId);
    }
}
