package com.cetcxl.xlpay.admin.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.server.dao.WalletCreditMapper;
import com.cetcxl.xlpay.admin.server.entity.model.Deal;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCredit;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCreditFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    public void process(Deal deal, WalletCredit walletCredit) {
        if (deal.getType() == Deal.DealType.ADMIN_QUOTA) {
            adjustQuota(deal, walletCredit);
        }
    }

    private void adjustQuota(Deal deal, WalletCredit walletCredit) {
        WalletCreditFlow creditFlow = WalletCreditFlow.builder()
                .walletCredit(walletCredit.getId())
                .deal(deal.getId())
                .build();

        BigDecimal subtract = deal.getAmount()
                .subtract(walletCredit.getCreditQuota());
        boolean isPlus = subtract.signum() > 0;

        WalletCreditFlow.CreditFlowType flowType = null;
        if (isPlus) {
            flowType = WalletCreditFlow.CreditFlowType.QUOTA_PLUS;
        } else {
            flowType = WalletCreditFlow.CreditFlowType.QUOTA_MINUS;
        }

        creditFlow.setType(flowType);
        creditFlow.setBalance(walletCredit.getCreditBalance());
        creditFlow.setQuota(walletCredit.getCreditQuota());
        creditFlow.setAmount(subtract.abs());
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

}
