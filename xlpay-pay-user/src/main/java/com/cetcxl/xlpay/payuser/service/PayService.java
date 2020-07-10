package com.cetcxl.xlpay.payuser.service;

import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import com.cetcxl.xlpay.common.entity.model.WalletCash;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
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
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        WalletCashVO cashWallets;
        WalletCreditVO creditWallets;
    }

    public List<StoreWalletDTO> listStoreWallet(PayUser payUser, Integer storeId) {

        List<CompanyStoreRelation> companyStoreRelations = companyStoreRelationService.lambdaQuery()
                .eq(CompanyStoreRelation::getStore, storeId)
                .ge(CompanyStoreRelation::getRelation, 0)
                .list();

        if (CollectionUtils.isEmpty(companyStoreRelations)) {
            throw new BaseRuntimeException(WALLET_RELATION_NOT_EXIST);
        }

        List<CompanyMember> companyMembers = companyMemberService.lambdaQuery()
                .eq(CompanyMember::getIcNo, payUser.getIcNo())
                .in(
                        CompanyMember::getCompany,
                        companyStoreRelations
                                .stream()
                                .map(companyStoreRelation -> companyStoreRelation.getCompany())
                                .collect(Collectors.toList())
                )
                .list();

        List<WalletCash> walletCashes = walletCashService.lambdaQuery()
                .in(
                        WalletCash::getCompanyMember,
                        companyMembers.stream()
                                .map(CompanyMember::getId)
                                .collect(Collectors.toList()))
                .list()
                .stream()
                .filter(
                        walletCash -> {
                            Integer company = companyMembers.stream()
                                    .filter(companyMember -> companyMember.getId().equals(walletCash.getCompanyMember()))
                                    .findFirst()
                                    .get()
                                    .getCompany();

                            return companyStoreRelations.stream()
                                    .filter(companyStoreRelation -> companyStoreRelation.getCompany().equals(company))
                                    .filter(
                                            companyStoreRelation ->
                                                    CompanyStoreRelation.Relation.CASH_PAY
                                                            .isOpen(companyStoreRelation.getRelation())
                                    )
                                    .findAny()
                                    .isPresent();
                        }
                )
                .collect(Collectors.toList());

        List<WalletCredit> walletCredits = walletCreditService.lambdaQuery()
                .in(
                        WalletCredit::getCompanyMember,
                        companyMembers.stream()
                                .map(CompanyMember::getId)
                                .collect(Collectors.toList()))
                .list()
                .stream()
                .filter(
                        walletCredit -> {
                            Integer company = companyMembers.stream()
                                    .filter(companyMember -> companyMember.getId().equals(walletCredit.getCompanyMember()))
                                    .findFirst()
                                    .get()
                                    .getCompany();

                            return companyStoreRelations.stream()
                                    .filter(companyStoreRelation -> companyStoreRelation.getCompany().equals(company))
                                    .filter(
                                            companyStoreRelation ->
                                                    CompanyStoreRelation.Relation.CREDIT_PAY
                                                            .isOpen(companyStoreRelation.getRelation())
                                    )
                                    .findAny()
                                    .isPresent();
                        }
                )
                .collect(Collectors.toList());


        Map<Integer, StoreWalletDTO> storeWalletDtoMap = companyMembers.stream()
                .collect(
                        Collectors.toMap(
                                CompanyMember::getId,
                                companyMember ->
                                        StoreWalletDTO.builder()
                                                .companyName(
                                                        companyService
                                                                .getById(companyMember.getCompany())
                                                                .getName()
                                                )
                                                .build()
                        )
                );

        List<StoreWalletDTO> storeWalletDtoS = storeWalletDtoMap.entrySet()
                .stream()
                .map(
                        entry -> {
                            Optional<WalletCash> cashOptional = walletCashes.stream()
                                    .filter(
                                            walletCash ->
                                                    walletCash
                                                            .getCompanyMember()
                                                            .equals(entry.getKey())
                                    ).findFirst();

                            if (cashOptional.isPresent()) {
                                entry.getValue()
                                        .setCashWallets(WalletCashVO.of(cashOptional.get(), WalletCashVO.class));
                            }

                            Optional<WalletCredit> creditOptional = walletCredits.stream()
                                    .filter(
                                            walletCredit ->
                                                    walletCredit
                                                            .getCompanyMember()
                                                            .equals(entry.getKey())
                                    ).findFirst();

                            if (creditOptional.isPresent()) {
                                entry.getValue()
                                        .setCreditWallets(WalletCreditVO.of(creditOptional.get(), WalletCreditVO.class));
                            }

                            return entry.getValue();
                        }
                )
                .collect(Collectors.toList());

        return storeWalletDtoS;
    }
}
