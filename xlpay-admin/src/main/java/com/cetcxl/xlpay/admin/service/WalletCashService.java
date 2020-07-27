package com.cetcxl.xlpay.admin.service;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.dao.WalletCashMapper;
import com.cetcxl.xlpay.common.chaincode.entity.Order;
import com.cetcxl.xlpay.common.chaincode.entity.PersonalWallet;
import com.cetcxl.xlpay.common.chaincode.enums.DealType;
import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import com.cetcxl.xlpay.common.constants.CommonResultCode;
import com.cetcxl.xlpay.common.entity.model.*;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.common.service.ChainCodeService;
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
    @Autowired
    ChainCodeService chainCodeService;

    @Data
    @Builder
    public static class WalletCashProcessParam {
        Integer walletId;
        Company company;
        CompanyMember companyMember;
    }

    @Transactional
    public void process(Deal deal, WalletCashProcessParam param) {
        WalletCash walletCash = getById(param.getWalletId());
        if (Objects.isNull(walletCash)) {
            throw new BaseRuntimeException(COMPANY_MEMBER_WALLET_NOT_EXIST);
        }
        if (!walletCash.getCompanyMember().equals(deal.getCompanyMember())) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
        }

        WalletCashFlow cashFlow = WalletCashFlow.builder()
                .walletCash(walletCash.getId())
                .deal(deal.getId())
                .amount(deal.getAmount())
                .balance(walletCash.getCashBalance())
                .info("")
                .build();

        WalletCashFlow.CashFlowType flowType = null;
        String orderAmount = null;
        switch (deal.getType()) {
            case ADMIN_RECHARGE:
                flowType = WalletCashFlow.CashFlowType.PLUS;
                orderAmount = cashFlow.getAmount().toString();
                break;
            case ADMIN_REDUCE:
                flowType = WalletCashFlow.CashFlowType.MINUS;
                orderAmount = cashFlow.getAmount().negate().toString();
                break;
            default:
        }

        cashFlow.setType(flowType);
        cashFlow.caculateBalance();
        walletCashFlowService.save(cashFlow);

        update(
                Wrappers
                        .lambdaUpdate(WalletCash.class)
                        .set(WalletCash::getCashBalance, cashFlow.getBalance())
                        .eq(WalletCash::getId, walletCash.getId())
        );

        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(deal.getId().toString())
                        .companySocialCreditCode(param.getCompany().getSocialCreditCode())
                        .identityCard(param.getCompanyMember().getIcNo())
                        .amount(orderAmount)
                        .dealType(DealType.RECHARGE)
                        .employeeWalletNo(walletCash.getId().toString())
                        .payType(PayType.CASH)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(
                                param.getCompany().getSocialCreditCode() +
                                        "." +
                                        param.getCompanyMember().getIcNo()
                        )
                        .personalCashBalance(cashFlow.getBalance().toString())
                        .amount(orderAmount)
                        .dealType(DealType.RECHARGE)
                        .payType(PayType.CASH)
                        .tradeNo(deal.getId().toString())
                        .build(),
                null
        );
    }

    @Data
    @Builder
    @HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
    @HeadFontStyle(fontHeightInPoints = 15)
    public static class WalletCashExportRow {
        @ExcelProperty("姓名")
        String name;
        @ExcelProperty("工号")
        String employeeNo;
        @ExcelProperty("身份证号")
        @ColumnWidth(50)
        String icNo;
        @ExcelProperty("手机号")
        @ColumnWidth(20)
        String phone;
        @ExcelProperty("部门")
        String department;
        @ExcelProperty("余额")
        String balance;
        @ExcelProperty("状态")
        String status;
    }

    public List<WalletCashExportRow> listWalletCashExport(Integer companyId, String department, String name) {
        List<WalletCashMapper.WalletCashDTO> walletCashDtos = baseMapper.listWalletCash(
                companyId,
                department,
                name);

        return walletCashDtos.stream()
                .map(
                        dto ->
                                WalletCashExportRow.builder()
                                        .name(dto.getName())
                                        .icNo(dto.getIcNo())
                                        .employeeNo(dto.getEmployeeNo())
                                        .phone(dto.getPhone())
                                        .department(dto.getDepartment())
                                        .balance(dto.getCashBalance().toString())
                                        .status(dto.getStatus().getDesc())
                                        .build()
                )
                .collect(Collectors.toList());
    }
}
