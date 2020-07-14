package com.cetcxl.xlpay.payuser.service;

import com.cetcxl.xlpay.common.constants.CommonResultCode;
import com.cetcxl.xlpay.common.entity.model.*;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.entity.vo.WalletCashVO;
import com.cetcxl.xlpay.payuser.entity.vo.WalletCreditVO;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.cetcxl.xlpay.payuser.constants.ResultCode.WALLET_RELATION_NOT_EXIST;

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
    @Autowired
    CompanyService companyService;
    @Autowired
    CompanyMemberService companyMemberService;
    @Autowired
    CompanyStoreRelationService companyStoreRelationService;
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
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
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
}
