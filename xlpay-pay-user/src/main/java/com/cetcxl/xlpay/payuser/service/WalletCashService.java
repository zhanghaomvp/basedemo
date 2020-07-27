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
import com.cetcxl.xlpay.payuser.dao.WalletCashMapper;
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
public class WalletCashService extends ServiceImpl<WalletCashMapper, WalletCash> {
    @Autowired
    WalletCashFlowService walletCashFlowService;
    @Autowired
    ChainCodeService chainCodeService;

    @Data
    @Builder
    public static class WalletCashProcessParam {
        Integer walletId;
        Company company;
        CompanyMember companyMember;
        Store store;
    }

    @Transactional
    public void process(Deal deal, WalletCashProcessParam param) {
        WalletCash walletCash = getById(param.getWalletId());

        checkEnoughBalance(walletCash, deal.getAmount());

        WalletCashFlow cashFlow = WalletCashFlow.builder()
                .walletCash(walletCash.getId())
                .deal(deal.getId())
                .type(WalletCashFlow.CashFlowType.MINUS)
                .amount(deal.getAmount())
                .balance(walletCash.getCashBalance())
                .info(deal.getInfo())
                .build();

        cashFlow.caculateBalance();
        walletCashFlowService.save(cashFlow);

        update(
                Wrappers
                        .lambdaUpdate(WalletCash.class)
                        .set(WalletCash::getCashBalance, cashFlow.getBalance())
                        .eq(WalletCash::getId, walletCash.getId())
        );

        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(deal.getId().toString())
                        .companySocialCreditCode(param.getCompany().getSocialCreditCode())
                        .identityCard(param.getCompanyMember().getIcNo())
                        .amount(cashFlow.getAmount().toString())
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(walletCash.getId().toString())
                        .payType(PayType.CASH)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(
                                param.getCompany().getSocialCreditCode() +
                                        "." +
                                        param.getCompanyMember().getIcNo()
                        )
                        .personalCashBalance(cashFlow.getBalance().toString())
                        .amount(cashFlow.getAmount().toString())
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CASH)
                        .tradeNo(deal.getId().toString())
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(param.getStore().getSocialCreditCode())
                        .amount(cashFlow.getAmount().toString())
                        .payType(PayType.CASH)
                        .tradeNo(deal.getId().toString())
                        .build()
        );
    }

    public void checkEnoughBalance(WalletCash walletCash, BigDecimal amount) {
        if (walletCash.getCashBalance()
                .subtract(amount)
                .signum() == -1) {
            throw new BaseRuntimeException(WALLET_BALANCE_NOT_ENOUGH);
        }
    }
}
