package com.cetcxl.xlpay.admin.service;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.common.entity.model.WalletCash;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.admin.dao.WalletCashMapper;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCashFlow;
import lombok.Builder;
import lombok.Data;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cetcxl.xlpay.admin.constants.ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Service
public class WalletCashService extends ServiceImpl<WalletCashMapper, WalletCash> {
    @Autowired
    WalletCashFlowService walletCashFlowService;

    @Transactional
    public void process(Deal deal, Integer walletId) {
        WalletCash walletCash = getById(walletId);
        if (Objects.isNull(walletCash)) {
            throw new BaseRuntimeException(COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        WalletCashFlow cashFlow = WalletCashFlow.builder()
                .walletCash(walletCash.getId())
                .deal(deal.getId())
                .info("")
                .build();

        WalletCashFlow.CashFlowType flowType = null;
        switch (deal.getType()) {
            case ADMIN_RECHARGE:
                flowType = WalletCashFlow.CashFlowType.PLUS;
                break;
            case ADMIN_REDUCE:
                flowType = WalletCashFlow.CashFlowType.MINUS;
                break;
        }

        cashFlow.setType(flowType);
        cashFlow.setAmount(deal.getAmount());
        cashFlow.caculateBalance(walletCash.getCashBalance());
        walletCashFlowService.save(cashFlow);

        update(
                Wrappers
                        .lambdaUpdate(WalletCash.class)
                        .set(WalletCash::getCashBalance, cashFlow.getBalance())
                        .eq(WalletCash::getId, walletCash.getId())
        );
    }

    @Data
    @Builder
    @HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
    @HeadFontStyle(fontHeightInPoints = 15)
    public static class WalletCashExportRow {
        @ExcelProperty("姓名")
        String name;
        @ExcelProperty("身份证号")
        @ColumnWidth(50)
        String icNo;
        @ExcelProperty("工号")
        String employeeNo;
        @ExcelProperty("部门")
        String department;
        @ExcelProperty("余额")
        String balance;
        @ExcelProperty("状态")
        String status;
    }

    public List<WalletCashExportRow> listWalletCashExport(Integer companyId, String department, String name) {
        List<WalletCashMapper.WalletCashDTO> walletCashDTOS = baseMapper.listWalletCash(
                companyId,
                department,
                name);

        return walletCashDTOS.stream()
                .map(
                        dto ->
                                WalletCashExportRow.builder()
                                        .name(dto.getName())
                                        .icNo(dto.getIcNo())
                                        .employeeNo(dto.getEmployeeNo())
                                        .department(dto.getDepartment())
                                        .balance(dto.getCashBalance().toString())
                                        .status(dto.getStatus().getDesc())
                                        .build()
                )
                .collect(Collectors.toList());
    }
}
