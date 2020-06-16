package com.cetcxl.xlpay.admin.server.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.server.dao.CompanyMemberMapper;
import com.cetcxl.xlpay.admin.server.entity.model.CompanyMember;
import com.cetcxl.xlpay.admin.server.entity.model.Wallet;
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
    WalletService walletService;

    @Transactional
    public CompanyMember addCompanyMember(CompanyMember companyMember) {
        Wallet wallet = Wallet.builder()
                .cashBalance(new BigDecimal(0))
                .creditBalance(new BigDecimal(0))
                .build();
        walletService.save(wallet);

        companyMember.setWallet(wallet.getId());
        save(companyMember);

        return companyMember;
    }

}
