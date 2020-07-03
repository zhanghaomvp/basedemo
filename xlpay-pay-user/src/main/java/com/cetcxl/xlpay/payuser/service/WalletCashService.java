package com.cetcxl.xlpay.payuser.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCash;
import com.cetcxl.xlpay.common.entity.model.WalletCashFlow;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.payuser.dao.WalletCashMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.SYSTEM_LOGIC_ERROR;
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

    @Transactional
    public void process(Deal deal, Integer walletId) {
        WalletCash walletCash = getById(walletId);
        if (Objects.isNull(walletCash)) {
            throw new BaseRuntimeException(SYSTEM_LOGIC_ERROR);
        }

        if (walletCash.getCashBalance()
                .subtract(deal.getAmount())
                .signum() == -1) {
            throw new BaseRuntimeException(WALLET_BALANCE_NOT_ENOUGH);
        }

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
    }
}
