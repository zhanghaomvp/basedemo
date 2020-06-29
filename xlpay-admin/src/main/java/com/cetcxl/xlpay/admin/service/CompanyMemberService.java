package com.cetcxl.xlpay.admin.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.dao.CompanyMemberMapper;
import com.cetcxl.xlpay.common.entity.model.WalletCash;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
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
 * @since 2020-06-15
 */
@Service
public class CompanyMemberService extends ServiceImpl<CompanyMemberMapper, CompanyMember> {
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @Transactional
    public CompanyMember addCompanyMember(CompanyMember companyMember) {
        save(companyMember);

        WalletCash walletCash = WalletCash.builder()
                .companyMember(companyMember.getId())
                .cashBalance(new BigDecimal(0))
                .status(WalletCash.WalletCashStaus.ENABLE)
                .build();
        walletCashService.save(walletCash);

        WalletCredit walletCredit = WalletCredit.builder()
                .companyMember(companyMember.getId())
                .creditBalance(new BigDecimal(0))
                .creditQuota(new BigDecimal(0))
                .status(WalletCredit.WalletCreditStaus.ENABLE)
                .build();
        walletCreditService.save(walletCredit);

        return companyMember;
    }

}
