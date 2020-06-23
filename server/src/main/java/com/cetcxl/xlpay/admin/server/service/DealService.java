package com.cetcxl.xlpay.admin.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.server.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.admin.server.dao.DealMapper;
import com.cetcxl.xlpay.admin.server.entity.model.Deal;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCash;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCredit;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

import static com.cetcxl.xlpay.admin.server.constants.ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST;
import static com.cetcxl.xlpay.admin.server.entity.model.Deal.PayType.CASH;
import static com.cetcxl.xlpay.admin.server.entity.model.Deal.PayType.CREDIT;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Service
public class DealService extends ServiceImpl<DealMapper, Deal> {
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @Data
    @Builder
    public static class DealForAdminParam {
        Integer walletId;
        Integer company;
        Deal.DealType dealType;
        BigDecimal amount;
        BigDecimal quota;
    }

    @Transactional
    public void dealCashForAdmin(DealForAdminParam param) {
        //todo 分布式锁待添加

        WalletCash walletCash = walletCashService.getById(param.getWalletId());
        if (Objects.isNull(walletCash)) {
            throw new BaseRuntimeException(COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        Deal deal = Deal.builder()
                .company(param.getCompany())
                .companyMember(walletCash.getCompanyMember())
                .amount(param.getAmount())
                .type(param.getDealType())
                .payType(CASH)
                .build();
        save(deal);

        walletCashService.process(deal, walletCash);
    }

    @Transactional
    public void dealCreditForAdmin(DealForAdminParam param) {
        //todo 分布式锁待添加

        WalletCredit walletCredit = walletCreditService.getById(param.getWalletId());
        if (Objects.isNull(walletCredit)) {
            throw new BaseRuntimeException(COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        Deal deal = Deal.builder()
                .company(param.getCompany())
                .companyMember(walletCredit.getCompanyMember())
                .amount(param.getAmount())
                .type(param.getDealType())
                .payType(CREDIT)
                .build();
        save(deal);
        walletCreditService.process(deal, walletCredit);
    }
}
