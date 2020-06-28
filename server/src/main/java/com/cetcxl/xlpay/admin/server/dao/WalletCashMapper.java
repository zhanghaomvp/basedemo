package com.cetcxl.xlpay.admin.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCash;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
public interface WalletCashMapper extends BaseMapper<WalletCash> {
    @Data
    class WalletCashDTO {
        private Integer walletId;
        private Integer companyMemberId;
        private String name;
        private String icNo;
        private String department;
        private String employeeNo;
        private BigDecimal cashBalance;
        private WalletCash.WalletCashStaus status;
    }

    IPage<WalletCashDTO> listWalletCash(Page page, Integer companyId, String department, String name);

    List<WalletCashDTO> listWalletCash(Integer companyId, String department, String name);

}
