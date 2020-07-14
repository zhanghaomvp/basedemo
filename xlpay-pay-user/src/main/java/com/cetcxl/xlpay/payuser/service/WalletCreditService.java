package com.cetcxl.xlpay.payuser.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
import com.cetcxl.xlpay.common.entity.model.WalletCreditFlow;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.payuser.dao.WalletCreditMapper;
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

    @Transactional
    public void process(Deal deal, Integer walletId) {
        WalletCredit walletCredit = getById(walletId);

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
    }

    public void checkEnoughBalance(WalletCredit walletCredit, BigDecimal amount) {
        if (walletCredit.getCreditBalance()
                .subtract(amount)
                .signum() == -1) {
            throw new BaseRuntimeException(WALLET_BALANCE_NOT_ENOUGH);
        }
    }
}
