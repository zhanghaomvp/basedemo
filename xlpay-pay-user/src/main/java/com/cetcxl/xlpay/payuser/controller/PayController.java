package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.common.controller.BaseController;
import com.cetcxl.xlpay.common.entity.model.*;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.entity.vo.DealVO;
import com.cetcxl.xlpay.payuser.entity.vo.WalletCashVO;
import com.cetcxl.xlpay.payuser.entity.vo.WalletCreditVO;
import com.cetcxl.xlpay.payuser.service.*;
import com.cetcxl.xlpay.payuser.util.ContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.SYSTEM_LOGIC_ERROR;
import static com.cetcxl.xlpay.payuser.constants.ResultCode.*;

@Validated
@RestController
@Api(tags = "支付及钱包接口")
public class PayController extends BaseController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PayUserService payUserService;
    @Autowired
    PayService payService;
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

    @GetMapping("/pay-user/company/{socialCreditCode}/wallet/cash")
    @ApiOperation("个人余额查询")
    public WalletCashVO getCashBalance(@PathVariable String socialCreditCode) {
        PayUser payUser = ContextUtil.getUserInfo().getPayUser();

        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getSocialCreditCode, socialCreditCode)
        );
        if (Objects.isNull(company)) {
            throw new BaseRuntimeException(COMPANY_NOT_EXIST);
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
        return WalletCashVO.of(walletCash, WalletCashVO.class);
    }

    @GetMapping("/pay-user/company/{socialCreditCode}/wallet/credit")
    @ApiOperation("个人信用额度查询")
    public WalletCreditVO getCreditBalance(@PathVariable String socialCreditCode) {
        PayUser payUser = ContextUtil.getUserInfo().getPayUser();

        Company company = companyService.getOne(
                Wrappers.lambdaQuery(Company.class)
                        .eq(Company::getSocialCreditCode, socialCreditCode)
        );
        if (Objects.isNull(company)) {
            throw new BaseRuntimeException(COMPANY_NOT_EXIST);
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

        return WalletCreditVO.of(walletCredit, WalletCreditVO.class);
    }

    @GetMapping("/pay-user/company/{socialCreditCode}/store/{storeId}/wallets")
    @ApiOperation("可支付钱包列表查询")
    public PayService.StoreWalletDTO storeWallet(@PathVariable Integer storeId, @PathVariable String socialCreditCode) {

        return payService
                .storeWallet(
                        ContextUtil.getUserInfo().getPayUser(),
                        socialCreditCode,
                        storeId);
    }

    @GetMapping("/pay-user/wallet/no-password-pay/status")
    @ApiOperation("是否当前交易支持免密支付")
    public Boolean canNoPasswordPay(@NotBlank String amount) {
        PayUser payUser = payUserService
                .getById(
                        ContextUtil.getUserInfo().getPayUser().getId()
                );

        return payService
                .checkNoPasswordPayValid(
                        payUser,
                        new BigDecimal(amount)
                );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("")
    public static class PayReq {
        @NotNull Integer storeId;
        @NotNull String amount;
        String password;
        String info;
    }

    @PostMapping("/pay-user/wallet/cash/{walletId}/deal")
    @ApiOperation("个人余额支付")
    public DealVO payCash(
            @PathVariable Integer walletId,
            @Validated @RequestBody PayReq req) {
        PayUser payUser = payUserService
                .getById(
                        ContextUtil.getUserInfo().getPayUser().getId()
                );

        BigDecimal decimal = new BigDecimal(req.getAmount());
        payUserService.checkPayValid(payUser, req.getPassword(), decimal);

        WalletCash walletCash = walletCashService.getById(walletId);
        if (Objects.isNull(walletCash)) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }
        if (WalletCash.WalletCashStaus.DISABLE == walletCash.getStatus()) {
            throw new BaseRuntimeException(WALLET_DISABLE);
        }

        //此处先做一次初步校验 实际扣款分布式锁加好后 还需再次校验
        walletCashService.checkEnoughBalance(walletCash, decimal);

        CompanyMember companyMember = companyMemberService.getById(walletCash.getCompanyMember());
        CompanyStoreRelation companyStoreRelation = companyStoreRelationService.getOne(
                Wrappers.lambdaQuery(CompanyStoreRelation.class)
                        .eq(CompanyStoreRelation::getCompany, companyMember.getCompany())
                        .eq(CompanyStoreRelation::getStore, req.getStoreId())
        );

        if (!CompanyStoreRelation.Relation.CASH_PAY
                .isOpen(companyStoreRelation.getRelation())) {
            throw new BaseRuntimeException(WALLET_RELATION_NOT_EXIST);
        }

        DealService.DealParam dealParam = DealService.DealParam.builder()
                .walletId(walletId)
                .companyMember(companyMember)
                .store(req.getStoreId())
                .amount(decimal)
                .dealType(Deal.DealType.CASH_DEAL)
                .info(req.getInfo())
                .build();

        return DealVO.of(dealService.dealCash(dealParam), DealVO.class);
    }

    @PostMapping("/pay-user/wallet/credit/{walletId}/deal")
    @ApiOperation("个人信用额度支付")
    public DealVO payCredit(
            @PathVariable Integer walletId,
            @Validated @RequestBody PayReq req) {
        PayUser payUser = payUserService
                .getById(
                        ContextUtil.getUserInfo().getPayUser().getId()
                );

        BigDecimal decimal = new BigDecimal(req.getAmount());
        payUserService.checkPayValid(payUser, req.getPassword(), decimal);

        WalletCredit walletCredit = walletCreditService.getById(walletId);
        if (Objects.isNull(walletCredit)) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }
        if (WalletCredit.WalletCreditStaus.DISABLE == walletCredit.getStatus()) {
            throw new BaseRuntimeException(WALLET_DISABLE);
        }

        //此处先做一次初步校验 实际扣款分布式锁加好后 还需再次校验
        walletCreditService.checkEnoughBalance(walletCredit, decimal);

        CompanyMember companyMember = companyMemberService.getById(walletCredit.getCompanyMember());
        CompanyStoreRelation companyStoreRelation = companyStoreRelationService.getOne(
                Wrappers.lambdaQuery(CompanyStoreRelation.class)
                        .eq(CompanyStoreRelation::getCompany, companyMember.getCompany())
                        .eq(CompanyStoreRelation::getStore, req.getStoreId())
        );

        if (CompanyStoreRelation.Relation.CREDIT_PAY
                .isClose(companyStoreRelation.getRelation())) {
            throw new BaseRuntimeException(WALLET_RELATION_NOT_EXIST);
        }

        DealService.DealParam dealParam = DealService.DealParam.builder()
                .walletId(walletId)
                .companyMember(companyMember)
                .store(req.getStoreId())
                .amount(decimal)
                .dealType(Deal.DealType.CREDIT_DEAL)
                .info(req.getInfo())
                .build();

        return DealVO.of(dealService.dealCredit(dealParam), DealVO.class);
    }
}
