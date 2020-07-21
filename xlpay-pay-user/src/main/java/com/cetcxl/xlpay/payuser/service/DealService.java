package com.cetcxl.xlpay.payuser.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.common.component.RedisLockComponent;
import com.cetcxl.xlpay.common.constants.Constants;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.payuser.dao.DealMapper;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.cetcxl.xlpay.common.entity.model.Deal.PayType.CASH;
import static com.cetcxl.xlpay.common.entity.model.Deal.PayType.CREDIT;


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
    CompanyService companyService;
    @Autowired
    StoreService storeService;
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @Data
    @Builder
    public static class DealParam {
        Integer walletId;
        CompanyMember companyMember;
        Integer store;
        Deal.DealType dealType;
        BigDecimal amount;
        BigDecimal quota;
        String info;
    }

    @Transactional
    public Deal dealCash(DealParam param) {
        try (RedisLockComponent.RedisLock redisLock =
                     new RedisLockComponent.RedisLock(Constants.KEY_CASH_DEAL + param.getWalletId())) {
            Deal deal = process(param, CASH);
            walletCashService.process(
                    deal,
                    WalletCashService.WalletCashProcessParam.builder()
                            .company(companyService.getById(param.getCompanyMember().getCompany()))
                            .companyMember(param.getCompanyMember())
                            .walletId(param.getWalletId())
                            .store(storeService.getById(param.getStore()))
                            .build()
            );
            return deal;
        }
    }

    @Transactional
    public Deal dealCredit(DealParam param) {
        try (RedisLockComponent.RedisLock redisLock =
                     new RedisLockComponent.RedisLock(Constants.KEY_CREDIT_DEAL + param.getWalletId())) {
            Deal deal = process(param, CREDIT);
            walletCreditService.process(
                    deal,
                    WalletCreditService.WalletCreditProcessParam.builder()
                            .company(companyService.getById(param.getCompanyMember().getCompany()))
                            .companyMember(param.getCompanyMember())
                            .walletId(param.getWalletId())
                            .store(storeService.getById(param.getStore()))
                            .build()
            );
            return deal;
        }
    }

    private Deal process(DealParam param, Deal.PayType payType) {
        Deal deal = Deal.builder()
                .company(param.getCompanyMember().getCompany())
                .companyMember(param.getCompanyMember().getId())
                .icNo(param.getCompanyMember().getIcNo())
                .store(param.getStore())
                .amount(param.getAmount())
                .type(param.getDealType())
                .payType(payType)
                .info(param.getInfo())
                .status(Deal.Status.PAID)
                .build();
        save(deal);
        return deal;
    }
}
