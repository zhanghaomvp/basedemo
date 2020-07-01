package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.*;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.service.*;
import com.cetcxl.xlpay.payuser.util.ContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.SYSTEM_LOGIC_ERROR;
import static com.cetcxl.xlpay.payuser.constants.ResultCode.COMPANY_NOT_EXIST;
import static com.cetcxl.xlpay.payuser.constants.ResultCode.WALLET_RELATION_NOT_EXIST;

@Validated
@RestController
@Api(tags = "支付及钱包接口")
public class PayController extends BaseController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PayUserService payUserService;
    @Autowired
    CompanyService companyService;
    @Autowired
    CompanyMemberService companyMemberService;
    @Autowired
    StoreService storeService;
    @Autowired
    CompanyStoreRelationService companyStoreRelationService;
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @Autowired
    DealService dealService;

    @GetMapping("/pay-user/{id}/company/{socialCreditCode}/wallet/cash")
    @ApiOperation("个人余额查询")
    public ResBody<String> getCashBalance(@PathVariable Integer id, @PathVariable String socialCreditCode) {
        PayUser payUser = ContextUtil.getUserInfo().getPayUser();

        if (payUser.getId() != id) {
            return ResBody.error(SYSTEM_LOGIC_ERROR);
        }

        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getSocialCreditCode, socialCreditCode)
        );
        if (Objects.isNull(company)) {
            return ResBody.error(COMPANY_NOT_EXIST);
        }

        CompanyMember companyMember = companyMemberService.getOne(
                Wrappers.lambdaQuery(CompanyMember.class)
                        .eq(CompanyMember::getIcNo, payUser.getIcNo())
                        .eq(CompanyMember::getCompany, company.getId())
        );

        WalletCash walletCash = walletCashService.getOne(
                Wrappers.lambdaQuery(WalletCash.class)
                        .eq(WalletCash::getCompanyMember, companyMember.getId())
        );

        return ResBody.success(walletCash.getCashBalance().toString());
    }

    @GetMapping("/pay-user/{id}/company/{socialCreditCode}/wallet/credit")
    @ApiOperation("个人信用额度查询")
    public ResBody<String> getCreditBalance(@PathVariable Integer id, @PathVariable String socialCreditCode) {
        PayUser payUser = ContextUtil.getUserInfo().getPayUser();

        if (payUser.getId() != id) {
            return ResBody.error(SYSTEM_LOGIC_ERROR);
        }

        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getSocialCreditCode, socialCreditCode)
        );
        if (Objects.isNull(company)) {
            return ResBody.error(COMPANY_NOT_EXIST);
        }

        CompanyMember companyMember = companyMemberService.getOne(
                Wrappers.lambdaQuery(CompanyMember.class)
                        .eq(CompanyMember::getIcNo, payUser.getIcNo())
                        .eq(CompanyMember::getCompany, company.getId())
        );

        WalletCredit walletCredit = walletCreditService.getOne(
                Wrappers.lambdaQuery(WalletCredit.class)
                        .eq(WalletCredit::getCompanyMember, companyMember.getId())
        );

        return ResBody.success(walletCredit.getCreditBalance().toString());
    }


    @PostMapping("/pay-user/{id}/wallet/cash/{walletId}/deal")
    @ApiOperation("个人余额支付")
    public ResBody payCash(
            @PathVariable Integer id,
            @PathVariable Integer walletId,
            @NotNull Integer storeId,
            @NotNull String amount,
            @NotBlank String password,
            String info) {
        WalletCash walletCash = walletCashService.getById(walletId);
        CompanyMember companyMember = companyMemberService.getById(walletCash.getCompanyMember());

        CompanyStoreRelation companyStoreRelation = companyStoreRelationService.getOne(
                Wrappers.lambdaQuery(CompanyStoreRelation.class)
                        .eq(CompanyStoreRelation::getCompany, companyMember.getCompany())
                        .eq(CompanyStoreRelation::getStore, storeId)
        );

        if (!CompanyStoreRelation.Relation.CASH_PAY
                .isOpen(companyStoreRelation.getRelation())) {
            return ResBody.error(WALLET_RELATION_NOT_EXIST);
        }

        DealService.DealParam dealParam = DealService.DealParam.builder()
                .walletId(walletId)
                .company(companyMember.getCompany())
                .companyMember(companyMember.getId())
                .store(storeId)
                .amount(new BigDecimal(amount))
                .dealType(Deal.DealType.CASH_DEAL)
                .info(info)
                .build();

        dealService.dealCash(dealParam);
        return ResBody.success();
    }
}
