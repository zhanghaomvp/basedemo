package com.cetcxl.xlpay.admin.service;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.constants.ResultCode;
import com.cetcxl.xlpay.admin.dao.WalletCreditMapper;
import com.cetcxl.xlpay.common.chaincode.entity.Order;
import com.cetcxl.xlpay.common.chaincode.entity.PersonalWallet;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cetcxl.xlpay.common.chaincode.enums.DealType.LIMIT_CHANGE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Service
public class WalletCreditService extends ServiceImpl<WalletCreditMapper, WalletCredit> {
    @Autowired
    WalletCreditFlowService walletCreditFlowService;
    @Autowired
    ChainCodeService chainCodeService;

    @Data
    @Builder
    public static class WalletCreditProcessParam {
        Integer walletId;
        Company company;
        CompanyMember companyMember;
    }

    @Transactional
    public void process(Deal deal, WalletCreditProcessParam param) {
        WalletCredit walletCredit = getById(param.getWalletId());
        if (Objects.isNull(walletCredit)) {
            throw new BaseRuntimeException(ResultCode.COMPANY_MEMBER_WALLET_NOT_EXIST);
        }

        if (!walletCredit.getCompanyMember().equals(deal.getCompanyMember())) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
        }

        WalletCreditFlow creditFlow = WalletCreditFlow.builder()
                .walletCredit(walletCredit.getId())
                .deal(deal.getId())
                .info("")
                .build();

        if (deal.getType() != Deal.DealType.ADMIN_QUOTA) {
            throw new BaseRuntimeException(CommonResultCode.SYSTEM_LOGIC_ERROR);
        }

        WalletCreditFlow.CreditFlowType flowType = null;

        BigDecimal subtract = deal.getAmount()
                .subtract(walletCredit.getCreditQuota());
        boolean isPlus = subtract.signum() > 0;
        if (isPlus) {
            flowType = WalletCreditFlow.CreditFlowType.QUOTA_PLUS;
        } else {
            flowType = WalletCreditFlow.CreditFlowType.QUOTA_MINUS;
        }

        creditFlow.setType(flowType);
        creditFlow.setBalance(walletCredit.getCreditBalance());
        creditFlow.setQuota(walletCredit.getCreditQuota());
        creditFlow.setAmount(subtract.abs());
        creditFlow.caculateBanlanceAndQuota();

        walletCreditFlowService.save(creditFlow);

        update(
                Wrappers
                        .lambdaUpdate(WalletCredit.class)
                        .set(WalletCredit::getCreditBalance, creditFlow.getBalance())
                        .set(WalletCredit::getCreditQuota, creditFlow.getQuota())
                        .eq(WalletCredit::getId, walletCredit.getId())
        );

        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(deal.getId().toString())
                        .companySocialCreditCode(param.getCompany().getSocialCreditCode())
                        .identityCard(param.getCompanyMember().getIcNo())
                        .amount(creditFlow.getQuota().toString())
                        .dealType(LIMIT_CHANGE)
                        .employeeWalletNo(
                                param.getCompany().getSocialCreditCode() +
                                        "." +
                                        param.getCompanyMember().getIcNo()
                        )
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(
                                param.getCompany().getSocialCreditCode() +
                                        "." +
                                        param.getCompanyMember().getIcNo()
                        )
                        .personalCreditBalance(creditFlow.getBalance().toString())
                        .personalCreditLimit(creditFlow.getQuota().toString())
                        .amount(creditFlow.getQuota().toString())
                        .dealType(LIMIT_CHANGE)
                        .payType(PayType.CREDIT)
                        .tradeNo(deal.getId().toString())
                        .build(),
                null
        );
    }

    @Data
    @Builder
    @HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
    @HeadFontStyle(fontHeightInPoints = 15)
    public static class WalletCreditExportRow {
        @ExcelProperty("姓名")
        String name;
        @ExcelProperty("身份证号")
        @ColumnWidth(50)
        String icNo;
        @ExcelProperty("手机号")
        @ColumnWidth(20)
        String phone;
        @ExcelProperty("工号")
        String employeeNo;
        @ExcelProperty("部门")
        String department;
        @ExcelProperty("最大额度")
        String quota;
        @ExcelProperty("待结算额度")
        String usedQuota;
        @ExcelProperty("剩余额度")
        String balance;
        @ExcelProperty("账户状态")
        String status;
    }

    public List<WalletCreditExportRow> listWalletCreditExport(Integer companyId, String department, String name) {
        List<WalletCreditMapper.WalletCreditDTO> walletCashDtos = baseMapper.listWalletCredit(
                companyId,
                department,
                name
        );

        return walletCashDtos.stream()
                .map(
                        dto ->
                                WalletCreditExportRow.builder()
                                        .name(dto.getName())
                                        .icNo(dto.getIcNo())
                                        .phone(dto.getPhone())
                                        .employeeNo(dto.getEmployeeNo())
                                        .department(dto.getDepartment())
                                        .status(dto.getStatus().getDesc())
                                        .quota(dto.getCreditQuota().toString())
                                        .balance(dto.getCreditBalance().toString())
                                        .usedQuota(dto.getCreditQuota().subtract(dto.getCreditBalance()).toString())
                                        .build()
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public void payment(Deal deal) {
        WalletCredit walletCredit = lambdaQuery()
                .eq(WalletCredit::getCompanyMember, deal.getCompanyMember())
                .one();

        WalletCreditFlow creditFlow = WalletCreditFlow.builder()
                .walletCredit(walletCredit.getId())
                .deal(deal.getId())
                .type(WalletCreditFlow.CreditFlowType.PAYMENT)
                .balance(walletCredit.getCreditBalance())
                .quota(walletCredit.getCreditQuota())
                .amount(deal.getAmount())
                .info(WalletCreditFlow.CreditFlowType.PAYMENT.name())
                .build();

        creditFlow.caculateBanlanceAndQuota();
        walletCreditFlowService.save(creditFlow);

        lambdaUpdate()
                .set(WalletCredit::getCreditBalance, creditFlow.getBalance())
                .set(WalletCredit::getCreditQuota, creditFlow.getQuota())
                .eq(WalletCredit::getId, walletCredit.getId())
                .update();

    }

}
