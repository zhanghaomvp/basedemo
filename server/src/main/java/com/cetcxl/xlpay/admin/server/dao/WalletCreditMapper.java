package com.cetcxl.xlpay.admin.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCredit;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
public interface WalletCreditMapper extends BaseMapper<WalletCredit> {
    @Data
    class WalletCreditDTO {
        private Integer walletId;
        private Integer companyMemberId;
        private String name;
        private String icNo;
        private String department;
        private String employeeNo;

        private BigDecimal creditBalance;
        private BigDecimal creditQuota;
        private Integer status;
    }

    IPage<WalletCreditDTO> listWalletCredit(Page page, Integer companyId, String department, String name);

}
