package com.cetcxl.xlpay.admin.server.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.server.common.config.MybatisPlusConfig;
import com.cetcxl.xlpay.admin.server.common.constants.PatternConstants;
import com.cetcxl.xlpay.admin.server.common.controller.BaseController;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import com.cetcxl.xlpay.admin.server.constants.ResultCode;
import com.cetcxl.xlpay.admin.server.dao.StoreMapper;
import com.cetcxl.xlpay.admin.server.entity.model.Company;
import com.cetcxl.xlpay.admin.server.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.admin.server.entity.model.CompanyUser;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyStoreRelationVO;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyUserVO;
import com.cetcxl.xlpay.admin.server.entity.vo.CompanyVO;
import com.cetcxl.xlpay.admin.server.service.*;
import com.cetcxl.xlpay.admin.server.util.ContextUtil;
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

import static com.cetcxl.xlpay.admin.server.constants.ResultCode.COMPANY_STORE_RELATION_APPROVING;
import static com.cetcxl.xlpay.admin.server.entity.model.CompanyStoreRelation.Relation.CASH_PAY;
import static com.cetcxl.xlpay.admin.server.entity.model.CompanyStoreRelation.RelationStatus.APPROVAL;

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

    @Autowired
    StoreMapper storeMapper;

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
        String verifyCode;

        @ApiModelProperty(value = "企业名称", required = true)
        @NotBlank
        String name;

        @ApiModelProperty(value = "统一社会信用代码", required = true)
        @NotBlank
        String socialCreditCode;

    }

    @PostMapping("/companys/register")
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

    @GetMapping("/companys/{companyId}")
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

    @Data
    @ApiModel("")
    public static class ListStoresReq extends MybatisPlusConfig.PageReq {
        @NotNull Boolean hasRelation;
    }

    @GetMapping("/companys/{companyId}/stores")
    @ApiOperation("商家查询")
    @Transactional
    public ResBody<IPage<StoreMapper.CompanyStoreDTO>> listStores(ListStoresReq req) {
        UserDetailService.UserInfo user = ContextUtil.getUserInfo();

        if (req.hasRelation) {
            return ResBody
                    .success(
                            storeMapper
                                    .listCompanyStoresWithRelation(
                                            new Page(req.getPageNo(), req.getPageSize()),
                                            user.getCompanyId()
                                    )
                    );
        } else {
            return ResBody
                    .success(
                            storeMapper
                                    .listCompanyStoresNotWithRelation(
                                            new Page(req.getPageNo(), req.getPageSize()),
                                            user.getCompanyId()
                                    )
                    );
        }
    }

    @PostMapping("/companys/{companyId}/stores/{storeId}/company-store-relation")
    @ApiOperation("增加企业商家授信")
    @Transactional
    public ResBody addCompanyStoreRelation(@PathVariable Integer storeId, @NotNull Boolean canCashPay, @NotNull Boolean canCreditPay) {
        UserDetailService.UserInfo user = ContextUtil.getUserInfo();
        Integer companyId = user.getCompanyId();

        CompanyStoreRelation one = companyStoreRelationService.getOne(
                Wrappers.lambdaQuery(CompanyStoreRelation.class)
                        .eq(CompanyStoreRelation::getCompany, companyId)
                        .eq(CompanyStoreRelation::getStore, storeId)
        );

        if (Objects.nonNull(one)) {
            return ResBody.error(ResultCode.COMPANY_STORE_RELATION_EXIST);
        }

        int relationValue = 0;
        if (canCashPay) {
            relationValue = CASH_PAY.addRelation(relationValue);
        }
        if (canCreditPay) {
            relationValue = CompanyStoreRelation.Relation.CREDIT_PAY.addRelation(relationValue);
        }

        CompanyStoreRelation relation = CompanyStoreRelation.builder()
                .company(companyId)
                .store(storeId)
                .relation(relationValue)
                .status(APPROVAL)
                .build();
        companyStoreRelationService.save(relation);
        return ResBody.success(CompanyStoreRelationVO.of(relation));
    }

    @PatchMapping("/companys/{companyId}/stores/{storeId}/company-store-relation/{id}")
    @ApiOperation("修改企业商家授信")
    @Transactional
    public ResBody updateCompanyStoreRelation(@PathVariable Integer id, @NotNull Boolean canCashPay, @NotNull Boolean canCreditPay) {
        CompanyStoreRelation relation = companyStoreRelationService.getById(id);

        if (APPROVAL == relation.getStatus()) {
            return ResBody.error(COMPANY_STORE_RELATION_APPROVING);
        }

        Integer applyReleation = 0;
        if (canCashPay) {
            applyReleation = CASH_PAY.addRelation(applyReleation);
        }
        if (canCreditPay) {
            applyReleation = CompanyStoreRelation.Relation.CREDIT_PAY.addRelation(applyReleation);
        }

        companyStoreRelationService.update(
                Wrappers.lambdaUpdate(CompanyStoreRelation.class)
                        .set(CompanyStoreRelation::getApplyReleation, applyReleation)
                        .set(CompanyStoreRelation::getStatus, APPROVAL)
                        .eq(CompanyStoreRelation::getId, relation.getId())
        );
        return ResBody.success();
    }
}
