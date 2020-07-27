package com.cetcxl.xlpay.payuser.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.common.chaincode.entity.BusinessWallet;
import com.cetcxl.xlpay.common.chaincode.entity.Order;
import com.cetcxl.xlpay.common.chaincode.entity.PersonalWallet;
import com.cetcxl.xlpay.common.chaincode.enums.DealType;
import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import com.cetcxl.xlpay.common.entity.model.*;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.common.service.ChainCodeService;
import com.cetcxl.xlpay.payuser.dao.WalletCreditMapper;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.cetcxl.xlpay.payuser.constants.ResultCode.WALLET_BALANCE_NOT_ENOUGH;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Service
public class WalletCreditService extends ServiceImpl<WalletCreditMapper, WalletCredit> {
    @Autowired
    WalletCreditFlowService walletCreditFlowService;
    @Autowired
    ChainCodeService chainCodeService;

    @Data
    @Builder
    public static class WalletCreditProcessParam {
        Integer walletId;
        Company company;
        CompanyMember companyMember;
        Store store;
    }

    @Transactional
    public void process(Deal deal, WalletCreditProcessParam param) {
        WalletCredit walletCredit = getById(param.getWalletId());

        checkEnoughBalance(walletCredit, deal.getAmount());

        WalletCreditFlow creditFlow = WalletCreditFlow.builder()
                .walletCredit(walletCredit.getId())
                .deal(deal.getId())
                .type(WalletCreditFlow.CreditFlowType.BALANCE_MINUS)
                .amount(deal.getAmount())
                .balance(walletCredit.getCreditBalance())
                .quota(walletCredit.getCreditQuota())
                .info(deal.getInfo())
                .build();

        creditFlow.caculateBanlanceAndQuota();
        walletCreditFlowService.save(creditFlow);

        update(
                Wrappers
                        .lambdaUpdate(WalletCredit.class)
                        .set(WalletCredit::getCreditBalance, creditFlow.getBalance())
                        .set(WalletCredit::getCreditQuota, creditFlow.getQuota())
                        .eq(WalletCredit::getId, walletCredit.getId())
        );

        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(deal.getId().toString())
                        .companySocialCreditCode(param.getCompany().getSocialCreditCode())
                        .identityCard(param.getCompanyMember().getIcNo())
                        .amount(creditFlow.getAmount().toString())
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(walletCredit.getId().toString())
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(
                                param.getCompany().getSocialCreditCode() +
                                        "." +
                                        param.getCompanyMember().getIcNo()
                        )
                        .personalCreditBalance(creditFlow.getBalance().toString())
                        .personalCreditLimit(creditFlow.getQuota().toString())
                        .amount(creditFlow.getAmount().toString())
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CREDIT)
                        .tradeNo(deal.getId().toString())
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(param.getStore().getSocialCreditCode())
                        .amount(creditFlow.getAmount().toString())
                        .payType(PayType.CREDIT)
                        .tradeNo(deal.getId().toString())
                        .build()
        );
    }

    public void checkEnoughBalance(WalletCredit walletCredit, BigDecimal amount) {
        if (walletCredit.getCreditBalance()
                .subtract(amount)
                .signum() == -1) {
            throw new BaseRuntimeException(WALLET_BALANCE_NOT_ENOUGH);
        }
    }
}
