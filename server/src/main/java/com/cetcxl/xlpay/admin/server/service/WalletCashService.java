package com.cetcxl.xlpay.admin.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.server.dao.WalletCashMapper;
import com.cetcxl.xlpay.admin.server.entity.model.Deal;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCash;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCashFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void process(Deal deal, WalletCash walletCash) {
        WalletCashFlow cashFlow = WalletCashFlow.builder()
                .walletCash(walletCash.getId())
                .deal(deal.getId())
                .info("")
                .build();

        WalletCashFlow.CashFlowType flowType = null;
        switch (deal.getType()) {
            case ADMIN_RECHARGE:
                flowType = WalletCashFlow.CashFlowType.PLUS;
                break;
            case ADMIN_REDUCE:
                flowType = WalletCashFlow.CashFlowType.MINUS;
                break;
        }

        cashFlow.setType(flowType);
        cashFlow.setAmount(deal.getAmount());
        cashFlow.caculateBalance(walletCash.getCashBalance());
        walletCashFlowService.save(cashFlow);

        update(
                Wrappers
                        .lambdaUpdate(WalletCash.class)
                        .set(WalletCash::getCashBalance, cashFlow.getBalance())
                        .eq(WalletCash::getId, walletCash.getId())
        );
    }
}
