package com.cetcxl.xlpay.admin.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.admin.controller.DealsController;
import com.cetcxl.xlpay.admin.dao.DealMapper;
import com.cetcxl.xlpay.admin.exception.DealCashImportException;
import com.cetcxl.xlpay.admin.util.ContextUtil;
import com.cetcxl.xlpay.common.component.RedisLockComponent;
import com.cetcxl.xlpay.common.constants.XlpayConstants;
import com.cetcxl.xlpay.common.entity.model.Company;
import com.cetcxl.xlpay.common.entity.model.CompanyMember;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCash;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cetcxl.xlpay.common.entity.model.Deal.PayType.CASH;
import static com.cetcxl.xlpay.common.entity.model.Deal.PayType.CREDIT;
import static com.cetcxl.xlpay.common.entity.model.WalletCash.WalletCashStaus.DISABLE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-19
 */
@Service
public class DealService extends ServiceImpl<DealMapper, Deal> {
    @Autowired
    CompanyMemberService companyMemberService;
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @Data
    @Builder
    @HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
    @HeadFontStyle(fontHeightInPoints = 15)
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER)
    public static class DealExportRow {
        public static final List<String> EXCLUDE_COMPANY_NAME = ImmutableList.of("companyName");
        public static final List<String> EXCLUDE_STORE_NAME = ImmutableList.of("storeName");

        @ExcelProperty("企业名称")
        @ColumnWidth(30)
        String companyName;

        @ExcelProperty("商家名称")
        @ColumnWidth(30)
        String storeName;

        @ExcelProperty("姓名")
        @ColumnWidth(25)
        String name;

        @ExcelProperty("交易金额")
        @ColumnWidth(25)
        String amount;

        @ExcelProperty("交易类型")
        @ColumnWidth(25)
        String payType;

        @ExcelProperty("交易时间")
        @ColumnWidth(40)
        LocalDateTime transTime;

        @ExcelProperty("结算状态")
        @ColumnWidth(25)
        String status;
    }

    public List<DealExportRow> listDealExport(DealsController.ListDealReq req) {
        List<DealMapper.DealDTO> dealDtoS = baseMapper.listDealExport(req);

        return dealDtoS.stream()
                .map(
                        dto ->
                                DealExportRow.builder()
                                        .name(dto.getName())
                                        .storeName(dto.getStoreName())
                                        .companyName(dto.getCompanyName())
                                        .amount(dto.getAmount().toString())
                                        .transTime(dto.getCreated())
                                        .payType(dto.getPayType().getDesc())
                                        .status(dto.getStatus().getDesc())
                                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<DealExportRow> listDetailExport(Integer batch) {
        List<DealMapper.DealDTO> dealDtoS = baseMapper.listCheckDealsExport(batch);

        return dealDtoS.stream()
                .map(
                        dto ->
                                DealExportRow.builder()
                                        .name(dto.getName())
                                        .storeName(dto.getStoreName())
                                        .companyName(dto.getCompanyName())
                                        .amount(dto.getAmount().toString())
                                        .transTime(dto.getCreated())
                                        .payType(dto.getPayType().getDesc())
                                        .status(dto.getStatus().getDesc())
                                        .build()
                )
                .collect(Collectors.toList());
    }

    @Data
    @Builder
    public static class DealForAdminParam {
        Integer walletId;
        Company company;
        CompanyMember companyMember;
        Deal.DealType dealType;
        BigDecimal amount;
        BigDecimal quota;
    }

    /*
     **********************余额交易 start**********************
     */

    public void batchDealCashForAdmin(List<DealForAdminParam> params) {
        params.forEach(
                param -> dealCashForAdmin(param)
        );
    }

    @Transactional
    public void dealCashForAdmin(DealForAdminParam param) {
        try (RedisLockComponent.RedisLock redisLock =
                     new RedisLockComponent.RedisLock(XlpayConstants.LOCK_CASH_DEAL + param.getWalletId())) {
            Deal deal = Deal.builder()
                    .company(param.getCompany().getId())
                    .companyMember(param.getCompanyMember().getId())
                    .amount(param.getAmount())
                    .type(param.getDealType())
                    .payType(CASH)
                    .build();

            save(deal);

            walletCashService.process(
                    deal,
                    WalletCashService.WalletCashProcessParam.builder()
                            .walletId(param.getWalletId())
                            .company(param.getCompany())
                            .companyMember(param.getCompanyMember())
                            .build()
            );
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
    @HeadFontStyle(fontHeightInPoints = 15)
    public static class DealCashImportRow {
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
        @ExcelProperty("变更类型(充值/扣减)")
        @ColumnWidth(30)
        String type;
        @ExcelProperty("金额")
        String amount;

        public static List<DealCashImportRow> data() {
            return Lists
                    .newArrayList(
                            DealCashImportRow
                                    .builder()
                                    .name("张三")
                                    .employeeNo("10001")
                                    .icNo("511528209909010018")
                                    .phone("15828580081")
                                    .department("区块链")
                                    .type("充值")
                                    .amount("100")
                                    .build(),
                            DealCashImportRow
                                    .builder()
                                    .name("李四")
                                    .employeeNo("10002")
                                    .icNo("511528209909010020")
                                    .phone("15828580082")
                                    .type("扣减")
                                    .department("重点实验室")
                                    .amount("50")
                                    .build()
                    );
        }

        public enum TypeEnum {
            充值,
            扣减,
            ;
        }
    }

    @Slf4j
    @Getter
    public static class DealCashImportListener extends AnalysisEventListener<DealCashImportRow> {
        private int total = 0;
        private List<DealCashImportRow> failRows = Lists.newArrayList();

        private DealService dealService;

        public DealCashImportListener(DealService dealService) {
            this.dealService = dealService;
        }

        @Override
        public void invoke(DealCashImportRow dealCashImportBean, AnalysisContext analysisContext) {
            try {
                dealService.dealCashImport(dealCashImportBean);
            } catch (DealCashImportException e) {
                log.error(" DealCashImportListener invoke error -> {}", analysisContext.readRowHolder().getRowIndex());
                failRows.add(dealCashImportBean);
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            total = analysisContext.readRowHolder().getRowIndex();
        }
    }

    @Transactional
    public void dealCashImport(DealCashImportRow row) throws DealCashImportException {
        CompanyMember companyMember = companyMemberService.getOne(Wrappers.lambdaQuery(CompanyMember.class)
                .eq(CompanyMember::getIcNo, row.getIcNo()));
        if (Objects.isNull(companyMember)) {
            throw new DealCashImportException();
        }

        Deal.DealType dealType = null;
        if (DealCashImportRow.TypeEnum.充值.name().equals(row.getType())) {
            dealType = Deal.DealType.ADMIN_RECHARGE;
        }
        if (DealCashImportRow.TypeEnum.扣减.name().equals(row.getType())) {
            dealType = Deal.DealType.ADMIN_REDUCE;
        }

        if (Objects.isNull(dealType)) {
            throw new DealCashImportException();
        }

        WalletCash walletCash = walletCashService.getOne(
                Wrappers.lambdaQuery(WalletCash.class)
                        .eq(WalletCash::getCompanyMember, companyMember.getId())
        );
        if (Objects.isNull(walletCash) || walletCash.getStatus() == DISABLE) {
            throw new DealCashImportException();
        }

        try (RedisLockComponent.RedisLock redisLock =
                     new RedisLockComponent.RedisLock(XlpayConstants.LOCK_CASH_DEAL + walletCash.getId())) {
            Deal deal = Deal.builder()
                    .company(ContextUtil.getUserInfo().getCompany().getId())
                    .companyMember(companyMember.getId())
                    .amount(new BigDecimal(row.getAmount()))
                    .type(dealType)
                    .payType(CASH)
                    .build();
            save(deal);

            walletCashService.process(
                    deal,
                    WalletCashService.WalletCashProcessParam.builder()
                            .walletId(walletCash.getId())
                            .company(ContextUtil.getUserInfo().getCompany())
                            .companyMember(companyMember)
                            .build()
            );
        } catch (Exception e) {
            throw new DealCashImportException(e);
        }
    }

    /*
     **********************余额交易 end**********************
     */

    /*
     **********************信用交易 start**********************
     */

    public void batchDealCreditForAdmin(List<DealForAdminParam> params) {
        params.forEach(
                param -> dealCreditForAdmin(param)
        );
    }

    @Transactional
    public void dealCreditForAdmin(DealForAdminParam param) {
        try (RedisLockComponent.RedisLock redisLock =
                     new RedisLockComponent.RedisLock(XlpayConstants.LOCK_CREDIT_DEAL + param.getWalletId())) {

            Deal deal = Deal.builder()
                    .company(param.getCompany().getId())
                    .companyMember(param.getCompanyMember().getId())
                    .amount(param.getAmount())
                    .type(param.getDealType())
                    .payType(CREDIT)
                    .build();
            save(deal);

            walletCreditService.process(
                    deal,
                    WalletCreditService.WalletCreditProcessParam.builder()
                            .walletId(param.getWalletId())
                            .company(param.getCompany())
                            .companyMember(param.getCompanyMember())
                            .build()
            );
        }
    }

    /*
     **********************信用交易 end**********************
     */

    /**
     * @param dashboardDTO
     * @return DealMapper.DashboardDTO
     * 计算数据面板的值
     */
    public DealMapper.DashboardDTO calculationAmount(DealMapper.DashboardDTO dashboardDTO) {

        if (ObjectUtil.isNull(dashboardDTO)) {
            return dashboardDTO;
        }
        dashboardDTO.setTotalCheckAmount(
                dashboardDTO.getCashCheckAmount()
                        .add(dashboardDTO.getCreditCheckAmount())
        );
        dashboardDTO.setCreditUncheckAmount(dashboardDTO.getCreditAmount()
                .subtract(dashboardDTO.getCreditCheckAmount())
        );
        dashboardDTO.setCashUncheckAmount(
                dashboardDTO.getCashAmount()
                        .subtract(dashboardDTO.getCashCheckAmount())
        );
        dashboardDTO.setTotalUncheckAmount(
                dashboardDTO.getTotalAmount().
                        subtract(
                                dashboardDTO.getCashCheckAmount()
                                        .add(dashboardDTO.getCreditCheckAmount())
                        )
        );

        return dashboardDTO;
    }
}
