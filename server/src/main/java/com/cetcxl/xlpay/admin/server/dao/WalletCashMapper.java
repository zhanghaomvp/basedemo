package com.cetcxl.xlpay.admin.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cetcxl.xlpay.admin.server.entity.model.WalletCash;
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
public interface WalletCashMapper extends BaseMapper<WalletCash> {
    @Data
    class WalletCashDTO {
        private String walletId;
        private String name;
        private String icNo;
        private String department;
        private String employeeNo;
        private BigDecimal cashBalance;
        private Integer status;
    }

    IPage<WalletCashDTO> listWalletCash(Page page, Integer companyId, String department, String name);
}
