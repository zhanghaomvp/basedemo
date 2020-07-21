package com.cetcxl.xlpay.payuser.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cetcxl.xlpay.common.entity.model.*;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.entity.vo.WalletCashVO;
import com.cetcxl.xlpay.payuser.entity.vo.WalletCreditVO;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;

import static com.cetcxl.xlpay.common.entity.model.Deal.DealType.CASH_DEAL;
import static com.cetcxl.xlpay.common.entity.model.Deal.DealType.CREDIT_DEAL;
import static com.cetcxl.xlpay.payuser.constants.ResultCode.*;
import static com.cetcxl.xlpay.payuser.entity.model.PayUser.PayUserFuntion.NO_PASSWORD_PAY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-28
 */
@Service
public class PayService {
    public static final int NO_PASSWORD_PAY_AMOUNT_LIMIT = 100;
    public static final int NO_PASSWORD_PAY_TOTAL_AMOUNT_LIMIT = 500;

    @Autowired
    CompanyService companyService;
    @Autowired
    CompanyMemberService companyMemberService;
    @Autowired
    CompanyStoreRelationService companyStoreRelationService;
    @Autowired
    DealService dealService;
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class StoreWalletDTO {
        String companyName;
        WalletCashVO cashWallet;
        WalletCreditVO creditWallet;
    }

    public StoreWalletDTO storeWallet(PayUser payUser, String socialCreditCode, Integer storeId) {

        Company company = companyService
                .lambdaQuery()
                .eq(Company::getSocialCreditCode, socialCreditCode)
                .one();
        if (Objects.isNull(company)) {
            throw new BaseRuntimeException(COMPANY_NOT_EXIST);
        }

        CompanyStoreRelation companyStoreRelation = companyStoreRelationService.lambdaQuery()
                .eq(CompanyStoreRelation::getStore, storeId)
                .eq(CompanyStoreRelation::getCompany, company.getId())
                .ge(CompanyStoreRelation::getRelation, 0)
                .one();
        if (Objects.isNull(companyStoreRelation)) {
            throw new BaseRuntimeException(WALLET_RELATION_NOT_EXIST);
        }

        CompanyMember companyMember = companyMemberService.lambdaQuery()
                .eq(CompanyMember::getIcNo, payUser.getIcNo())
                .eq(CompanyMember::getCompany, company.getId())
                .one();
        if (Objects.isNull(companyMember)) {
            throw new BaseRuntimeException(COMPANY_MEMBER_NOT_EXIST);
        }

        StoreWalletDTO storeWalletDTO = StoreWalletDTO.builder()
                .companyName(company.getName())
                .build();

        if (CompanyStoreRelation.Relation.CASH_PAY
                .isOpen(companyStoreRelation.getRelation())) {
            storeWalletDTO.setCashWallet(
                    WalletCashVO.of(
                            walletCashService
                                    .lambdaQuery()
                                    .eq(WalletCash::getCompanyMember, companyMember.getId())
                                    .one(),
                            WalletCashVO.class)
            );
        }

        if (CompanyStoreRelation.Relation.CREDIT_PAY
                .isOpen(companyStoreRelation.getRelation())) {
            storeWalletDTO.setCreditWallet(
                    WalletCreditVO.of(
                            walletCreditService
                                    .lambdaQuery()
                                    .eq(WalletCredit::getCompanyMember, companyMember.getId())
                                    .one(),
                            WalletCreditVO.class)
            );
        }

        return storeWalletDTO;
    }

    public boolean checkNoPasswordPayValid(PayUser payUser, BigDecimal amount) {
        if (amount.intValue() > NO_PASSWORD_PAY_AMOUNT_LIMIT) {
            return false;
        }

        if (Objects.isNull(payUser.getFunctions()) || NO_PASSWORD_PAY.isClose(payUser.getFunctions())) {
            return false;
        }

        Map<String, Object> map = dealService
                .getMap(
                        new QueryWrapper<Deal>()
                                .select("sum(amount) as SUM")
                                .lambda()
                                .eq(Deal::getIcNo, payUser.getIcNo())
                                .ge(Deal::getCreated, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                                .in(Deal::getType, ImmutableList.of(CASH_DEAL, CREDIT_DEAL))
                );

        if (Objects.isNull(map)) {
            return true;
        }

        Object sum = map.get("SUM");
        if (Objects.isNull(sum)) {
            return true;
        }

        if (((BigDecimal) sum).intValue() < NO_PASSWORD_PAY_TOTAL_AMOUNT_LIMIT) {
            return true;
        }

        return false;
    }
}
